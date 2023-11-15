@echo off
kubectl apply -f statefulset_role.yaml -n default
kubectl apply -f statefulset_rolebinding.yaml -n default
