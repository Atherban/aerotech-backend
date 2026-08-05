# CED Ops Backend

CED Operations Management System — a Spring Boot REST API that digitizes
manufacturing quality-inspection and shop-floor reporting: master data
configuration, six predefined report types, an approval workflow, dashboards,
analytics, and global search.

## Features

- **Authentication & roles** — JWT access + refresh tokens, BCrypt, role-based
  access control (SUPER_ADMIN / ADMIN / OPERATOR).
- **Master data** — users, shifts (with automatic, overnight-aware shift
  detection), production lines, and a fully configurable parameter catalog per
  report type.
- **Six standard report types** — Process Monitoring, Chemical Consumption,
  Daily Startup Checklist, Daily Inspection, First Piece Inspection,
  Pre-Delivery Inspection — all built on a shared report engine with a
  consistent lifecycle.
- **Approval workflow** — draft → submit → approve / reject with remarks.
  **V1 limitation:** reports are created in one step and cannot be edited or
  resumed afterwards; rejected reports cannot be edited/re-submitted (no report
  update endpoint).
- **Dashboard & analytics** — live overview, today's reports, pending
  approvals, approval summary, recent activity, and KPI analytics.
- **Global search** — one search across reports, users, and parameters.
- **Unified pagination & filtering** — consistent list experience across every
  module.
- **Attachments, notifications, settings, integration center, audit logs** —
  supporting platform modules. Notifications are workflow-triggered in-app
  (report created/submitted/approved/rejected, user created/welcome, password
  changed).

## Documentation Index

| Document | Audience | Purpose |
|----------|----------|---------|
| [BUSINESS_FLOW.md](BUSINESS_FLOW.md) | Client, frontend, QA | Business functional specification — how the system is used (no implementation details) |
| [PROJECT_BLUEPRINT.md](PROJECT_BLUEPRINT.md) | Backend, architects | Architecture, design decisions, report engine, database, security |
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | Backend, frontend, QA | Complete REST API reference — every endpoint, DTO, request/response |
| [CURRENT_STATE.md](CURRENT_STATE.md) | All | Current implementation snapshot — what exists, limitations, pending work |
| [FEATURES_ROADMAP.md](FEATURES_ROADMAP.md) | Product, all | V1 scope, completed features, future features, production roadmap |
| [VERIFICATION_REPORT.md](VERIFICATION_REPORT.md) | QA, client | Business-workflow audit of the feature-complete V1 backend |
| [CONSISTENCY_REPORT.md](CONSISTENCY_REPORT.md) | All | Documentation-vs-implementation consistency report (V1 capabilities, resolved contradictions, planned features) |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Ops | Deployment, environment variables, Docker Compose, production hardening |

Suggested reading order: **README → BUSINESS_FLOW → PROJECT_BLUEPRINT →
API_DOCUMENTATION → CURRENT_STATE → FEATURES_ROADMAP.**

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
| **Containerization** | Docker Compose |

## Quick Start (Docker Compose)

The fastest way to run the full stack. You only need Docker Desktop (or Docker +
Docker Compose) — no Java or Maven installation required on your host.

```bash
# 1. One-time setup — copy environment template
cp .env.example .env

# 2. Build and start everything
docker compose up --build
```

The app starts at `http://localhost:3000`.

- **Swagger UI:** http://localhost:3000/swagger-ui.html
- **OpenAPI JSON:** http://localhost:3000/v3/api-docs
- **API reference:** `API_DOCUMENTATION.md` (hand-maintained; mirrors the source). No `api-docs.json` snapshot is committed — the OpenAPI spec is generated at runtime by SpringDoc. To produce one: start the app and run `curl http://localhost:3000/v3/api-docs -o api-docs.json`.

## Prerequisites

- **Docker Desktop** (macOS/Windows) or **Docker + Docker Compose** (Linux)
- *(Optional)* Java 21 JDK + Maven 3.9+ for running outside Docker

## Folder Structure

```
src/main/java/com/aerotech/ced_ops_backend/
├── analytics/          # Aggregated metrics and dashboards
├── attachment/         # File upload/download management
├── audit/              # Audit logging (read model)
├── auth/               # Authentication & authorization
├── common/             # Base entities, enums, exceptions, config, pagination
├── integration/        # External system connectors
├── master/             # Master data (line, shift, parameter, report types)
├── notification/       # User notifications
├── report/             # Report engine + 6 report types + dashboard + search
├── role/               # Role management
├── security/           # JWT filter, security config, user details
├── settings/           # System settings
└── user/               # User management
```

## Running the Project

### Start / Stop / Reset

```bash
docker compose up --build        # build + start (use --build on first run)
docker compose down              # stop, keep data
docker compose down -v           # stop and delete database + uploads
docker compose logs -f app       # follow app logs
```

### Run locally (without Docker)

```bash
# 1. Start PostgreSQL
docker compose up -d postgres

# 2. Build and run with Maven
./mvnw spring-boot:run
```

### Tests

```bash
./mvnw test
```

### Environment Variables

All settings are configured via `.env` (copy from `.env.example`):

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

> **Security:** Generate a real JWT secret for any non-local deployment:
> ```bash
> openssl rand -base64 32
> ```

## Database Migrations

Schema is managed by Flyway and migrations run automatically on startup.
Migration files live in `src/main/resources/db/migration/`:

```bash
./mvnw flyway:info -Dflyway.url=jdbc:postgresql://localhost:5432/ced_ops \
                   -Dflyway.user=postgres \
                   -Dflyway.password=postgres
```

## Quality Status

- The business workflow of the feature-complete V1 backend was audited against
  every documented capability per role: **42 of 43 (97.7%) fully implemented**.
  The single gap is report **edit/resubmit** (no update endpoint yet — drafts
  can't be edited/resumed and rejected reports can't be resubmitted). See
  `VERIFICATION_REPORT.md` and `CURRENT_STATE.md`.
- Default seeded Super Admin: `ADMIN001` / `admin123`. **Change this before
  production.**

## Troubleshooting

| Problem | Likely Cause | Fix |
|---------|-------------|-----|
| `JWT_SECRET is required` | Missing `.env` file | `cp .env.example .env` |
| App won't start, DB connection refused | PostgreSQL needs more time | Check `docker compose logs postgres`; the app waits for the DB health check automatically |
| Port 5432 already in use | Another PostgreSQL is running | Set `POSTGRES_PORT=5433` in `.env` |
| Port 3000 already in use | Another process on port 3000 | Set `SERVER_PORT=3001` in `.env` |
| Uploaded files lost | Container recreated without volume | Named volume `app_uploads` preserves uploads — `docker compose down -v` deletes them |

## License

Proprietary — Aerotech
