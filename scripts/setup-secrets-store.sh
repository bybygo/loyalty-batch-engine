#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Add the Helm repository
echo -e "${BLUE}Adding Secrets Store CSI Driver Helm repository...${NC}"
helm repo add secrets-store-csi-driver https://kubernetes-sigs.github.io/secrets-store-csi-driver/charts
helm repo update

# Install the secrets-store-csi-driver
echo -e "${BLUE}Installing Secrets Store CSI Driver...${NC}"
helm install csi-secrets-store secrets-store-csi-driver/secrets-store-csi-driver \
  --namespace kube-system \
  --create-namespace \
  --set syncSecret.enabled=true

# Wait for the driver to be ready
echo -e "${BLUE}Waiting for CSI driver to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=secrets-store-csi-driver -n kube-system

# Install Vault CSI Provider
echo -e "${BLUE}Installing Vault CSI Provider...${NC}"
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

helm install vault-csi-provider hashicorp/vault-csi-provider \
  --namespace kube-system \
  --set "vault.address=http://127.0.0.1:8200"

# Wait for the Vault provider to be ready
echo -e "${BLUE}Waiting for Vault CSI provider to be ready...${NC}"
kubectl wait --for=condition=ready pod -l app=vault-csi-provider -n kube-system

echo -e "${GREEN}Secret Store CSI Driver and Vault provider installed successfully!${NC}"