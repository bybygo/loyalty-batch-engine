#!/bin/bash

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "Usage: ./rollback.sh <version>"
    exit 1
fi

# Rollback deployment
kubectl rollout undo deployment/loyalty-batch-engine --to-revision=$VERSION

# Verify rollback
kubectl rollout status deployment/loyalty-batch-engine