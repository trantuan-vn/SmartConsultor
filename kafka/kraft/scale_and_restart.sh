statefulset_name=$1
replicas=$2
delay=$3

# Scale StatefulSet
kubectl scale statefulset "$statefulset_name" --replicas="$replicas"

# Delay
while true; do
  ready_replicas=$(kubectl get statefulset "$statefulset_name" -o jsonpath='{.status.readyReplicas}')
  if [ "$ready_replicas" == "$replicas" ]; then
    break
  fi
  sleep 5
done

# Restart StatefulSet
kubectl rollout restart statefulset "$statefulset_name"