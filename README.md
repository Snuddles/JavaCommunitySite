# Java Community Site - Docker Setup

A Spring Boot application with multi-environment database support using Docker and Spring profiles.

## 🚀 Quick Start

### 1. Setup Environment
```bash
# Create environment file
touch .env

# Edit .env with your database credentials
# DB_USER=your_username
# DB_PASSWORD=your_password
```

### 2. Start Development
```bash
# Local development (Docker PostgreSQL)
just start
```

## 📋 Prerequisites

- Docker and Docker Compose
- Java 21
- Maven 3.11.X+

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 3.4.4
- **Database Access**: jOOQ (type-safe SQL)
- **Database**: PostgreSQL 16
- **Schema Management**: Flyway migrations
- **Connection Pooling**: HikariCP

### Multi-Environment Support
The application supports two database configurations:

| Environment | Database | Profile | Command |
|------------|----------|---------|---------|
| **Local** | Docker PostgreSQL | `local` | `make dev-local` |
| **Remote** | AWS RDS PostgreSQL | `remote` | `make dev-remote` |

## 🐳 Docker Configurations

### Development (`docker-compose.yaml`)
- **Backend**: Spring Boot app with hot reload
- **PostgreSQL**: Local database container (local profile only)
- **Adminer**: Database admin interface (local profile only)

### Production (`docker-compose.prod.yaml`)
- **Backend**: Optimized production build
- **Database**: Remote PostgreSQL only
- **No admin tools**: Minimal production setup

## 📁 Configuration Files

```
src/main/resources/
├── application.properties          # Shared configuration
├── application-local.properties    # Local database settings
└── application-remote.properties   # Remote database settings
```

### Profile Resolution
Spring automatically loads configuration based on the active profile:
1. `application.properties` (base configuration)
2. `application-{profile}.properties` (environment-specific overrides)

## 🛠️ Available Commands

### Development Commands
```bash
make dev         # Start local development (default)
make dev-local   # Start with local PostgreSQL
make dev-remote  # Start with remote PostgreSQL
make up          # Start local in background
```

### Docker Management
```bash
make down        # Stop all containers
make logs        # View application logs
make restart     # Restart containers
make ps          # Show running containers
make clean       # Stop and remove volumes
```

### Database Commands
```bash
make db          # Connect to local PostgreSQL
```

### Production Commands
```bash
make prod        # Start production setup
make build-prod  # Build and start production
```

## 🏃 Running the Application

### Local Development
```bash
# 1. Create environment file
touch .env

# 2. Edit .env with your credentials
# 3. Start local development
make dev

# Application runs at: http://localhost:8080
# Adminer runs at: http://localhost:8081
```

### Remote Development
```bash
# 1. Ensure .env has your RDS credentials
# 2. Start remote development
make dev-remote

# Application runs at: http://localhost:8080
# Connects to your remote database
```

### Production Deployment
```bash
# 1. Set production credentials in .env
# 2. Build and deploy
make build-prod

# Application runs at: http://localhost:8080
# Uses remote database only
```

## 🗄️ Database Management

### Schema Migrations
Database schema is managed by Flyway:
- Migration files: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql`
- Auto-executed on application startup

### Local Database Access
```bash
# Via Adminer (web interface)
http://localhost:8081
System: PostgreSQL
Server: postgres
Database: jcsdb

# Via command line
make db
```

### jOOQ Code Generation
jOOQ generates type-safe Java classes from your database schema:
- Generated during Maven build
- Provides compile-time SQL safety
- Auto-completion for database objects

## 🔧 Environment Variables

The `.env` file contains database credentials used by both environments:

```bash
# Database credentials
DB_USER=your_username
DB_PASSWORD=your_password
```

### How Profiles Use Environment Variables

**Local Profile:**
- URL: `jdbc:postgresql://postgres:5432/jcsdb`
- Credentials: From `.env` file

**Remote Profile:**
- URL: `jdbc:postgresql://your-rds-endpoint:5432/jcsdb`
- Credentials: From `.env` file

## 📊 Monitoring & Debugging

### Application Logs
```bash
# View live logs
make logs

# Debug specific container
docker logs jcs-backend
docker logs jcs-postgres
```

### Database Debugging
- **Local**: Use Adminer at `http://localhost:8081`
- **jOOQ queries**: Enabled in local profile logs
- **Flyway migrations**: Debug logging in local profile

## 🚨 Troubleshooting

### Common Issues

**Container startup fails:**
```bash
# Check if .env file exists
ls -la .env

# Verify Docker is running
docker ps

# Check container logs
docker logs jcs-backend
```

**Database connection fails:**
```bash
# Local: Ensure postgres container is running
docker ps | grep postgres

# Remote: Verify RDS credentials and network access
# Check security groups allow your IP
```

**Profile not activated:**
```bash
# Check application logs for:
# "The following 1 profile is active: local/remote"
make logs | grep profile
```

## 🔄 Development Workflow

### Typical Development Session
```bash
# 1. Start local development
make dev

# 2. Make code changes (auto-reload enabled)
# 3. View logs if needed
make logs

# 4. Stop when done
make down
```

### Testing Remote Connection
```bash
# Switch to remote database for testing
make down
make dev-remote

# Switch back to local
make down
make dev
```

### Database Schema Changes
```bash
# 1. Create new migration file
# src/main/resources/db/migration/V2__add_new_table.sql

# 2. Restart application (migrations auto-run)
make down
make dev
```

## 📝 Notes

- Environment variables are loaded from `.env` file
- Local development includes hot reload for faster development
- Production uses optimized Docker image with pre-built JAR
- Database credentials are never hardcoded in configuration files
- Both environments use the same credentials for consistency
