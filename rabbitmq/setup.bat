kubectl apply -f https://github.com/rabbitmq/cluster-operator/releases/latest/download/cluster-operator.yml
kubectl apply -f rabbitmq-cluster-operator.yaml
kubectl port-forward "service/rabbitmq" 15672
rem username="$(kubectl get secret rabbitmq-default-user -o jsonpath='{.data.username}' | base64 --decode)"
rem password="$(kubectl get secret rabbitmq-default-user -o jsonpath='{.data.password}' | base64 --decode)"
rem service="$(kubectl get service rabbitmq -o jsonpath='{.spec.clusterIP}')"
rem kubectl run perf-test --image=pivotalrabbitmq/perf-test -- --uri amqp://$username:$password@$service