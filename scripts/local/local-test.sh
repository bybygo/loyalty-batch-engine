#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Test endpoints
echo -e "${BLUE}Testing endpoints...${NC}"

# Create test transaction
echo -e "${BLUE}Creating test transaction...${NC}"
curl -X POST 'http://localhost:9874/api/v1/test/transactions' \
-H 'Content-Type: application/json' \
-d '{
    "customerId": 1001,
    "amount": 150.50,
    "transactionDate": "2025-02-14T10:30:00",
    "transactionType": "PURCHASE"
}'

echo -e "\n"

# Start batch job
echo -e "${BLUE}Starting batch job...${NC}"
curl -X POST 'http://localhost:9874/api/v1/batch/jobs' \
-H 'Content-Type: application/json' \
-d '{
    "processDate": "2023-12-14",
    "description": "Test batch processing",
    "requestId": "TEST-001"
}'

echo -e "\n"

# Check job status
echo -e "${BLUE}Checking recent jobs...${NC}"
curl -X GET 'http://localhost:9874/api/v1/batch/status?limit=5'