@echo off

set statefulset_name=%1
set replicas=%2

rem Scale StatefulSet
kubectl scale statefulset %statefulset_name% --replicas=%replicas%

rem Delay
while true; do
  replicas=$(kubectl get statefulset %statefulset_name% -o jsonpath='{.status.readyReplicas}')
  if [ "$replicas" == "%replicas%" ]; then
    break
  fi
  sleep 5
done

rem Restart StatefulSet
kubectl rollout restart statefulset %statefulset_name%