# Makefile for JCS Docker

set export

SERVICE_NAME := "jcs-backend"
COMPOSE_FILE := "docker-compose.yaml"
COMPOSE_PROD_FILE := "docker-compose.prod.yaml"

list:
    just --list





########################### Development commands

# Start the existing container (does NOT recreate)
start:
	@echo "Starting ${SERVICE_NAME}..."
	docker compose -f ${COMPOSE_FILE} up -d

# Stop the running container (does NOT remove)
stop:
	@echo "Stopping ${SERVICE_NAME}..."
	docker compose -f ${COMPOSE_FILE} stop

# Restart the container (stop + start)
restart:
	@echo "Restarting ${SERVICE_NAME}..."
	docker compose -f ${COMPOSE_FILE} restart

# Clean everything (remove container, images, volumes)
clean:
	@echo "Cleaning ${SERVICE_NAME} container, images, and volumes..."
	docker compose -f ${COMPOSE_FILE} down --rmi all --volumes --remove-orphans
	docker system prune -f

# Show logs in real-time
logs:
	@echo "Showing logs for ${SERVICE_NAME}..."
	docker compose -f ${COMPOSE_FILE} logs -f
	
# Generate jOOQ classes
codegen:
	@echo "Generating jOOQ classes..."
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn jooq-codegen:generate

# Clean Maven target directory
maven-clean:
	@echo "Cleaning Maven target directory..."
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn clean
	
# Clean and regenerate jOOQ classes
codegen-clean:
	@echo "Cleaning Maven target and regenerating jOOQ classes..."
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn clean
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn jooq-codegen:generate
	
# Compile the project (useful for checking compilation errors)
compile:
	@echo "Compiling ${SERVICE_NAME}..."
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn compile
	
# Full development build: clean + codegen + compile
build:
	@echo "Development build: clean + codegen + compile..."
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn clean
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn compile
	
# Build and restart application (most useful for development)
rebuild:
	@echo "Building and restarting ${SERVICE_NAME}..."
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn clean
	docker compose -f ${COMPOSE_FILE} exec ${SERVICE_NAME} mvn compile
	docker compose -f ${COMPOSE_FILE} restart ${SERVICE_NAME}





########################### Production commands

# Start production container
start-prod:
	@echo "Starting ${SERVICE_NAME} (prod)..."
	docker compose -f ${COMPOSE_PROD_FILE} up -d

# Stop production container
stop-prod:
	@echo "Stopping ${SERVICE_NAME} (prod)..."
	docker compose -f ${COMPOSE_PROD_FILE} stop

# Restart the container (stop + start)
restart-prod:
	@echo "Restarting ${SERVICE_NAME}..."
	docker compose -f ${COMPOSE_PROD_FILE} restart

# Clean everything (remove container, images, volumes)
clean-prod:
	@echo "Cleaning ${SERVICE_NAME} container, images, and volumes..."
	docker compose -f ${COMPOSE_PROD_FILE} down --rmi all --volumes --remove-orphans
	docker system prune -f

# Show production logs
logs-prod:
	@echo "Showing logs for ${SERVICE_NAME} (prod)..."
	docker compose -f ${COMPOSE_PROD_FILE} logs -f

# Package JAR (only needed for production builds)
package:
	@echo "Packaging ${SERVICE_NAME} (for production)..."
	docker compose -f ${COMPOSE_PROD_FILE} exec ${SERVICE_NAME} mvn package