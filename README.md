1. Create cluster and use the local registry image in Kubernetes

```shell
kind create cluster --config kind-config.yaml
```

2. Build Docker Image and Push to Local Registry
    1. Ensure registry is on
   ```shell
   make registry-start
   ```
    2. Build Image
   ```shell
   make docker-build
   ```
    3. Push to Registry
   ```shell
   make docker-push
   ```

3. Connect the registry to Kind's network:

```shell
podman network connect kind registry
```

4. Deploy to Kubernetes

```shell
kubectl apply -f kubernetes/service.yaml

kubectl apply -f kubernetes/deployment.yaml
```

5. port-forward to the Service

```shell
kubectl port-forward svc/loyalty-batch-engine-svc 9874:9874
```