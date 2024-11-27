# Java MeetUp Demo

This code was created for the Sydney Java MeetUp on 27 Nov 2024.

The aim is to demonstrate how to use dynamic, rotating passwords
using Spring Boot, Hashicorp's Vault and Postgres, all running in
a local Kubernetes cluster based on Kind.

The application is a fun back-end microservice called `aquarium`
that allows you to manage fish and fish tanks. Details of the 
application appear below. 

This work is based on a set of Medium articles I have written to
help developers work with Kubernetes and other technologies.

You can find out more in [these medium articles](https://medium.com/@martin.hodges).

## Process

To run this demonstration you will need to do:
1. Environment Set Up
2. Create a Spring Boot Ready Kubernetes Cluster
3. Create Aquarium Spring Boot Application
4. Deploy the Application

## Environment Set Up

***Note that this code was developed to be run on a Mac OS machine***
***I used a MacBookPro, M1 Max, 32GB running Sonoma 14.6.1***

***Note that a straight M1 chip will not run Kind.***

We will need to install:
- Docker
- Kind
- Kubectl
- Helm

### Install Docker

If you are running on a Mac, you will need to install Docker for
Desktop.

You can find the installation instructions here:
[https://docs.docker.com/desktop/setup/install/mac-install/](https://docs.docker.com/desktop/setup/install/mac-install/)

### Install Kind

To run this example, we need to install KinD, which is Kubernetes
in Docker. This is a lightweight, fully featured Kubernetes cluster
for local development and supports multiple nodes on a development
machine.

You can find `kind` installation instructions here:
[https://kind.sigs.k8s.io/docs/user/quick-start](https://kind.sigs.k8s.io/docs/user/quick-start)

### Install Kubectl

You will also need to install `kubectl` to access your cluster:

[https://kubernetes.io/docs/tasks/tools/](https://kubernetes.io/docs/tasks/tools/)

### Install Helm

Helm is like a package manager for manging the deployment of
applications on your cluster.

[https://helm.sh/docs/intro/install/](https://helm.sh/docs/intro/install/)

## Create a Spring Boot Ready Kubernetes Cluster

***Do NOT use this for creating production clusters***

This script will create a 4 node cluster that can be used to run
your Spring Boot application. It includes:
- Vault for secrets management
- Grafana for log management
- Postgres as the database

***Note that this can take up to 15 minutes to complete. It only
needs to be done once. Each step does not continue until that step
has completed, which can take up to 5 minutes***

Run the following command to create your cluster
```./create-local-cluster.sh <cluster-name> <port offset>```

The `cluster-name` should not contain spaces, eg: `jmu-1`. The port offset ensures it does not clash with any other cluster you have created and should be a single digit from `1`-`9`.

The cluster name is also used to identify the configuration file 
to use, eg:
```kind-config-<cluster-name>```

During the MeetUp, I set up the environment using:
```./create-local-cluster.sh jmu-2 2```

The script should complete successfully and I have included an
annotated copy of my script output for reference
(`kind/create-cluster-log.txt`).

At the end of the run, it should announce the following 
connections:

```
Cluster created

You can now access your services on:
	application: http://localhost:30102
	grafana:     http://localhost:31302
	postgres:    http://localhost:31322
	vault:       http://localhost:31402

You can find credentials in: (not saved in git)
	vault_creds.tmp
	grafana_creds.tmp

Happy development...
```

The port number will be based on the port number you chose earlier.

### What if things don't go to plan

The cluster creation script is built to skip anything already
deployed. If you want to deploy something again, you can delete
the relevant namespace and then rerun the script.

For example, if you want to reinstall `vault`:

```
kubectl get namesspace
kubectl delete namespace vault
./create-local-cluster.sh <name> <offset>
```

If required, you can simply delete the entire cluster and start 
again:
```
kind delete cluster --name <cluster-name>
```

Or even just create a new cluster with a different name!

## Create Aquarium Spring Boot Application

This is a simple application that uses REST APIs to manage
two types of resource:
- Fishes
- Fish Tanks

The features include the ability to:
* Manage a set of fish tanks (`GET`, `POST`, `PUT`, `DELETE`)
* Create and manage fishes (`GET`, `POST`, `PUT`, `DELETE`)
* Add and remove fish to/from a tank (`PUT`, `DELETE`)

The REST API end points are at:
```
    /api/v1/fish-tanks
    /api/v1/fish-tanks/{id}
    /api/v1/fish-tanks/{id}/fishes/{id}
    /api/v1/fish-types
    /api/v1/fish-types/{id}
    /api/v1/fishes
    /api/v1/fishes/{id}
```

### Spring Profiles

The application has 4 profiles:
- standalone
- connected
- k8s-debug
- local-cluster

#### standalone
This version uses an in memory H2 database to allow standalone
development.

#### conected
This version connects to the postgres database running in the
kubernetes cluster. With a change to the properties, it can
connect to any instance with a database called `aquarium_db`
and a schema called `aquarium_schema`.

The username and password in this profile are static and applied
via two environment variables:
- `STATIC_DB_USERNAME`
- `STATIC_DB_PASSWORD`

#### k8s-debug
This version runs within the cluster but still uses static
credentials from Vault. These are stored as a Vault K2
(version 2) secret at the path of 
`aquarium-api/static-db-credentials`.

You will find that the `create-local-cluster.sh` script
creates this set of static database credentials for you.

You should have a Kubernetes secret called 
`static-db-credentials` when you run:
```
kubectl get secrets
```

When you run your Spring Boot application (as described
below), it links the JVM to the debug port defined in
your kind config. This allows your IDE to be connected 
to the application so you can debug it remotely.

To use this profile, it is necessary to build the docker
image using the `Dockerfile.k8s-debug` file.

#### local-cluster
This version is close to the production environment and
uses dynamic credentials from Vault.

***This is the profile you should use for this demonstration.***

To use this profile, it is necessary to build the docker
image using the `Dockerfile.local-cluster`.

## Deploy the Application

The following section describes the steps to create an
executable JAR, build it into a docker image and load
it into your Kind cluster.

If you want to skip these steps, you can go straight
to the scripts I have provided (don't forget to use
your own cluster name):
```
kind/scripts/deploy-k8s-debug-to-kind.sh <cluster-name>
kind/scripts/deploy-local-cluster-to-kind.sh <cluster-name>
```

### Building the JAR File
To build the dockerfile, you first need an executable
JAR. From the command line and then load it into your
Kind cluster:
```
./gradlew bootJar
```

This will create an executable JAR file:
```
build/libs/aquarium-api-0.0.1-SNAPSHOT.jar
```

Once you have this, it can now be turned into a Docker
image. Use the following, remembering to use the 
appropriate profile:
```
docker build -t aquarium-api-<profile>:01 -f Dockerfile.<profile> .
```
This is using the local cluster profile. Use the other
Dockerfile for the `k8s-debug` version.

### Loading the Docker image

Once a Docker image has been created, it can be loaded
into the cluster without having to upload it to an
image repository first. This makes for a faster deployment
time. Remember to use the name of your cluster and the profile.

```
kind load docker-image aquarium-api-<profile>:01 --name <cluster-name>
```

### Deploying to the Cluster

Now you have your docker image loaded into all the nodes
in your cluster, you can now deploy it to the cluster
in order to run it.

To deploy the application with the profile you want,
you can use the following:
```
kubectl apply -f kind/k8s/k8s-debug-deployment.yml
```
***or***
```
kubectl apply -f kind/k8s/local-cluster-deployment.yml
```
If you try to run both, you will have problems with port
contention. To reload the same or different deployment,
first delete the previous deployment with:
```
kubectl delete -f kind/k8s/k8s-debug-deployment.yml
kubectl delete -f kind/k8s/local-cluster-deployment.yml
```
The script automatically does this for you.

## Technical details

You do not need to know this to use the cluster but for those who
are interested, here is a quick explanation of what is created.

Grafana, Loki and Prometheus are installed on each Kubernetes node
and collect and process the logs from all pods on that node. This
includes the Kubernetes system pods itself. You can find an admin
access password in `grafana_creds.tmp` to allow you to log in.

Vault is installed and unsealed. You can find a superuser access 
token in `vault_creds.tmp` to allow you to log in. You will also
find the single Shamir key to unseal the vault should you need to.

Postgres 15 is installed using the `cnpg/cloudnative-pg` Kubernetes
postgres operator. It is exposed to the host so that you can use 
your favourite database client to access it.  There are two 
users, a superuser/admin user and an application user. You can find
their credentials in the `pg_creds.tmp` file.

The script also creates a database and a schema:
```
aquarium_db
aquarium_schema
```
Both users are given full access to these.

If you wish to use static credentials, you can do via `vault`.
These are made available via the `external-secrets-db.yml` file.
It assumes that there is a KV secret that holds the username
and password in the `db` secret.

### Database connections

Whilst it is possible to connect to the database using static
credentials, the aim of the demonstration is to use dynamic 
credentials.

To do this, `vault` is configured with a database connection. This 
goes to the read/write instance via the postgres Kubernetes 
service. The connection uses the superuser credentials to provide
sufficient access to allow `vault` to create users and passwords.

Using the connection, `vault` is given the information it requires
to create users and passwords using a vault database role (not to
be confused with a postgres database role).

A `vault` policy is created to allow the application to read the
latest database credentials. The application is given permission
to do this through a kubernetes role attached to its service 
account.

It is assumed that the application is run in the `default` 
namespace.

### Storage

All storage (eg: Loki, Grafana, Vault and postgres) is provided by 
way of the `standard` storage class and is managed through the 
Kind cluster.

***If you delete the cluster, you will delete all data stored
in the `standard` storage class.***

### TLS

TLS has not been enabled for any of the applications. This is to
simplify the configuration. For production, TLS must be enabled
and this can be done using a service mesh such as ISTO, which
can be deployed to a Kind cluster.

## Creating a Spring Boot application

Now we have our cluster, we can now build our Spring Boot 
application and install it into the cluster.

The application is used to create a bootable JAR, which is
then added to a Docker image to allow it to be run within
your Kubernetes cluster.

The deployment file for the application is annotated with
Vault settings to tell Vault to:
1. Inject an Init containers to obtain a password (`vault-agent-init`)
2. Inject a sidecar to obtain ongoing passwords (`vault-agent`)
3. Save the dynamic credentials to a shared volume with your application

On start up, the `vault-agent-init` container will authenticate
itself against Kubernetes using its ServiceAccount via Vault.
It will then connect to Vault and obtain the credentials, which
it will save in a shared volume.

The init container will then terminate and this triggers the
main application along with a `vault-agent` sidecar container.

Periodically, the Vault Agent will obtian the latest credentials
from Vault. It will then pass them to the application, which will
then use them for accessing the database

