#!/bin/bash
# scripts/generate-secrets.sh

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# Function to generate a secure random password
generate_password() {
    openssl rand -base64 24 | tr -d '/+=' | cut -c1-16
}

# Function to encode in base64
encode_base64() {
    echo -n "$1" | base64
}

# Generate credentials
DB_USER="loyalty_user"
DB_PASSWORD=$(generate_password)
DB_NAME="loyalty_db"
DB_ROOT_PASSWORD=$(generate_password)

# Create secrets directory if it doesn't exist
mkdir -p kubernetes/secrets

# \${ENVIRONMENT:-production}

# Generate the secrets file
cat > kubernetes/secrets/postgres-secrets.yaml << EOF
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secrets
  namespace: loyalty-system
  labels:
    app: loyalty-batch-engine
    environment: production
type: Opaque
data:
  # Database credentials
  POSTGRES_USER: $(encode_base64 "$DB_USER")
  POSTGRES_PASSWORD: $(encode_base64 "$DB_PASSWORD")
  POSTGRES_DB: $(encode_base64 "$DB_NAME")
  POSTGRES_ROOT_PASSWORD: $(encode_base64 "$DB_ROOT_PASSWORD")

  # Connection URL (encoded)
  DATABASE_URL: $(encode_base64 "jdbc:postgresql://postgres:5432/${DB_NAME}")

  # Additional security configurations
  POSTGRES_INITDB_ARGS: $(encode_base64 "--auth-host=scram-sha-256")
EOF

# Create a backup of the credentials (for admin use)
cat > .env.secrets << EOF
DB_USER=$DB_USER
DB_PASSWORD=$DB_PASSWORD
DB_NAME=$DB_NAME
DB_ROOT_PASSWORD=$DB_ROOT_PASSWORD
EOF

# Set proper permissions
chmod 600 .env.secrets

# Print success message
echo -e "${GREEN}Secrets generated successfully!${NC}"
echo -e "${GREEN}Credentials backup saved to .env.secrets${NC}"
echo -e "${RED}IMPORTANT: Keep .env.secrets secure and DO NOT commit it to version control!${NC}"