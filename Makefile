# Makefile to orchestrate HamBooking multi-container environment

.PHONY: all help xhost up up-d down clean db backend frontend web rebuild rebuild-backend rebuild-frontend rebuild-web status logs logs-backend logs-frontend logs-web

# Default target
all: up

## help: Show this help message
help:
	@echo "HamBooking Container Orchestration Commands:"
	@echo ""
	@grep -E '^## .*$$' $(MAKEFILE_LIST) | sed -e 's/## //' | awk 'BEGIN {FS = ": "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

## xhost: Grant local container access to the X11 display server
xhost:
	@echo "Configuring X11 local access control..."
	@xhost +local: || echo "Warning: xhost command failed (this is normal if running headlessly or outside an X11 session)"

## up: Build, create and start all services in foreground
up: xhost
	@echo "Starting all services (db, backend, frontend)..."
	DISPLAY=$${DISPLAY:-:0} docker compose up --build

## up-d: Build, create and start all services in background (detached mode)
up-d: xhost
	@echo "Starting all services in detached mode..."
	DISPLAY=$${DISPLAY:-:0} docker compose up --build -d

## down: Stop and remove all containers and networks
down:
	@echo "Stopping all services..."
	docker compose down

## clean: Stop containers, remove networks, volumes (hard reset) and orphaned containers
clean:
	@echo "Cleaning up all containers, networks, and persistent database volumes..."
	docker compose down -v --remove-orphans

## db: Start only the MySQL database container
db:
	@echo "Starting database service..."
	docker compose up db

## backend: Start only the Spring Boot backend service
backend:
	@echo "Starting backend service..."
	docker compose up backend

## frontend: Grant X11 access and start only the JavaFX desktop frontend service
frontend: xhost
	@echo "Starting frontend service..."
	DISPLAY=$${DISPLAY:-:0} docker compose up frontend

## web: Start only the React/Vite web frontend service
web:
	@echo "Starting web frontend service..."
	docker compose up frontend-web

## rebuild: Rebuild all Docker images and restart all services
rebuild: xhost
	@echo "Forcing build and restart of all services..."
	DISPLAY=$${DISPLAY:-:0} docker compose up --build --force-recreate

## rebuild-backend: Rebuild the backend image and restart the service
rebuild-backend:
	@echo "Rebuilding and restarting backend service..."
	docker compose build backend
	docker compose up -d backend

## rebuild-frontend: Rebuild the frontend image and restart the service
rebuild-frontend: xhost
	@echo "Rebuilding and restarting frontend service..."
	docker compose build frontend
	DISPLAY=$${DISPLAY:-:0} docker compose up -d frontend

## rebuild-web: Rebuild the web frontend image and restart the service
rebuild-web:
	@echo "Rebuilding and restarting web frontend service..."
	docker compose rm -f -s -v frontend-web
	docker compose build --no-cache frontend-web
	docker compose up -d frontend-web

## status: List status of all active containers in the stack
status:
	@docker compose ps

## logs: View and follow logs of all services
logs:
	@docker compose logs -f

## logs-backend: Follow logs of the backend service
logs-backend:
	@docker compose logs -f backend

## logs-frontend: Follow logs of the frontend service
logs-frontend:
	@docker compose logs -f frontend

## logs-web: Follow logs of the web frontend service
logs-web:
	@docker compose logs -f frontend-web
