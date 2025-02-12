#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Environment variables
ENVIRONMENT=${ENVIRONMENT:-production}
VERSION=${VERSION:-0.0.1}
NAMESPACE="loyalty-system"
VAULT_ADDR=${VAULT_ADDR:-http://127.0.0.1:8200}

export VAULT_ADDR='http://127.0.0.1:8200'

# Error handling
set -e
trap 'handle_error $? $LINENO' ERR

handle_error() {
    echo -e "${RED}Error: Script failed at line $2 with exit code $1${NC}"
    cleanup
    exit 1
}

cleanup() {
    echo -e "${BLUE}Performing cleanup...${NC}"
    # Add cleanup logic here
}

# Function to check prerequisites
check_prerequisites() {
    echo -e "${BLUE}Checking prerequisites...${NC}"

    local tools=("podman" "kubectl" "vault" "helm")
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            echo -e "${RED}Error: $tool is not installed${NC}"
            exit 1
        fi
    done

    # Check Kubernetes context
    if ! kubectl config current-context &> /dev/null; then
        echo -e "${RED}Error: Kubernetes context is not set${NC}"
        echo -e "${BLUE}Available contexts:${NC}"
        kubectl config get-contexts
        echo -e "${BLUE}Please set a context using: kubectl config use-context <context-name>${NC}"
        echo -e "${BLUE}Or configure your kubeconfig file${NC}"
        exit 1
    fi

    # Verify cluster access
    if ! kubectl cluster-info &> /dev/null; then
        echo -e "${RED}Error: Cannot connect to Kubernetes cluster${NC}"
        exit 1
    fi

    # Verify Vault access
    vault status || {
        echo -e "${RED}Error: Cannot connect to Vault${NC}"
        exit 1
    }
}

setup_csi_driver() {
    echo -e "${BLUE}Setting up Secrets Store CSI Driver...${NC}"
    ./scripts/setup-secrets-store.sh
}

# Function to generate and store secrets
setup_secrets() {
    echo -e "${BLUE}Setting up secrets...${NC}"

    # Generate secrets
    ./scripts/generate-secrets.sh

    # Store secrets in Vault
    # vault kv put secret/loyalty-batch-engine/database @.env.secrets
    vault kv put secret/loyalty-batch-engine/database $(cat .env.secrets | xargs)

    # Create Kubernetes secrets from Vault
    ./scripts/sync-vault-secrets.sh
}

# Function to build application
build_application() {
    echo -e "${BLUE}Building application...${NC}"

    # Run tests
    ./gradlew test

    # Build application
    ./gradlew clean build -x test
}

# Function to build and push container
build_container() {
    echo -e "${BLUE}Building container image...${NC}"

    # Build image
    podman build -t localhost/loyalty-batch-engine:${VERSION} .

    # Scan image for vulnerabilities
    # trivy image localhost/loyalty-batch-engine:${VERSION}

    # Sign image
    # podman sign localhost/loyalty-batch-engine:${VERSION}
}

# Function to deploy to Kubernetes
deploy_kubernetes() {
    echo -e "${BLUE}Deploying to Kubernetes...${NC}"

    # Create namespace if it doesn't exist
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

    # Apply security labels to namespace
    kubectl label namespace ${NAMESPACE} \
      pod-security.kubernetes.io/enforce=baseline \
      pod-security.kubernetes.io/audit=restricted \
      pod-security.kubernetes.io/warn=restricted \
      --overwrite

    # Switch to namespace
    kubectl config set-context --current --namespace=${NAMESPACE}

    # Deploy in order
    local components=("security" "config" "secrets" "storage" "services" "deployments")
    for component in "${components[@]}"; do
        echo -e "${BLUE}Deploying ${component}...${NC}"
        kubectl apply -f kubernetes/${component}/
    done

    # Verify RBAC
    echo -e "${BLUE}Verifying RBAC...${NC}"
    kubectl auth can-i get secrets --as=system:serviceaccount:${NAMESPACE}:loyalty-batch-engine -n ${NAMESPACE}

    # Wait for deployments
    wait_for_deployment "loyalty-batch-engine" "${NAMESPACE}" "300s" || {
        echo "Failed waiting for loyalty-batch-engine deployment"
        exit 1
    }

    wait_for_deployment "postgres" "${NAMESPACE}" "300s" || {
        echo "Failed waiting for postgres deployment"
        exit 1
    }
}

wait_for_deployment() {
    local deployment=$1
    local namespace=$2
    local timeout=${3:-300s}

    echo "Waiting for deployment/${deployment} to be available..."

    if kubectl wait --for=condition=available --timeout=${timeout} deployment/${deployment} -n ${namespace}; then
        echo "✅ Deployment ${deployment} is available"
    else
        echo "❌ Timeout waiting for deployment/${deployment}"
        echo "Checking deployment status..."
        kubectl describe deployment/${deployment} -n ${namespace}
        echo "Checking pod status..."
        kubectl get pods -n ${namespace} -l app=${deployment}
        return 1
    fi
}

verify_security() {
    echo -e "${BLUE}Verifying security configuration...${NC}"

    # Check Pod Security Standards
    kubectl get ns ${NAMESPACE} -o yaml | grep pod-security.kubernetes.io

    # Check NetworkPolicy
    kubectl get networkpolicy -n ${NAMESPACE}

    # Check ServiceAccount
    kubectl get serviceaccount loyalty-batch-engine -n ${NAMESPACE}

    # Check RBAC
    kubectl get role,rolebinding -n ${NAMESPACE}
}

# Function to verify deployment
verify_deployment() {
    echo -e "${BLUE}Verifying deployment...${NC}"

    # Check pod status
    kubectl get pods

    # Check service status
    kubectl get services

    # Run health checks
    SERVICE_IP=$(kubectl get service loyalty-batch-engine -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    curl -f "http://${SERVICE_IP}/actuator/health" || {
        echo -e "${RED}Health check failed${NC}"
        return 1
    }
}

# Main deployment process
main() {
    echo -e "${GREEN}Starting deployment process for version ${VERSION} in ${ENVIRONMENT} environment${NC}"

    # Check prerequisites
    check_prerequisites

    # Setup CSI Driver
    setup_csi_driver

    # Setup secrets
    setup_secrets

    # Build application
    build_application

    # Build container
    build_container

    # Deploy to Kubernetes
    deploy_kubernetes

    # Verify Security
    verify_security

    # Verify deployment
    verify_deployment

    echo -e "${GREEN}Deployment completed successfully!${NC}"
}

# Run main function
main
