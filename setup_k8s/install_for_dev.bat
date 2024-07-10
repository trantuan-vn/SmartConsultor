#dev environment: minikube v1.32.0, vs studio code 1.88.1, flutter 3.16.5, 
#cd ..\setup_k8s
minikube config set memory 16384
minikube config set disk-size 40g
minikube config set cpus 6

#minikube config view
minikube start
minikube node add 

minikube addons enable metrics-server
#minikube addons enable istio-provisioner
#minikube addons enable istio
cd C:\istioctl-1.17.2-win
istioctl install --set profile=demo
istioctl operator init
kubectl label namespace default istio-injection=enabled
#kubectl label namespace default istio-injection-


echo Waiting for security to be installed...
#kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.21/samples/addons/prometheus.yaml
#kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.21/samples/addons/grafana.yaml
#kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.21/samples/addons/jaeger.yaml
#kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.21/samples/addons/kiali.yaml
kubectl apply -f .\istio\skywalking.yaml 
kubectl apply -f .\istio\podToSkywalking.yaml 
cd C:\istioctl-1.17.2-win
istioctl manifest apply --set profile=demo --set meshConfig.enableEnvoyAccessLogService=true --set meshConfig.defaultConfig.envoyAccessLogService.address=skywalking-oap.istio-system.svc.cluster.local:11800
#kubectl apply -f .\istio\mTls.yaml 
#kubectl port-forward service/skywalking-ui 8080:8080 -n istio-system
#kubectl port-forward service/grafana 3000:3000 -n istio-system

echo Waiting for CRDS to be installed...
#kubectl create -f .\olm\crds.yaml 
kubectl create -f https://raw.githubusercontent.com/operator-framework/operator-lifecycle-manager/master/deploy/upstream/quickstart/crds.yaml
echo Waiting for OLM to be installed...
#kubectl create -f .\olm\olm.yaml 
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
#https://keycloak.ch/keycloak-tutorials/
cd keycloak
docker build . -t keycloak
docker tag keycloak:latest tuantahp/keycloak:latest
docker login
docker push tuantahp/keycloak:latest
kubectl apply -f keycloak/keycloak.yaml 
#helm repo add bitnami https://charts.bitnami.com/bitnami
helm install  keycloak .\keycloak

echo Waiting for ignite to be installed...
#https://github.com/helm/charts/tree/master/stable/ignite, https://artifacthub.io/ install helm 
#helm repo add cloudnativeapp https://cloudnativeapp.github.io/charts/curated/
#helm fetch cloudnativeapp/ignite --version 1.0.0 
helm install  ignite .\ignite --set persistence.persistenceVolume.size=50Gi --set persistence.walVolume.size=50Gi  
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
C:\Users\tuant\SmartConsultor\flutter\smartconsultor-1.0.0\smartconsultor
flutter build web
cd build
#copy 2 file default.conf và Dockerfile từ thư mục nginx-flutter vào thư mục build
docker build . -t nginx-flutter
docker login
docker tag nginx-flutter tuantahp/nginx-flutter:latest
docker push tuantahp/nginx-flutter:latest
cd C:\Users\tuant\SmartConsultor\setup_k8s\nginx-flutter
kubectl apply -f nginx.yaml
kubectl port-forward service/nginx-svc 8080:80

echo Waiting for istio to be installed...
#tao CA
openssl genrsa -out ca.key 4096
openssl req -new -key ca.key -out ca.csr -config ca.cnf
openssl x509 -req -days 365 -in ca.csr -signkey ca.key -out ca.crt
#tao cert từ CA cung cấp
openssl genrsa -out ia.key 4096
openssl req -new -key ia.key -out ia.csr -config ia.cnf
openssl x509 -req -in ia.csr -out ia.crt -extfile ia.cnf -extensions v3_req -days 365 -CA ca.crt -CAkey ca.key -CAcreateserial
#openssl x509 -in ia.crt -text -noout

kubectl create -n istio-system secret tls istio-ca --key ia.key --cert ia.crt
#kubectl delete -n istio-system secret istio-ca
kubectl apply -f .\gateway.yaml

