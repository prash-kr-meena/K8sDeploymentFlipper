
# Test Simulation 1

### Setup

1. Already had 2 matching deployment present on the cluster in same namespace as of controller "epic"
2. After that deployed the controller started running - but would not do anything, as no Flipper Resource has been added
3. Add Flipper resource -> Note : Finalizer being added to the resource (CleanUp Method will be Called)

```
kubectl create namespace epic
kubectl config set-context --current --namespace=epic
kubectl apply -f target/classes/META-INF/fabric8/flippers.com.meena-v1.yml


kubectl apply -f k8s/application/react-nginx-frontend-application-v1.yaml -f k8s/application/react-nginx-frontend-application-v2.yaml
kubectl apply -f k8s/application/k8s-deployment-flipper-contoller-application.yaml
kubectl apply -f k8s/flipper/flipper-1m.yaml
```

### Result
curl --location --request POST 'http://localhost:30000/rollout-restart/epic/deployment-flipper'


1. It would create a cronjob to run every 1 minute
2. we would see the resource has been pickedup as now it has the status field and the generation value
2. Which will hit the controllers api endpoint to restart the deployment afte a minute

```
kubectl get deployments 
kubectl rollout history deployment/react-nginx-frontend-v2
```

# Test Simulation 2

### Setup

1. Already had 2 matching deployment present on the cluster in same namespace as of controller "epic"
2. After that deployed the controller started running - but would not do anything, as no Flipper Resource has been added
3. Add Flipper resource -> Note : Finalizer being added to the resource (CleanUp Method will be Called)

```
kubectl apply -f k8s/application/react-nginx-frontend-application-v1.yaml -f k8s/application/react-nginx-frontend-application-v2.yaml
kubectl apply -f k8s/application/k8s-deployment-flipper-contoller-application.yaml
kubectl apply -f k8s/flipper/flipper-1m.yaml
```

### Result

1. It would create a cronjob to run every 1 minute
2. Which will hit the controllers api endpoint to restart the deployment afte a minute



# Test Simulation 3 - Full Cleanup

### Setup

1. Already had 2 matching deployment present on the cluster in same namespace as of controller "epic"
2. After that deployed the controller started running - but would not do anything, as no Flipper Resource has been added
3. Add Flipper resource -> Note : Finalizer being added to the resource (CleanUp Method will be Called)
4. Deleting the whole application deployment
```
kubectl apply -f k8s/application/react-nginx-frontend-application-v1.yaml -f k8s/application/react-nginx-frontend-application-v2.yaml
kubectl apply -f k8s/application/k8s-deployment-flipper-contoller-application.yaml
kubectl apply -f k8s/flipper/flipper-1m.yaml

kubectl delete -f k8s/application/k8s-deployment-flipper-contoller-application.yaml
# delete whole deploying controller application setup
```

### Result

1. It would create a cronjob to run every 1 minute
2. Which will hit the controllers api endpoint to restart the deployment afte a minute



### Cleanup

One by One

```
kubectl delete -f k8s/flipper/flipper-1m.yaml
kubectl delete -f k8s/application/react-nginx-frontend-application-v1.yaml -f k8s/application/react-nginx-frontend-application-v2.yaml
kubectl delete -f k8s/application/k8s-deployment-flipper-contoller-application.yaml  # deploying controller
```
- should delete all the cronjobs

All at Ones

delete whole deploying controller application setup
```
kubectl delete -f k8s/application/k8s-deployment-flipper-contoller-application.yaml
```

- should delete all the cronjobs

