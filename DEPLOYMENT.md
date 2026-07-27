# Deployment Guide

## Prerequisites

- **Docker Desktop** or **Docker + Docker Compose** (recommended)
- Java 21 (JDK) and Maven 3.9+ (if running outside Docker)
- PostgreSQL 17 (handled by Docker Compose)

## Quick Start (Docker)

```bash
cp .env.example .env
docker compose up --build
```

This builds and starts both PostgreSQL and the Spring Boot application. The app is available at `http://localhost:3000`.

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `POSTGRES_DB` | No | `ced_ops` | PostgreSQL database name |
| `POSTGRES_USER` | No | `postgres` | Database user |
| `POSTGRES_PASSWORD` | No | `postgres` | Database password |
| `POSTGRES_PORT` | No | `5432` | Host port for PostgreSQL |
| `SERVER_PORT` | No | `3000` | HTTP listener port |
| `SPRING_PROFILES_ACTIVE` | No | *(empty)* | Spring profile |
| `JWT_SECRET` | **Yes** | *(required)* | HS256 signing key (256-bit, base64-encoded) |
| `JWT_ACCESS_TOKEN_EXPIRATION` | No | `900000` | Access token lifetime in ms (15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | No | `604800000` | Refresh token lifetime in ms (7 days) |

### Generating a JWT Secret

```bash
openssl rand -base64 32
# Example output: 7B5a2p3RvQ9mXwYzK8LcNfTgHj1M4sV6=
```

Set the output as `JWT_SECRET` in `.env`. Do not use the placeholder value in production.

## Docker Compose

The project includes a complete `compose.yml` with two services:

| Service | Image | Purpose |
|---------|-------|---------|
| `postgres` | `postgres:17-alpine` | PostgreSQL database with health check |
| `app` | Built from `Dockerfile` | Spring Boot application |

### Build

```bash
# Build without starting
docker compose build

# Build and start
docker compose up --build
```

The `Dockerfile` uses a multi-stage build:
1. **Builder stage** — `maven:3.9-eclipse-temurin-21-alpine` — compiles the application and packages a fat JAR
2. **Runtime stage** — `eclipse-temurin:21-jre-alpine` — runs the JAR as a non-root user

Dependency caching is handled by Docker layer caching: `pom.xml` is copied first and `mvn dependency:go-offline` is run separately so Maven dependencies are only re-downloaded when `pom.xml` changes.

### Database

PostgreSQL data is persisted in a named volume `postgres_data`. File uploads persist in `app_uploads`.

### Startup Order

The `app` service has `depends_on` with `condition: service_healthy` on `postgres`. The PostgreSQL health check uses `pg_isready` (interval: 5s, timeout: 5s, retries: 5, start period: 10s). The app only starts after PostgreSQL is accepting connections.

## Manual Build (without Docker)

```bash
# Clean build
./mvnw clean package -DskipTests

# Output: target/ced-ops-backend-0.0.1-SNAPSHOT.jar
```

## Database Setup

### Option 1: Docker Compose (Recommended)

```bash
docker compose up -d postgres
```

This starts PostgreSQL 17 on port 5432 with database `ced_ops`.

### Option 2: External PostgreSQL

Create the database manually:

```sql
CREATE DATABASE ced_ops;
```

### Migrations

Flyway runs automatically on application startup. The schema is managed through Flyway.

To check migration status:

```bash
./mvnw flyway:info -Dflyway.url=jdbc:postgresql://<host>:5432/ced_ops \
                   -Dflyway.user=<user> \
                   -Dflyway.password=<password>
```

## Running

### Docker

```bash
docker compose up --build -d   # start in background
docker compose logs -f          # follow logs
docker compose down             # stop
```

### Production (bare metal)

```bash
export JWT_SECRET="<generated-256-bit-key>"
export SPRING_DATASOURCE_URL="jdbc:postgresql://<db-host>:5432/ced_ops"
export SPRING_DATASOURCE_USERNAME="<db-user>"
export SPRING_DATASOURCE_PASSWORD="<db-password>"

java -jar target/ced-ops-backend-0.0.1-SNAPSHOT.jar
```

### As a Systemd Service

```ini
[Unit]
Description=CED Ops Backend
After=network.target postgresql.service

[Service]
Type=simple
User=cedops
WorkingDirectory=/opt/ced-ops-backend
Environment=JWT_SECRET=<key>
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ced_ops
Environment=SPRING_DATASOURCE_USERNAME=cedops
Environment=SPRING_DATASOURCE_PASSWORD=<password>
ExecStart=/usr/bin/java -jar /opt/ced-ops-backend/ced-ops-backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

## Docker Image Details

### Dockerfile

The `Dockerfile` at the project root is a multi-stage build:

```
maven:3.9-eclipse-temurin-21-alpine    →   eclipse-temurin:21-jre-alpine
(builder stage)                         (runtime stage, ~200MB)
```

Key characteristics:
- **Non-root user**: Application runs as `appuser` for security
- **Upload directory**: `/app/uploads` created with correct permissions
- **Port**: 3000 (EXPOSE)
- **Health check**: No actuator endpoint — the `compose.yml` handles dependency ordering via `pg_isready`

### .dockerignore

Excludes from build context: `target/`, `.git/`, `.env`, `uploads/` (handled via volume), and other non-essential files.

## Production Considerations

### Required Before Production

1. **Replace JWT secret** with a real 256-bit key (see above)
2. **Change database credentials** from defaults in `.env`
3. **Set `spring.jpa.hibernate.ddl-auto=validate`** in `application.properties` (currently `update` — Flyway should manage the schema)
4. **Add `spring-boot-starter-actuator`** for health check endpoints (enables Docker health checks on the app container)
5. **Configure CORS** — replace `Customizer.withDefaults()` with explicit allowed origins

### Highly Recommended

- **Add `/actuator/health`** — add `spring-boot-starter-actuator` dependency for health checks and Kubernetes probes
- **Add rate limiting** — brute-force protection on `/api/auth/login`
- **Add request logging** — `CommonsRequestLoggingFilter` for observability
- **Configure graceful shutdown** — `server.shutdown=graceful`
- **Set up logging** — configure logback to write to files with rotation, or use JSON logging for log aggregation
- **Use environment profiles** — separate `application-dev.properties` and `application-prod.properties`

### Scaling

- The application is stateless (JWT auth), so horizontal scaling by adding instances behind a load balancer works
- File attachments are stored on the local filesystem (`/app/uploads`) — for multi-instance deployments, use a shared filesystem (NFS, EFS) or swap to S3-compatible storage
- The `ConcurrentMapCacheManager` is in-memory and per-instance — for clustered deployments, replace with Redis
- Add a load balancer (nginx/traefik) in front of multiple app instances

### Monitoring

- No metrics or health endpoints are currently exposed
- Consider adding Micrometer + Prometheus for metrics collection
- Consider adding structured logging (JSON) for log aggregation tools

## Rollback

```bash
# Rollback to a previous JAR version
java -jar target/ced-ops-backend-0.0.1-SNAPSHOT.jar.bak

# Flyway rollback (if using undo migrations)
./mvnw flyway:undo
```

> Flyway undo migrations must be authored manually. Without undo scripts, roll back by restoring the DB from backup and deploying the previous application version.

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| `JWT_SECRET is required` | No `.env` file | `cp .env.example .env` |
| Container exits immediately | DB not reachable | App waits for `pg_isready` automatically; check `docker compose logs postgres` |
| Port conflict | Local service on same port | Change ports in `.env` |
| Uploaded files lost | Volume removed | Use `docker compose down` (keep data) not `docker compose down -v` (delete data) |
| Slow first build | Maven downloads all deps | Normal for first build; subsequent builds are cached |
