#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

DB_CONTAINER="postgres-loc"
NETWORK_NAME="loyalty-network"

# Error handling
set -e
trap 'handle_error $? $LINENO' ERR

handle_error() {
    echo -e "${RED}Error: Script failed at line $2 with exit code $1${NC}"
    exit 1
}

echo -e "${BLUE}Setting up local development environment...${NC}"

# Check prerequisites
command -v podman >/dev/null 2>&1 || { echo -e "${RED}Error: podman is required${NC}"; exit 1; }
command -v kubectl >/dev/null 2>&1 || { echo -e "${RED}Error: kubectl is required${NC}"; exit 1; }

# Create network if it doesn't exist
if ! podman network ls | grep -q "${NETWORK_NAME}"; then
    echo -e "${BLUE}Creating network...${NC}"
    podman network create ${NETWORK_NAME}
fi

# Check if PostgreSQL container exists and remove it if necessary
if podman container exists ${DB_CONTAINER}; then
    echo -e "${BLUE}Found existing PostgreSQL container. Removing it...${NC}"
    podman stop ${DB_CONTAINER} 2>/dev/null || true
    podman rm ${DB_CONTAINER} 2>/dev/null || true
fi

# Start PostgreSQL using podman
echo -e "${BLUE}Starting PostgreSQL...${NC}"
podman run -d \
    --name ${DB_CONTAINER} \
    --network ${NETWORK_NAME} \
    -e POSTGRES_DB=loyalty_db \
    -e POSTGRES_USER=postgreadmin \
    -e POSTGRES_PASSWORD=postgreadmin \
    -e PGDATA=/var/lib/postgresql/data/pgdata \
    -p 5432:5432 \
    postgres:17

# Add after the podman run command:
if podman container inspect ${DB_CONTAINER} >/dev/null 2>&1; then
    echo -e "${GREEN}PostgreSQL container started successfully${NC}"
    echo -e "${GREEN}PostgreSQL is running on port 5432${NC}"
else
    echo -e "${RED}Failed to start PostgreSQL container${NC}"
    exit 1
fi