echo Waiting for development service to be installed...
cd microservices
skaffold config set --global local-cluster true
minikube docker-env | Invoke-Expression
skaffold dev
kubectl port-forward service/gateway 8080:80

echo Waiting for citus to be installed...
#https://docs.citusdata.com/en/v12.1/use_cases/realtime_analytics.html#data-model
kubectl apply -f citus/secrets.yaml
kubectl apply -f citus/master.yaml #replicas=1
kubectl apply -f citus/workers.yaml #replicas=2

kubectl exec -it citus-master-0 -- bash
su postgres
#pulsar manager
psql -U smartconsultor -d smartconsultor -f postgresql-schema.sql 
psql
SELECT citus_set_coordinator_host('citus-master-0', 5432);
SELECT * from citus_add_node('citus-worker-0.citus-workers', 5432);
SELECT * from citus_add_node('citus-worker-1.citus-workers', 5432);
SELECT * FROM citus_get_active_worker_nodes();
ALTER SYSTEM SET citus.shard_replication_factor TO 2;
SELECT pg_reload_conf();


echo Waiting for citus to be installed...
git clone https://github.com/apache/pulsar-helm-chart
cd .\pulsar-helm-chart\
./scripts/pulsar/prepare_helm_release.sh -n pulsar -k pulsar-mini -c

#https://streamnative.io/blog/wechat-using-apache-pulsar-support-high-throughput-real-time-recommendation-service
# một số hiệu năng : 
#   bỏ proxy; 
#   sử dụng multidisk (Add useMultiVolumes option under .Values.bookkeeper.volumes.journal and .Values.bookkeeper.volumes.ledgers.);
#   volume; use non persistence topic; 
#   loadBalancerDistributeBundlesEvenlyEnabled = false
#   Tăng tỷ lệ truy cập bộ nhớ cache
#   Tạo bộ giảm tải COS bằng cách sử dụng lưu trữ theo tầng

echo Waiting for ELK to be installed...
minikube ssh
sudo sysctl -w vm.max_map_count=524288
cd C:\Users\tuant\SmartConsultor\setup_k8s\elk

#helm search repo elastic/elasticsearch
helm pull elastic/elasticsearch --version 8.5.1
#unzip va sửa replicas thành 1, minimumMasterNodes thành 1
helm install elasticsearch .\elasticsearch

#helm search repo elastic/kibana
helm pull elastic/kibana --version 8.5.1
#unzip
helm install kibana .\kibana

#helm search repo elastic/logstash
helm pull elastic/logstash --version 8.5.1
#unzip
helm install logstash .\logstash

echo Waiting for pulsar to be installed...
helm repo add apache https://pulsar.apache.org/charts
helm search repo pulsar
helm repo update
helm pull apache/pulsar --version 3.4.1
helm install  pulsar .\pulsar

 echo Waiting for microservices to be installed...
 C:\Users\tuant\SmartConsultor\flutter\smartconsultor-1.0.0\smartconsultor
flutter build web
#copy vào thư mục buid/web tới src/main/resources/webroot
#vào main.dart.js thay :
#https://www.gstatic.com/flutter-canvaskit/3f3e560236539b7e2702f5ac790b2a4691b32d49/ thay bằng canvaskit/
#https://fonts.gstatic.com/s/roboto/v20/KFOmCnqEu92Fr1Me5WZLCzYlKw.ttf thành assets/fonts/KFOmCnqEu92Fr1Me5WZLCzYlKw.ttf (trc do download file va copy vào thu muc assets/fonts)

cd C:\Users\tuant\SmartConsultor\microservices
skaffold dev
#skaffold delete
kubectl port-forward service/gateway 8080:80

echo Waiting for redis to be installed...
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm search repo bitnami/redis
helm pull bitnami/redis
helm install redis ./redis

echo Waiting for superset to be installed...
helm repo add superset https://apache.github.io/superset
helm repo update
helm search repo superset/superset
helm pull superset/superset --version 0.12.11 
helm install superset ./superset



