# Makefile for JCS Docker

set export

SERVICE_NAME := "jcs-backend"
COMPOSE_FILE := "docker-compose.yaml"
COMPOSE_PROD_FILE := "docker-compose.prod.yaml"

list:
    just --list

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