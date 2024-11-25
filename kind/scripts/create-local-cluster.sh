#!/bin/bash

if [ "$2" = "" ] || [ "$1" = "" ]; then
  echo "Usage: create-local-cluster.sh <cluster-name> <port-index>"
  echo "<port-index> must be between 1 and 9"
  exit 1
fi

echo -e '\nCreating cluster starting' `date`
kind create cluster --config kind-config-$1.yml --name $1

kubectl config use-context kind-$1

create_or_update_namespace() {
  NAME=$1
  EXISTS=`kubectl get namespace $NAME -o custom-columns=:metadata.name --ignore-not-found`
  if [ "$EXISTS" == "" ]; then
      kubectl create namespace $NAME
  fi
}

echo -e '\nUpdating local helm repo'
helm repo add grafana https://grafana.github.io/helm-charts
helm repo add cnpg https://cloudnative-pg.github.io/charts
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo add external-secrets https://charts.external-secrets.io
helm repo update

create_or_update_namespace default

# This is done first so that the daemon sets can be set up and log collection started
echo -e '\nAdding grafana/loki'
create_or_update_namespace monitoring
pod_status=$(kubectl get pod loki-0 -n monitoring -o jsonpath='{.status.phase}' 2> /dev/null)
if [[ "$pod_status" != "" ]]; then
  echo "Loki/Grafana already installed - skipping installation and configuration"
else
  helm install loki grafana/loki-stack -n monitoring -f helm/loki-config.yml --wait
  kubectl apply -f k8s/grafana-svc.yml
fi

echo -e '\nInstalling vault'
create_or_update_namespace vault
pod_status=$(kubectl get pod vault-0 -n vault -o jsonpath='{.status.phase}' 2> /dev/null)
if [[ "$pod_status" != "" ]]; then
  echo "Vault already installed - skipping installation and configuration"
else
  helm install vault hashicorp/vault -f helm/vault-config-sa.yml -n vault --wait

  while true ; do
    pod_status=$(kubectl get pod vault-0 -n vault -o jsonpath='{.status.phase}' 2> /dev/null)
    if [[ "$pod_status" == "Running" ]]; then
      break
    fi
  done

  echo -e '\nInitialising and unsealing vault (see vault_creds.tmp for details)'
  kubectl exec -it vault-0 -n vault -- vault operator init -n 1 -t 1 | grep -e Key -e Token > vault_creds.tmp
  UNSEAL=`sed -n '1s/^.*: //p' vault_creds.tmp`
  UNSEAL_TRIMMED=`echo $UNSEAL | sed 's/\x1b[^m]*m//g' | tr -dc '[:alnum:]+=/'`
  UNSEAL_QUOTED=`echo '"'$UNSEAL_TRIMMED'"'`
  JSON='{"key":"'${UNSEAL_TRIMMED}'"}'
  curl -X POST "http://localhost:3140$2/v1/sys/unseal" -d $JSON
  echo -e "Vault unsealed - credentials in vault_creds.tmp"

  echo Configuring vault engines and secrets
  TOKEN=`sed -n '2s/^.*: //p' vault_creds.tmp`
  VAULT_TOKEN=`echo $TOKEN | sed 's/\x1b[^m]*m//g' | tr -dc '[:alnum:]+=/.'`

  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/sys/mounts/aquarium-api" -d @vault/enable-kv-engine.json
  kubectl apply -f k8s/aquarium-service-account.yml
fi

# capture vault token in case it was not captured earlier due to skipping
TOKEN=`sed -n '2s/^.*: //p' vault_creds.tmp`
VAULT_TOKEN=`echo $TOKEN | sed 's/\x1b[^m]*m//g' | tr -dc '[:alnum:]+=/.'`

echo -e '\nInstalling external secrets'
create_or_update_namespace eso
pod_status=$(kubectl get pod -n eso -o jsonpath='{.items[0].status.phase}' 2> /dev/null)
if [[ "$pod_status" != "" ]]; then
  echo "External secrets already installed - skipping installation and configuration"
