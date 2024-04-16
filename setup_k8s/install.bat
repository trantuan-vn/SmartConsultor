#dev environment: install minikube
#cd ..\setup_k8s

minikube addons enable metrics-server
minikube addons enable istio-provisioner
minikube addons enable istio

echo Waiting for CRDS to be installed...
kubectl create -f .\olm\crds.yaml 
kubectl create -f https://raw.githubusercontent.com/operator-framework/operator-lifecycle-manager/master/deploy/upstream/quickstart/crds.yaml
echo Waiting for OLM to be installed...
kubectl create -f .\olm\olm.yaml 
kubectl create -f https://raw.githubusercontent.com/operator-framework/operator-lifecycle-manager/master/deploy/upstream/quickstart/olm.yaml

echo Waiting for CNPG to be installed...
kubectl create -f https://operatorhub.io/install/cloudnative-pg.yaml

echo Waiting for POSTGRES to be installed...
kubectl apply -f .\postgres\postgresql.yaml 
echo If error volume ...
minikube ssh --node minikube-m02
cd /tmp/hostpath-provisioner/default
chmod 777 postgresql-1  postgresql-1-wal postgresql-2 postgresql-2-wal
chmod 777 postgresql-2 postgresql-2-wal


echo Waiting for GRANT POSTGRES to be installed...
$content= Get-Content -Path .\postgres\grant_app.sql -Raw
kubectl exec -it postgresql-1 -- sh -c "echo '$content' | psql -U postgres -d postgres"
kubectl exec -it postgresql-1 -- sh
psql
ALTER USER app WITH PASSWORD 'secret99';

echo Waiting for KEYCLOAK to be installed...
cd keycloak
docker build . -t keycloak
docker tag keycloak:latest tuantahp/keycloak:latest
docker login
docker push tuantahp/keycloak:latest
kubectl apply -f keycloak.yaml 
#helm repo add bitnami https://charts.bitnami.com/bitnami

echo Waiting for ignite to be installed...
#https://github.com/helm/charts/tree/master/stable/ignite, https://artifacthub.io/ install helm 
#helm repo add cloudnativeapp https://cloudnativeapp.github.io/charts/curated/
#helm fetch cloudnativeapp/ignite --version 1.0.0 
helm install  ignite .\ignite --set persistence.persistenceVolume.size=2Gi --set persistence.walVolume.size=2Gi  
#helm uninstall ignite
kubectl exec -n default ignite-0 -- /opt/ignite/apache-ignite/bin/control.sh --activate
kubectl exec -n default ignite-1 -- /opt/ignite/apache-ignite/bin/control.sh --activate
kubectl exec -n default ignite-0 -- /opt/ignite/apache-ignite/bin/control.sh --state

echo Waiting for kraft-kafka to be installed...
docker build -t kraft-kafka .\kraft-kafka\docker
docker tag kraft-kafka:latest tuantahp/kraft-kafka:latest
docker login
docker push tuantahp/kraft-kafka:latest
kubectl apply -f .\kraft-kafka\kubernetes\kafka.yml

echo Waiting for nginx flutter to be installed...
cd C:\Users\tuant\SmartConsultor\flutter\smartconsultor-1.0.0\smartconsultor\build
#copy 2 file default.conf và Dockerfile từ thư mục nginx-flutter vào thư mục build
docker build . -t nginx-flutter
docker login
docker tag nginx-flutter tuantahp/nginx-flutter:latest
docker push tuantahp/nginx-flutter:latest

echo Waiting for mongodb to be installed...
#https://www.mongodb.com/docs/kubernetes-operator/stable/tutorial/install-k8s-operator/#std-label-install-k8s-operator
#https://www.mongodb.com/docs/kubernetes-operator/stable/tutorial/mdb-resources-arch/
helm repo add mongodb https://mongodb.github.io/helm-charts
helm install enterprise-operator mongodb/enterprise-operator