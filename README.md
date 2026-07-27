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
| **Build** | Maven (Maven Wrapper) |
| **Mappers** | MapStruct 1.6.3 |
| **Exports** | Apache POI (Excel), OpenPDF (PDF) |
| **Containerization** | Docker Compose |

## Quick Start (Docker Compose)

This is the fastest way to get the full stack running. You only need Docker Desktop (or Docker + Docker Compose) — no Java or Maven installation required on your host.

```bash
# 1. One-time setup — copy environment template
cp .env.example .env

# 2. Build and start everything
docker compose up --build
```

The app starts at `http://localhost:3000`.

**Swagger UI**: http://localhost:3000/swagger-ui.html
**OpenAPI JSON**: http://localhost:3000/v3/api-docs

## Prerequisites

- **Docker Desktop** (macOS/Windows) or **Docker + Docker Compose** (Linux)
- *(Optional)* Java 21 JDK + Maven 3.9+ for running outside Docker
- IDE with Lombok plugin

## Workflow

### Start
```bash
docker compose up --build
```
Use `--build` on first run and after every code change. Omit it on subsequent starts if no code changed.

### Stop (keep data)
```bash
docker compose down
```

### Stop and reset everything (delete database)
```bash
docker compose down -v
```

### View logs
```bash
docker compose logs -f          # both services
docker compose logs -f app      # app only
docker compose logs -f postgres # database only
```

### Enter the app container
```bash
docker compose exec app sh
```

### Rebuild after code changes
```bash
docker compose up --build
```
Docker caches Maven dependencies — only changed source files trigger recompilation.

### Rebuild after dependency changes (pom.xml)
```bash
docker compose build --no-cache app
```

## Environment Variables

All settings are configured via `.env` (copy from `.env.example`). Variables and their defaults:

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | `ced_ops` | PostgreSQL database name |
| `POSTGRES_USER` | `postgres` | PostgreSQL user |
| `POSTGRES_PASSWORD` | `postgres` | PostgreSQL password |
| `POSTGRES_PORT` | `5432` | PostgreSQL host port |
| `SERVER_PORT` | `3000` | HTTP port |
| `JWT_SECRET` | *(required)* | 256-bit HS256 signing key |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900000` | Access token TTL (ms, 15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh token TTL (ms, 7 days) |
| `SPRING_PROFILES_ACTIVE` | *(empty)* | Spring profile (e.g. `dev`) |

> **Security**: Generate a real JWT secret for any non-local deployment:
> ```bash
> openssl rand -base64 32
> ```
> Then update `JWT_SECRET` in `.env`.

## Running Locally (without Docker)

```bash
# 1. Start PostgreSQL (Docker or local)
docker compose up -d postgres

# 2. Build and run with Maven
./mvnw spring-boot:run
```

## API Documentation

- **Swagger UI**: http://localhost:3000/swagger-ui.html
- **OpenAPI JSON**: http://localhost:3000/v3/api-docs

## Database Migrations

Schema is managed by Flyway. Migrations run automatically on startup.

Migration files live in `src/main/resources/db/migration/`.

```bash
# Check migration status from host
./mvnw flyway:info -Dflyway.url=jdbc:postgresql://localhost:5432/ced_ops \
                   -Dflyway.user=postgres \
                   -Dflyway.password=postgres

# Check status from inside the container
docker compose exec app sh -c "java -jar app.jar --spring.flyway.enabled=false"
```

## Testing

```bash
./mvnw test
```

> **Note**: Currently only a skeleton context-load test exists.

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

## Troubleshooting

| Problem | Likely Cause | Fix |
|---------|-------------|-----|
| `JWT_SECRET is required` | Missing `.env` file | `cp .env.example .env` |
| App won't start, DB connection refused | PostgreSQL needs more time | Check `docker compose logs postgres`; the app waits for the DB health check automatically |
| Port 5432 already in use | Another PostgreSQL is running on your machine | Set `POSTGRES_PORT=5433` in `.env` |
| Port 3000 already in use | Another process on port 3000 | Set `SERVER_PORT=3001` in `.env` |
| Maven build fails with "package ... does not exist" | Lombok/MapStruct annotation processing issue | Run `./mvnw clean compile` on your host first to verify the build works |
| Uploaded files lost | Container recreated without volume | Named volume `app_uploads` preserves uploads — `docker compose down -v` deletes them |

## License

Proprietary — Aerotech
