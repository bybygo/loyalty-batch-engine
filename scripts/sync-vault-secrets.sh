#!/bin/bash

VAULT_ADDR=${VAULT_ADDR:-"http://127.0.0.1:8200"}
NAMESPACE=${NAMESPACE:-"loyalty-system"}

# Sync secrets from Vault to Kubernetes
sync_secrets() {
    # Get secrets from Vault
    SECRETS=$(vault kv get -format=json secret/loyalty-app/database)

    # Create Kubernetes secret
    kubectl create secret generic postgres-secrets \
        --from-literal=POSTGRES_USER=$(echo $SECRETS | jq -r '.data.data.DB_USER') \
        --from-literal=POSTGRES_PASSWORD=$(echo $SECRETS | jq -r '.data.data.DB_PASSWORD') \
        --from-literal=POSTGRES_DB=$(echo $SECRETS | jq -r '.data.data.DB_NAME') \
        --dry-run=client -o yaml | kubectl apply -f -
}

sync_secrets