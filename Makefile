.PHONY: build test clean docker-build docker-push run help \
        postgres postgres-volume postgres-clean \
        bytebase bytebase-volume bytebase-stop bytebase-clean \
        start-all stop-all status logs docker-run docker-stop docker-logs

APP_NAME=loyalty-batch-engine
VERSION=latest
DOCKER_IMAGE=$(APP_NAME):$(VERSION)

help:
	@echo "Available targets:"
	@echo "  build              - Build the application"
	@echo "  test              - Run tests"
	@echo "  clean             - Clean build artifacts"
	@echo "  docker-build      - Build Docker image"
	@echo "  docker-push       - Push Docker image to registry"
	@echo "  run               - Run the application locally"
	@echo "  spotless          - Format code using spotless"
	@echo "  coverage          - Generate test coverage report"
	@echo "  postgres          - Start PostgreSQL container"
	@echo "  postgres-volume   - Create PostgreSQL Volume"
	@echo "  postgres-clean    - Remove PostgreSQL container and volume"
	@echo "  bytebase         - Start Bytebase container"
	@echo "  bytebase-clean   - Remove Bytebase container and volume"
	@echo "  start-all        - Start all services (PostgreSQL, Bytebase, App)"
	@echo "  stop-all         - Stop all services"
	@echo "  status           - Show status of all containers"
	@echo "  logs             - Show logs (usage: make logs service=[app|postgres|bytebase])"
	@echo "  docker-logs      - Show application container logs"
	@echo "  docker-stop      - Stop application container"

build:
	./gradlew build -x test

test:
	./gradlew test

clean:
	./gradlew clean

run:
	./gradlew bootRun

spotless:
	./gradlew spotlessApply

coverage:
	./gradlew jacocoTestReport
	@echo "Coverage report generated in build/jacocoHtml/index.html"

azure-prepare:
	./gradlew azurePrepareBuild

azure-verify:
	./gradlew azureVerifyBuild

docker-build:
	podman build -t $(DOCKER_IMAGE) .

docker-push:
	podman push $(DOCKER_IMAGE)

docker-run: docker-build
	podman run --replace --name $(APP_NAME) \
		-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/loyalty_db \
		-p 9874:9874 \
		-d $(DOCKER_IMAGE)

docker-stop:
	podman stop $(APP_NAME) || true

docker-logs:
	podman logs -f $(APP_NAME)

postgres-volume:
	podman volume create loyalty-postgres-data || true

postgres: postgres-volume
	podman run --replace --name loyalty-postgres \
		-e POSTGRES_USER=loyaltyadmin \
		-e POSTGRES_PASSWORD=loyaltyadmin \
		-e POSTGRES_DB=loyalty_db \
		-v loyalty-postgres-data:/var/lib/postgresql/data \
		-p 5432:5432 \
		-d postgres:latest

postgres-stop:
	podman stop loyalty-postgres || true

postgres-clean:
	podman rm -f loyalty-postgres 2>/dev/null || true
	podman volume rm loyalty-postgres-data 2>/dev/null || true

bytebase-volume:
	podman volume create bytebase-data || true

bytebase: bytebase-volume
	podman run --replace --name bytebase \
		--publish 8080:8080 \
		--volume bytebase-data:/var/opt/bytebase \
		-d bytebase/bytebase:$(BYTEBASE_VERSION)

bytebase-stop:
	podman stop bytebase || true

bytebase-clean: bytebase-stop
	podman rm -f bytebase 2>/dev/null || true
	podman volume rm bytebase-data 2>/dev/null || true

start-all: postgres bytebase docker-run
	@echo "All services started"

stop-all: docker-stop postgres-clean bytebase-stop
	@echo "All services stopped"

status:
	@echo "=== Container Status ==="
	podman ps -a

logs:
	@if [ "$(service)" = "app" ]; then \
		podman logs -f $(APP_NAME); \
	elif [ "$(service)" = "postgres" ]; then \
		podman logs -f loyalty-postgres; \
	elif [ "$(service)" = "bytebase" ]; then \
		podman logs -f bytebase; \
	else \
		echo "Usage: make logs service=[app|postgres|bytebase]"; \
	fi
