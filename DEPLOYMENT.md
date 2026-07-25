# Deployment Guide

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- PostgreSQL 17
- Docker & Docker Compose (optional, for local DB)

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SERVER_PORT` | No | `3000` | HTTP listener port |
| `SPRING_DATASOURCE_URL` | Yes | `jdbc:postgresql://localhost:5432/ced_ops` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | `postgres` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Yes | `postgres` | Database password |
| `JWT_SECRET` | **Yes** | *(placeholder)* | HS256 signing key (256-bit, base64-encoded) |
| `JWT_ACCESS_TOKEN_EXPIRATION` | No | `900000` | Access token lifetime in ms (15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | No | `604800000` | Refresh token lifetime in ms (7 days) |

### Generating a JWT Secret

```bash
openssl rand -base64 32
# Example output: 7B5a2p3RvQ9mXwYzK8LcNfTgHj1M4sV6=
```

Set the output as `JWT_SECRET` in your environment. Do not use the placeholder value in production.

## Build

```bash
# Clean build
./mvnw clean package -DskipTests

# Build without tests (if no test DB available)
./mvnw clean package -DskipTests

# Output: target/ced-ops-backend-0.0.1-SNAPSHOT.jar
```

## Database Setup

### Option 1: Docker Compose (Local/Dev)

```bash
docker compose up -d
```

This starts PostgreSQL 17 on port 5432 with database `ced_ops`.

### Option 2: External PostgreSQL

Create the database manually:

```sql
CREATE DATABASE ced_ops;
```

### Migrations

Flyway runs automatically on application startup. The schema is managed entirely through Flyway (DDL auto is set to `validate`).

To check migration status:

```bash
./mvnw flyway:info -Dflyway.url=jdbc:postgresql://<host>:5432/ced_ops \
                   -Dflyway.user=<user> \
                   -Dflyway.password=<password>
```

## Running

### Production

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

## Docker Deployment

Create a `Dockerfile` in the project root:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ced-ops-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Then add to `compose.yml`:

```yaml
services:
  postgres:
    # ... existing postgres config ...

  app:
    build: .
    ports:
      - "3000:3000"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ced_ops
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: "${JWT_SECRET}"
    depends_on:
      postgres:
        condition: service_healthy
```

## Production Considerations

### Required Before Production

1. **Replace JWT secret** with a real 256-bit key (see above)
2. **Change database credentials** from defaults
3. **Add Flyway migrations** for the ~20 Hibernate-generated tables
4. **Set `spring.jpa.hibernate.ddl-auto=validate`** (already set — ensure no tables rely on auto-generation)

### Highly Recommended

- **Add `/actuator/health`** — add `spring-boot-starter-actuator` dependency for health checks and Kubernetes probes
- **Configure CORS** — replace `Customizer.withDefaults()` with explicit allowed origins
- **Add rate limiting** — brute-force protection on `/api/auth/login`
- **Add request logging** — `CommonsRequestLoggingFilter` for observability
- **Configure graceful shutdown** — `server.shutdown=graceful`
- **Set up logging** — configure logback to write to files with rotation, or use JSON logging for log aggregation
- **Use environment profiles** — separate `application-dev.properties` and `application-prod.properties`
- **Add `.env` file** for local development to avoid hardcoded credentials

### Scaling

- The application is stateless (JWT auth), so horizontal scaling by adding instances behind a load balancer works
- File attachments are stored on the local filesystem — for multi-instance deployments, use a shared filesystem (NFS, EFS) or swap to S3-compatible storage
- The `ConcurrentMapCacheManager` is in-memory and per-instance — for clustered deployments, replace with Redis

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
