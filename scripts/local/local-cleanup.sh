#!/bin/bash
# local-cleanup.sh

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Variables
APP_NAME="loyalty-batch-engine"
DB_CONTAINER="postgres-loc"
NETWORK_NAME="loyalty-network"

# Stop and remove containers
echo -e "${BLUE}Stopping containers...${NC}"
podman stop ${APP_NAME} ${DB_CONTAINER} || true
podman rm ${APP_NAME} ${DB_CONTAINER} || true

# Remove images
echo -e "${BLUE}Removing images...${NC}"
podman rmi localhost/${APP_NAME}:1.0.0 || true

# Remove network
echo -e "${BLUE}Removing network...${NC}"
podman network rm ${NETWORK_NAME} || true

# Remove volumes (optional, uncomment if needed)
# echo -e "${BLUE}Removing volumes...${NC}"
# podman volume rm loyalty-db-data || true

echo -e "${GREEN}Cleanup completed${NC}"