For installation instruction, please click here.

This is a demo of a development workflow using Camel-K for a simple integration application.

The application is composed of 3 microservices:
* Transaction Service
* Credit Service
* Debit Service

The Transaction services uses several Enterprise Integration Patterns (EIP) to interact with the two backend services (Credit and Debit) such as content-based router (CBR), parallel processor and aggregator.

From a high level, the application logical architecture looks like:
![application](images/app.png)

The network flow and security at the ingress point and between microservices is controlled by OpenShift Service Mesh (Istio).

This demo, sets up the following tools and process:

![process](images/processflow.png)

* **Step 1** A devfile to automatically creates the developer tooling for Camel-K including test/debug utilities and all required CLI to interact with OpenShift

* **Step A** CodeReady Workspaces(CRW) interprets the devfile and provision the secure and persistent environment for the developer

* **Step 2** The developer uses CRW to create and deploy the OpenShift Service Mesh resources required by the application

* **Step B** OpenShift Service Mesh (OSSM) based on Istio, automatically detects the desired configuration and applies it to the developer's sandbox.

* **Step 3** The developer uses CRW to create the Camel-K services and deploy them to his/her sandbox environment.

* **Step C** OSSM automatically detects that OIDC claims and authentication are required and RH-SSO generates or validates the auth token with different claims.

This concludes the demo for the developer sandbox. The developer sandbox is considered unmanaged, as it requires no gating, approval. The developer can change and deploy configuration and application to align to business needs.

The next step are installing and configuring a managed, secure and controlled environment to deploy the same application using automation provided by OpenShift Container Platform.

* **Step 4** When a change is commited and the pull request is merged to the main branch in Git, it triggers a sequence of autonation to deploy the application in an integrated development environment,

* **Step D** OpenShift GitOps, based on ArgoCD, is notified automatically of the change and apply the required configuration and application changes to the OpenShift cluster

* **Step 5** The Istio Configuration is applied and the namespace is added to the Istio managed namespace

* **Step B** OpenShift Service Mesh (OSSM) based on Istio, automatically detects the desired configuration and applies it to the developer's sandbox.

* **Step 6** The required CI steps, tasks and pipelines are synced from GIT to the OpenShift Pipelines by OpenShift GitOps.

* **Step E** OpenShift Pipelines, based on Tekton, executes the pipelines to build and deployed the microservices to the managed namespace

* **Step F** Camel-K operator detects the new integration routes and assemble the application

* **Step 7** The application with all the microservices are running in the managed environment.

* **Step C** OSSM automatically detects that OIDC claims and authentication are required and RH-SSO generates or validates the auth token with different claims.










# camel-demo
Pré-requis
l'Operator Camel-K doit être déployé dans le namespace ou cluster utilisé.

L'outil CLI Kamel doit être installé.

Étape 1
Création des bases de données:

```
oc apply -k manifests/database/creditdb
oc apply -k manifests/database/debitdb
```

Création des secrets pour les services:
```
oc apply -f apps/creditservice/db-secret.yaml
oc apply -f apps/transactionservice/db-secret.yaml
```


Étape 2
Déployer les services:

```
kamel run --profile=openshift --open-api=apps/creditservice/creditservice-openapi.yaml --config secret:my-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false apps/creditservice/creditservice.java
```

```
kamel run --profile=openshift --open-api=apps/debitservice/debitservice-openapi.yaml --config secret:my-debit-datasource --build-property quarkus.datasource.camel.db-kind=postgresql  -d mvn:io.quarkus:quarkus-jdbc-postgresql -t knative.enabled=false apps/debitservice/debitservice.java
```

```
kamel run --profile=openshift --open-api=apps/transactionservice/transactionservice-openapi.yaml -t knative.enabled=false -t route.enabled=true apps/transactionservice/transactionservice.java

```