else
  helm install external-secrets external-secrets/external-secrets -n eso --wait
  kubectl create secret generic vault-token --from-literal=token=${VAULT_TOKEN}
  kubectl apply -f k8s/secret-store.yml
  # create static DB credentials for k8s-debug cluster
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/aquarium-api/data/db" -d @vault/static-db-creds.json
  kubectl apply -f k8s/external-secrets-db.yml
fi


echo -e '\nAdding postgres operator and creating database'
create_or_update_namespace pg
pod_status=$(kubectl get pod -n pg -o jsonpath='{.items[0].status.phase}' 2> /dev/null)
if [[ "$pod_status" != "" ]]; then
  echo "Postgres operator already installed - skipping installation and configuration"
else
  helm install cnpg cnpg/cloudnative-pg -n pg --wait
fi

echo -e '\nDeploying postgres database'
pod_status=$(kubectl get pod db-cluster-1 -n pg -o jsonpath='{.status.phase}' 2> /dev/null)
if [[ "$pod_status" != "" ]]; then
  echo "Postgres DB already installed - skipping installation and configuration"
else
  kubectl apply -f k8s/db-user-config.yml
  kubectl apply -f k8s/db-config.yml
  echo 'Waiting for database cluster to be created'
  while true; do
    pod_status=$(kubectl get pod db-cluster-1 -n pg -o jsonpath='{.status.phase}' 2> /dev/null)
    if [[ "$pod_status" == "Running" ]]; then
      break
    fi
  done
  echo 'Waiting for database cluster to be ready'
  kubectl wait pod db-cluster-1 -n pg --for=condition=Ready
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/sys/mounts/aquarium-db" -d @vault/enable-db-engine.json
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/aquarium-db/config/aquarium-db-cnx" -d @vault/aquarium-db-cnx.json
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/aquarium-db/roles/aquarium-db-role" -d @vault/aquarium-db-role.json
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/sys/auth/kubernetes" -d @vault/enable-k8s-engine.json
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/auth/kubernetes/config" -d @vault/vault-k8s-config.json
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/sys/policies/acl/aquarium-db-policy" -d @vault/aquarium-db-policy.json
  curl -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" "http://localhost:3140$2/v1/auth/kubernetes/role/aquarium-k8s-role" -d @vault/aquarium-k8s-role.json
fi

# Save Grafana username/password
username=`kubectl get secret loki-grafana -n monitoring -o jsonpath='{.data.admin-user}' | base64 -d`
password=`kubectl get secret loki-grafana -n monitoring -o jsonpath='{.data.admin-password}' | base64 -d`
echo "Username: $username" > grafana_creds.tmp
echo "Password: $password" >> grafana_creds.tmp

# Save Postgres usernames/passwords
su_username=`kubectl get secret pg-superuser -n pg -o jsonpath='{.data.username}' | base64 -d`
su_password=`kubectl get secret pg-superuser -n pg -o jsonpath='{.data.password}' | base64 -d`
app_username=`kubectl get secret app-user -n pg -o jsonpath='{.data.username}' | base64 -d`
app_password=`kubectl get secret app-user -n pg -o jsonpath='{.data.password}' | base64 -d`
echo "Superuser Username: $su_username" > pg_creds.tmp
echo "Supseruser Password: $su_password" >> pg_creds.tmp
echo "App Username: $app_username" >> pg_creds.tmp
echo "App Password: $app_password" >> pg_creds.tmp

echo -e '\nCluster created'
echo -e '\nYou can now access your services on:'
echo -e "\tapplication: http://localhost:3010$2"
echo -e "\tgrafana:     http://localhost:3130$2"
echo -e "\tpostgres:    http://localhost:3132$2"
echo -e "\tvault:       http://localhost:3140$2"
echo -e '\nYou can find credentials in: (not saved in git)'
echo -e '\tkind/vault_creds.tmp'
echo -e '\tkind/grafana_creds.tmp'
echo -e '\tkind/pg_creds.tmp'
echo -e '\nHappy development...\n'

echo -e '\nFinished ' `date`