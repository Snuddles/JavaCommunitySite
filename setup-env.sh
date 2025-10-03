#!/bin/bash

# Java Community Site - Environment Setup Script
# This script sets up the required environment variables for AWS RDS database connection

echo "Setting up environment variables for Java Community Site..."

# Database credentials (from .env file)
export DB_USER=postgres
export DB_PASSWORD=JCSpassword123!

# Optional: Set Spring profile if needed
# export SPRING_PROFILES_ACTIVE=default

echo "✅ Environment variables set successfully!"
echo "DB_USER: $DB_USER"
echo "DB_PASSWORD: [HIDDEN]"
echo ""
echo "You can now start the Spring Boot application with: ./mvnw spring-boot:run"
echo "Or source this file in your shell with: source setup-env.sh"
