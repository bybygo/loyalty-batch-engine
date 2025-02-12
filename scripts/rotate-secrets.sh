#!/bin/bash

source scripts/generate-secrets.sh

# Function to update Vault secrets
update_vault_secrets() {
    VAULT_ADDR="http://127.0.0.1:8200"

    # Update secrets in Vault
    vault write secret/loyalty-batch-engine/database \
        credentials="$(cat .env.secrets | base64)"

    # Update Kubernetes secrets
    kubectl apply -f kubernetes/secrets/postgres-secrets.yaml

    # Restart pods to pick up new secrets
    kubectl rollout restart deployment/loyalty-batch-engine
}

# Generate new secrets
./scripts/generate-secrets.sh

# Update secrets in Vault and Kubernetes
update_vault_secrets

echo "Secrets rotated successfully!"