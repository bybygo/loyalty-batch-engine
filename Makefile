# Makefile

.PHONY: deploy rollback clean test

# Load environment variables
include .env.$(ENVIRONMENT)

deploy:
	./deploy-master.sh

rollback:
	./scripts/rollback.sh $(VERSION)

clean:
	kubectl delete namespace $(NAMESPACE)
	./scripts/cleanup.sh

test:
	./gradlew test

generate-secrets:
	./scripts/generate-secrets.sh

sync-secrets:
	./scripts/sync-vault-secrets.sh