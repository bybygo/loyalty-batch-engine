#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Variables
VERSION="0.0.1"
APP_NAME="loyalty-batch-engine"

# Get script location and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../" && pwd)"

CONTAINER_REGISTRY="localhost"

# Error handling
set -e
trap 'handle_error $? $LINENO' ERR

handle_error() {
    echo -e "${RED}Error: Script failed at line $2 with exit code $1${NC}"
    exit 1
}

# Navigate to project root
cd "${PROJECT_ROOT}"

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}Error: gradlew not found in ${PROJECT_ROOT}${NC}"
    exit 1
fi

# Check if Dockerfile/Containerfile exists
if [ -f "Dockerfile" ]; then
    CONTAINER_FILE="Dockerfile"
elif [ -f "Containerfile" ]; then
    CONTAINER_FILE="Containerfile"
else
    echo -e "${RED}Error: Neither Dockerfile nor Containerfile found in ${PROJECT_ROOT}${NC}"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Build application
echo -e "${BLUE}Building application...${NC}"
echo -e "${BLUE}Building from context: ${PROJECT_ROOT}${NC}"
echo -e "${BLUE}Using container file: ${CONTAINER_FILE}${NC}"

./gradlew clean build

# After gradle build, verify the JAR exists
if [ ! -f "build/libs/${APP_NAME}-${VERSION}.jar" ]; then
    echo -e "${RED}Error: JAR file not found after build${NC}"
    exit 1
fi

# Build container image
echo -e "${BLUE}Building container image...${NC}"
podman build -t ${CONTAINER_REGISTRY}/${APP_NAME}:${VERSION} -f ${CONTAINER_FILE} .

# Check if container exists and remove it
if podman container exists ${APP_NAME}; then
    echo -e "${BLUE}Found existing container. Removing it...${NC}"
    podman stop ${APP_NAME} 2>/dev/null || true
    podman rm ${APP_NAME} 2>/dev/null || true
fi

# Create network if it doesn't exist
if ! podman network ls | grep -q "loyalty-network"; then
    echo -e "${BLUE}Creating network...${NC}"
    podman network create loyalty-network || true
fi

# Run the application
echo -e "${BLUE}Starting application...${NC}"
podman run -d \
    --name ${APP_NAME} \
    -p 9874:9874 \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.containers.internal:5432/loyalty_db \
    -e SPRING_DATASOURCE_USERNAME=postgreadmin \
    -e SPRING_DATASOURCE_PASSWORD=postgreadmin \
    ${CONTAINER_REGISTRY}/${APP_NAME}:${VERSION}

# Verify container is running
if podman container inspect ${APP_NAME} >/dev/null 2>&1; then
    echo -e "${GREEN}Container started successfully${NC}"
else
    echo -e "${RED}Failed to start container${NC}"
    exit 1
fi

# Add after container start
echo -e "${BLUE}Waiting for application to start...${NC}"

MAX_RETRIES=30
RETRY_INTERVAL=2
COUNTER=0

while [ $COUNTER -lt $MAX_RETRIES ]; do
    if curl -f http://localhost:9874/api/v1/health -s > /dev/null; then
        echo -e "${GREEN}Application started successfully${NC}"
        echo -e "${GREEN}Application is running on http://localhost:9874${NC}"
        exit 0
    else
        echo -e "${YELLOW}Waiting for application startup... (Attempt $((COUNTER+1))/$MAX_RETRIES)${NC}"
        COUNTER=$((COUNTER+1))
        sleep $RETRY_INTERVAL
    fi
done

echo -e "${RED}Application failed to start after $((MAX_RETRIES * RETRY_INTERVAL)) seconds${NC}"
exit 1