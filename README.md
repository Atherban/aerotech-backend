# CED Ops Backend

CED Operations Management System — a Spring Boot REST API for managing quality inspection reports, master data, analytics, and workflow in a manufacturing/operations environment.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Runtime** | Java 21 |
| **Framework** | Spring Boot 3.5.4 |
| **Database** | PostgreSQL 17 |
| **ORM** | Spring Data JPA / Hibernate |
| **Migrations** | Flyway |
| **Auth** | JWT (access + refresh tokens), BCrypt |
| **API Docs** | SpringDoc OpenAPI 2.8.9 |
| **Build** | Maven |
| **Mappers** | MapStruct 1.6.3 |
| **Exports** | Apache POI (Excel), OpenPDF (PDF) |

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Docker & Docker Compose (for PostgreSQL)
- IDE with Lombok plugin

## Quick Start

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Build and run
./mvnw spring-boot:run
```

The app starts at `http://localhost:3000`.

## Environment Variables

All properties in `application.properties` can be overridden via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `3000` | HTTP port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ced_ops` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | DB password |
| `JWT_SECRET` | *(placeholder)* | 256-bit HS256 signing key |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900000` | Access token TTL (ms) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh token TTL (ms) |

> **Security**: Change `JWT_SECRET` to a real 256-bit key before any non-local deployment. Generate one with:
> ```bash
> openssl rand -base64 32
> ```

## Running Locally

```bash
# Development mode (with devtools hot reload)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build executable JAR
./mvnw clean package -DskipTests
java -jar target/ced-ops-backend-0.0.1-SNAPSHOT.jar
```

## Database Migrations

Schema is managed exclusively by Flyway (`spring.jpa.hibernate.ddl-auto=validate`).

```bash
# Flyway will apply pending migrations on startup automatically.
# To manually verify:
./mvnw flyway:info
```

Migration files live in `src/main/resources/db/migration/`.

## API Documentation

- **Swagger UI**: http://localhost:3000/swagger-ui.html
- **OpenAPI JSON**: http://localhost:3000/v3/api-docs

## Testing

```bash
./mvnw test
```

> **Note**: Currently only a skeleton context-load test exists. Unit and integration tests are not yet implemented (see `PROJECT_BLUEPRINT.md`).

## Project Structure

```
src/main/java/com/aerotech/ced_ops_backend/
├── analytics/          # Aggregated metrics and dashboards
├── attachment/         # File upload/download management
├── audit/              # Audit logging
├── auth/               # Authentication & authorization
├── common/             # Shared base entities, enums, exceptions, config
├── export/             # Excel/PDF report export
├── integration/        # External system connectors
├── master/             # Master data (line, shift, process, parameter)
├── notification/       # User notifications
├── report/             # Quality inspection reports (6 types + dashboard + search)
├── role/               # Role management
├── security/           # JWT filter, security config, user details
├── settings/           # System settings
└── user/               # User management
```

## License

Proprietary — Aerotech
