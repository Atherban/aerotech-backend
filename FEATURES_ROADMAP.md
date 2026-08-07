# CED Operations — Features & Roadmap

> This document separates what **Version 1 delivers today** from **planned
> enhancements** (V1.x) and **future ideas** (Version 2). For the current
> implementation snapshot, see `CURRENT_STATE.md`; for the full API reference,
> see `API_DOCUMENTATION.md`.

## Table of Contents

1. [Version 1](#1-version-1)
2. [Version 1 — Completed Features](#2-version-1--completed-features)
3. [Version 1.x — Planned Enhancements](#3-version-1x--planned-enhancements)
4. [Version 2 — Future Ideas](#4-version-2--future-ideas)
5. [Production Roadmap](#5-production-roadmap)

---

## 1. Version 1

Version 1 is the **configuration-driven** release: a digitized shop-floor
reporting platform whose report types are defined entirely by master data, not
by code. It covers configuration, session-driven daily entry, dashboard,
analytics, and unified search.

- **Generic report engine** — one engine serves every module from
  configuration alone; adding a report type means adding a Module, never code.
- **Template versioning** — each module is versioned; publishing a template
  freezes it so historical reports keep the spec in use at the time.
- **Session-driven daily entry** — Start → Save & Next (per process) → Save &
  Submit → Completed Report.
- **No approval workflow in V1** — a completed report ends `SUBMITTED`.
- **No report edit / re-run in V1** — completed reports and recorded processes
  are immutable.

## 2. Version 1 — Completed Features

All features below are implemented, verified, and documented. Details in
`CURRENT_STATE.md` and `API_DOCUMENTATION.md`.

| Feature | Status |
|---------|--------|
| Authentication (JWT access + refresh, BCrypt) | ✅ Shipped |
| Role-based access (`SUPER_ADMIN` / `ADMIN` / `OPERATOR`) | ✅ Shipped |
| User management | ✅ Shipped |
| Master data — module types | ✅ Shipped |
| Master data — modules (with unique prefix + lifecycle) | ✅ Shipped |
| Template versioning (publish / supersede / snapshot copy) | ✅ Shipped |
| Master data — processes (ordered by `displayOrder`) | ✅ Shipped |
| Master data — global parameters | ✅ Shipped |
| Master data — process parameter bindings (order, mandatory, visible, unit, min/max, default) | ✅ Shipped |
| Generic report engine (start / save-next / save-submit / sessions / recorded / reports) | ✅ Shipped |
| Automatic shift detection (incl. overnight) | ✅ Shipped |
| Master data — production lines & shifts | ✅ Shipped |
| Dashboard (10 endpoints over the engine) | ✅ Shipped |
| Unified search (reports + users + parameters) | ✅ Shipped |
| Unified pagination & filtering across all list endpoints | ✅ Shipped |
| Analytics & KPIs (overview, quality, consumption, stability, trends, performance) | ✅ Shipped |
| Attachments (upload / view / download / preview / delete) | ✅ Shipped |
| Notifications inbox | ✅ Shipped |
| System settings | ✅ Shipped |
| Integration center (config + enable/disable/test + execution history) | ✅ Shipped |
| Audit logs (read model) | ✅ Shipped |

## 3. Version 1.x — Planned Enhancements

Near-term enhancements that extend Version 1 without reworking the engine.

| Feature | Description | Depends on |
|---------|-------------|------------|
| Approval workflow | Approve / reject a `SUBMITTED` report with remarks; complete the `approvedAt`/`approvedBy` columns. | Backend `PATCH`/`PUT /api/report-engine/reports/{id}/approve|reject` |
| Report edit / re-run | Allow an operator to re-open a session or step backward to correct recorded values before submit. | New update path |
| Reject → resubmit | Return a rejected report to the submission path for correction. | Approval workflow |
| Write-path audit logging | Persist `audit_logs` for login/create/submit/approve/reject events. | — |
| External notification channels | Email / SMS / push delivery (in-app inbox already ships). | — |
| Attachments per recorded value | Attach files to individual recorded values (currently whole-report). | — |
| Parameter-level analytics & per-module dashboard widgets | Drill into individual parameter readings; per-module widgets. | — |

## 4. Version 2 — Future Ideas

Longer-horizon ideas, not commitments.

- **AI Insights** — anomaly detection and reading-trend analysis over recorded
  values.
- **ERP / MES integration** — live data exchange with external systems.
- **Offline sync** — capture reports without connectivity and sync later.
- **Advanced analytics** — richer time series and cross-module analytics.
- **Export services** — backend-generated PDF/Excel/CSV (currently
  frontend-generated from structured JSON).

## 5. Production Roadmap

Hardening items recommended before production deployment. These are
configuration/quality changes, not new features.

| # | Item | Current state | Target |
|---|------|---------------|--------|
| 1 | Database credentials | Environment-configurable (`.env`) | Externalized secret management |
| 2 | `jwt.secret` | Environment variable (already supported) | Rotated, never committed |
| 3 | `ddl-auto` | `update` | `validate` (Flyway owns the schema) |
| 4 | SQL logging | `show-sql=true`, Hibernate `SQL`=DEBUG / `bind`=TRACE | Gated behind a debug profile |
| 5 | Connection pool | No explicit HikariCP settings | Configure max/min/queue and timeouts |
| 6 | Test coverage | 24 unit tests, 1 env-blocked context test | Controller/API tests; CI that provisions Postgres |
| 7 | Observability | No metrics or health endpoint | `spring-boot-starter-actuator` + Micrometer |
| 8 | Default credentials | Seeded `ADMIN001` / `admin123` | Change on first production login |
| 9 | Environment profiles | Single `application.properties` | `dev` / `prod` profiles |
| 10 | CORS | Default configuration | Explicit allowed origins |
| 11 | Rate limiting | None | Brute-force protection on `/api/auth/login` |

---

*Nothing in this document represents a committed delivery date. Version 1.x
items and Version 2 ideas are for planning only.*