# CED Ops Backend

CED Operations Management System — a Spring Boot REST API that digitizes
manufacturing shop-floor quality and inspection reporting. It provides a
**configuration-driven report engine** (module master data → versioned
templates → process steps → recorded values), plus master data, dashboard,
analytics, unified search, and supporting platform modules.

There is **no report-specific Java code**: every report is defined by
configuration, so new report types are added as data, never as code.

- **Quick start:** see [Quick Start](#quick-start)
- **API reference:** [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- **OpenAPI (Swagger UI):** `/swagger-ui.html` · raw spec `/v3/api-docs`

---

## Features

- **Authentication & roles** — JWT access + refresh tokens, BCrypt, role-based
  access control (`SUPER_ADMIN` / `ADMIN` / `OPERATOR`).
- **Configuration-driven report engine** — the report engine build an
  Architecture hierarchy `Module Type → Module → Template Version → Process →
  Process Parameter → (global) Parameter`. Work-in-progress is captured in a
  **report session**; **Save & Submit** produces a **completed report** with
  immutable snapshots of the configuration used at submit time.
- **Template versioning** — every module is versioned; publishing a version
  freezes it and supersedes earlier `ACTIVE` versions so historical reports keep
  the exact specification they were recorded against.
- **Automatic shift detection** — the backend assigns the current shift
  (overnight shifts included) unless a shift/line is provided at start.
- **Dashboard & analytics** — live overview, today's reports, approval summary,
  recent activity, and KPI analytics — all read from the engine.
- **Unified search** — one search across reports, users, and parameters.
- **Unified pagination & filtering** — consistent list experience across every
  module.
- **Supporting modules** — attachments, notifications, system settings,
  integration center, audit logs.

## Architecture Overview

```
HTTP / REST / JWT
      │
      ▼
Controller ──► Service ──► Repository ──► PostgreSQL
     │            │            │
     └── DTOs ────┘            └── Flyway migrations
```

The domain is a single, generic data model — no per-report-type tables:

```
Module Type
   ──1:N──► Module
              ──1:N──► Template Version
                          ──1:N──► Process
                                      ──1:N──► Process Parameter ──M:1──► Parameter (global)

Report execution (work-in-progress → permanent record):
Report Session ──► Recorded Process (×1 per process) ──► Recorded Value
   └── Save & Submit ──► Completed Report (immutable snapshots)
```

Report workflow (backend-authoritative; the frontend only renders each step):

```
Start → save / next (per process, ordered by displayOrder) → Save & Submit
      → Completed Report (SUBMITTED with immutable snapshots)
```

## Folder Structure

```
src/main/java/com/aerotech/ced_ops_backend/
├── analytics/          # Aggregated KPI metrics
├── attachment/         # File upload/download management
├── audit/              # Audit log read model
├── auth/               # Authentication & authorization
├── common/             # Base entities, enums, exceptions, config, pagination
├── integration/        # External system connectors
├── master/
│   ├── line/           # Production line master data
│   ├── shift/          # Shift master data + automatic shift detection
│   └── module/         # Module hierarchy: module types, modules,
│                       #   template versions, processes, global parameters
├── notification/       # User notifications
├── report/
│   ├── engine/         # Generic report engine (sessions + completed reports)
│   ├── dashboard/      # Dashboard summaries
│   └── globalsearch/   # Unified search (reports/users/parameters)
├── role/               # Role management
├── security/           # JWT filter, security config, user details
├── settings/           # System settings
└── user/               # User management
```

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

## Quick Start (Docker Compose)

```bash
# 1. One-time setup — copy environment template
cp .env.example .env

# 2. Build and start everything
docker compose up --build
```

The app listens on `http://localhost:3000`.

- **Swagger UI:** http://localhost:3000/swagger-ui.html
- **OpenAPI JSON:** http://localhost:3000/v3/api-docs
- **API reference:** `API_DOCUMENTATION.md` (hand-maintained, mirrors the source).

> The OpenAPI spec is generated at runtime by SpringDoc from the controllers. A
> committed snapshot is kept at `api-docs.json` for frontend/QA tooling; refresh
> it with the exported JSON path above whenever APIs change.

## Prerequisites

- **Docker Desktop** (macOS/Windows) or **Docker + Docker Compose** (Linux)
- *(Optional)* Java 21 JDK + Maven 3.9+ for running outside Docker

## Documentation Index

| Document | Audience | Purpose |
|----------|----------|---------|
| [BUSINESS_FLOW.md](BUSINESS_FLOW.md) | Client, frontend, QA | Business functional specification — how the system is used (no implementation details) |
| [PROJECT_BLUEPRINT.md](PROJECT_BLUEPRINT.md) | Backend, architects | Architecture, design decisions, report engine, database, security |
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | Backend, frontend, QA | Complete REST API reference — every endpoint, DTO, request/response |
| [CURRENT_STATE.md](CURRENT_STATE.md) | All | Current implementation snapshot — what exists, limitations, pending work |
| [FEATURES_ROADMAP.md](FEATURES_ROADMAP.md) | Product, all | V1 scope, planned enhancements, future ideas, production roadmap |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Ops | Deployment, environment variables, Docker Compose, production hardening |
| [MIGRATION_PLAN.md](MIGRATION_PLAN.md) | Backend, architects | Historical record of the phased migration to the report engine |

Suggested reading order: **README → BUSINESS_FLOW → PROJECT_BLUEPRINT →
API_DOCUMENTATION → CURRENT_STATE → FEATURES_ROADMAP.**

## Running Locally

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

## Known Limitations

- **No approval workflow yet** — a completed report begins in `SUBMITTED`;
  approve/reject endpoints are not implemented (`approvedAt`/`approvedBy`
  columns are forward-compatible).
- **No report edit / re-run** — a completed report cannot be edited, a session
  cannot step backwards, and there is no report update/delete endpoint.
- **Audit logs are read-only** — nothing writes to `audit_logs` yet.
- **Production hardening pending** — DB credentials are configured via
  environment but `ddl-auto` is `update` (should be `validate` for prod) and
  observability/metrics are not configured. See `FEATURES_ROADMAP.md` §5.
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