$statefulset_name = $args[0]
$replicas = $args[1]

# Scale StatefulSet
kubectl scale statefulset $statefulset_name --replicas=$replicas

# Delay
while ($true) {
  $ready_replicas = (kubectl get statefulset $statefulset_name -o jsonpath='{.status.readyReplicas}')
  if ($ready_replicas -eq $replicas) {
    break
  }
  Start-Sleep -Seconds 5
}

# Restart StatefulSet
kubectl rollout restart statefulset $statefulset_name
