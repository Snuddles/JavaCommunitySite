# Java Community Site

A Spring Boot web application for managing a Java developer community platform with features for posts, comments, voting, and user management.

## Project Overview

This is a full-stack web application built with Spring Boot that provides a Reddit-like community platform specifically designed for Java developers. The application includes:

### Core Features
- **User Management**: User registration, authentication, and profiles
- **Community System**: Organized communities for different Java topics
- **Post Management**: Create, read, update posts with categorization
- **Comment System**: Comments on posts
- **Voting System**: Upvote/downvote functionality for posts
- **Category Management**: Organize posts by categories

### Technology Stack
- **Backend**: Spring Boot 3.4.4 with Java 21
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway for database versioning
- **Security**: Spring Security
- **Frontend**: Thymeleaf templating engine
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Development**: Spring DevTools for hot reload
- **Containerization**: Docker with Docker Compose

## Prerequisites

Before setting up the project, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **Git**

## Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd java-community-site
```

### 2. Environment Configuration

Create a `.env` file in the project root with the following environment variables:

```bash
# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=jcsdb
DB_USER=jcsadmin
DB_PASSWORD=supersecret
```

### 3. Running with Docker (Recommended)

The easiest way to run the application is using Docker Compose:

#### Development Environment
```bash
# Start all services (application + database + adminer)
make up

# Or manually
docker compose --env-file .env up --build -d
```

#### Production Environment
```bash
make prod

# Or manually
docker compose --env-file .env -f docker-compose.prod.yaml up -d
```

### 4. Running Locally (Without Docker)

If you prefer to run the application locally:

1. **Start PostgreSQL database only:**
   ```bash
   docker run -d \
     --name jcs-postgres \
     -e POSTGRES_DB=jcsdb \
     -e POSTGRES_USER=jcsadmin \
     -e POSTGRES_PASSWORD=supersecret \
     -p 5432:5432 \
     postgres:16
   ```

2. **Update application.properties** for local database connection:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/jcsdb
   ```

3. **Run the Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## Available Services

Once the application is running, you can access:

- **Main Application**: http://localhost:8080
- **API Documentation (Swagger)**: http://localhost:8080/swagger-ui.html
- **Database Admin (Adminer)**: http://localhost:8081
  - System: PostgreSQL
  - Server: postgres
  - Username: jcsadmin
  - Password: supersecret
  - Database: jcsdb

## Development Commands

### Using Makefile

```bash
# Start development environment
make dev

# View logs
make logs

# Restart services
make restart

# Stop services
make down

# Clean (remove volumes)
make clean

# Connect to database
make db
```

### Manual Docker Commands

```bash
# Build and start services
docker compose up --build -d

# View logs
docker compose logs -f

# Stop services
docker compose down

# Remove volumes (clean slate)
docker compose down -v
```

### Maven Commands

```bash
# Run tests
./mvnw test

# Package application
./mvnw package

# Run application locally
./mvnw spring-boot:run
```

## Database Management

The application uses Flyway for database migrations. Migration files are located in `src/main/resources/db/migration/`.

### Database Schema

The application includes the following main entities:
- **Users**: User accounts and authentication
- **Communities**: Organized groups for posts
- **Posts**: Main content with title and content
- **Comments**: Replies to posts
- **Categories**: Tags for organizing posts
- **Votes**: Upvote/downvote system

## API Endpoints

The application provides RESTful APIs for:
- `/api/users` - User management
- `/api/communities` - Community operations
- `/api/posts` - Post CRUD operations
- `/api/comments` - Comment management
- `/api/votes` - Voting functionality
- `/heartbeat` - Health check endpoint

Complete API documentation is available at the Swagger UI endpoint when the application is running.

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8080, 5432, and 8081 are available
2. **Docker issues**: Make sure Docker daemon is running
3. **Database connection**: Verify PostgreSQL container is healthy
4. **Environment variables**: Check `.env` file configuration

### Logs

View application logs:
```bash
# All services
make logs

# Specific service
docker compose logs -f jcs-backend
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./mvnw test`
5. Submit a pull request

## Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.4/maven-plugin)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.4.4/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.4.4/reference/using/devtools.html)
* [Docker Compose Support](https://docs.spring.io/spring-boot/3.4.4/reference/features/dev-services.html#features.dev-services.docker-compose)
* [Thymeleaf](https://docs.spring.io/spring-boot/3.4.4/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.4/reference/web/servlet.html)

