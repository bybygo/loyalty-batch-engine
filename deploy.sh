#!/bin/bash

# Create namespace if it doesn't exist
kubectl create namespace loyalty-system --dry-run=client -o yaml | kubectl apply -f -

# Switch to the namespace
kubectl config set-context --current --namespace=loyalty-system

# Apply configurations in order
kubectl apply -f kubernetes/config/
kubectl apply -f kubernetes/secrets/
kubectl apply -f kubernetes/storage/
kubectl apply -f kubernetes/services/
kubectl apply -f kubernetes/deployments/

# Wait for deployments to be ready
kubectl wait --for=condition=available --timeout=300s deployment/postgres
kubectl wait --for=condition=available --timeout=300s deployment/loyalty-batch-engine

echo "Deployment completed successfully!"