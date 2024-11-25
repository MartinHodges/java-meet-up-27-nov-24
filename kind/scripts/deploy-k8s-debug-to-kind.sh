#!/bin/bash

if [ "$1" = "" ] || [ "$1" = "--help" ] || [ "$1" = "-h" ] || [ "$1" = "-help" ]; then
    echo 'Usage kind/scripts/deploy-k8s-debug-to-kind.sh <cluster-name> [<option>]'
    echo -e '\nOptions:'
    echo -e '\tredeploy\tredeploys the application in the cluster'
    exit 1
fi

case "$2" in
  "")
    echo -e '\nRebuilding and deploying local-cluster to kind cluster'
    gradle bootJar
    docker build -t aquarium-api-k8s-debug:01 -f Dockerfile.local-cluster .
    kind load docker-image aquarium-api-k8s-debug:01 --name $1
    kubectl delete -f kind/k8s/local-cluster-deployment.yml --ignore-not-found
    kubectl delete -f kind/k8s/k8s-debug-deployment.yml --ignore-not-found
    kubectl apply -f kind/k8s/k8s-debug-deployment.yml
    echo -e "\nDeployment complete"
  ;;

  "reload" | "redeploy")
    echo -e '\nRedeploying local-cluster to kind cluster'
    kubectl delete -f kind/k8s/local-cluster-deployment.yml --ignore-not-found
    kubectl delete -f kind/k8s/k8s-debug-deployment.yml --ignore-not-found
    kubectl apply -f kind/k8s/k8s-debug-deployment.yml
    echo -e "\nDeployment complete"
  ;;

  "-h" | "-help" | "--help")
    echo 'Usage kind/scripts/deploy-k8s-debug-to-kind.sh <cluster-name> [<option>]'
    echo -e '\nOptions:'
    echo -e '\tredeploy\tredeploys the application in the cluster'
    exit 1
  ;;

  *)
    echo -e "Unknown option $2"
    echo -e 'Usage kind/scripts/deploy-k8s-debug-to-kind.sh <cluster-name>'
    echo 'For help kind/scripts/deploy-k8s-debug-to-kind.sh --help'
    exit 1
esac