# CED Operations — Features & Roadmap

> Version 1 and beyond. This document describes what Version 1 delivers, what is
> planned next, and the production hardening roadmap. For the current
> implementation snapshot, see `CURRENT_STATE.md`; for the full API reference,
> see `API_DOCUMENTATION.md`.

## Table of Contents

1. [Version 1](#1-version-1)
2. [Completed Features](#2-completed-features)
3. [Future Features](#3-future-features)
4. [Version 2 Ideas](#4-version-2-ideas)
5. [Production Roadmap](#5-production-roadmap)

---

## 1. Version 1

Version 1 is the **predefined-reports** release: a digitized shop-floor
reporting system covering configuration, daily entry, approval, and management
insight.

- **Six standard report types** (fixed, not user-creatable): Process Monitoring,
  Chemical Consumption, Daily Startup Checklist, Daily Inspection, First Piece
  Inspection, Pre-Delivery Inspection.
- **Configurable report templates** — each factory defines its own parameters
  (fields) per report type.
- **Full report lifecycle** — draft → submit → approve / reject. **Not in V1:**
  editing an existing draft or resuming it, and editing/re-submitting a rejected
  report (no report update endpoint). See the capability table in §2.
- **Live management view** — dashboard, analytics, and global search.
- **Custom report creation is NOT supported** in Version 1. There is no generic
  report builder; the six types are the catalog.

## 2. Completed Features

All Version 1 features below are implemented, verified, and documented.
Details in `CURRENT_STATE.md` and `API_DOCUMENTATION.md`.

| Feature | Status |
|---------|--------|
| Authentication (JWT access + refresh, BCrypt) | ✅ Shipped |
| Role-based access (SUPER_ADMIN / ADMIN / OPERATOR) | ✅ Shipped |
| User management | ✅ Shipped |
| Master data — shifts (automatic shift detection incl. overnight) | ✅ Shipped |
| Master data — production lines | ✅ Shipped |
| Master data — parameters (full 11-attribute template config) | ✅ Shipped |
| Report type catalog (fixed, read-only) | ✅ Shipped |
| Six report modules on the shared report engine | ✅ Shipped |
| Approval workflow (submit / approve / reject) | ✅ Shipped |
| Dashboard (overview, today, pending, approval summary, recent activity) | ✅ Shipped |
| Global search (reports + users + parameters) | ✅ Shipped |
| Unified pagination & filtering across all list endpoints | ✅ Shipped |
| Analytics (KPIs, trends, line/shift/operator performance) | ✅ Shipped |
| Attachments (upload / view / download) | ✅ Shipped |
| Notifications inbox | ✅ Shipped |
| System settings | ✅ Shipped |
| Integration center (config + execution history) | ✅ Shipped |
| Audit logs (read model) | ✅ Shipped |

## Report capabilities not in Version 1

| Capability | Version 1 status |
|---|---|
| ✓ Create report (stored as DRAFT) | Implemented |
| ✓ View / list / search / filter reports | Implemented |
| ✓ Submit / approve / reject with remarks | Implemented |
| ✓ Delete draft (SUPER_ADMIN only) | Implemented |
| ✗ Edit an existing draft | **Not supported** — no `PUT`/`PATCH` report endpoint |
| ✗ Resume / save draft changes later | **Not supported** — no update endpoint |
| ✗ Edit rejected report and resubmit | **Not supported** — no update/resubmit endpoint (⚠️ planned; see below) |

## 3. Future Features

Candidates for near-term work, before or alongside Version 2. These are
**not** committed; each is independently shippable and none requires a change
to the frozen report engine.

| Feature | Description | Backend note |
|---------|-------------|--------------|
| Report edit / resubmit | Allow an operator to edit a rejected report and resubmit it (completes the reject → edit → resubmit loop). Also enables editing/resuming existing drafts. | Currently **missing** — no `PUT`/`PATCH` report endpoint. The single gap found in the business-workflow verification (`VERIFICATION_REPORT.md`). Awaiting client decision on V1 vs post-V1 scope. |
| External notification channels | Deliver notifications out-of-band (email/SMS/push). | In-app notifications are already workflow-triggered via `NotificationChannel` (report submit/approve/reject, user created/welcome, password changed). Only external delivery is outstanding. |
| Write-path audit logging | Record audit entries for every lifecycle event (login, create, submit, approve, reject, edit). | Audit service is currently read-only. |
| Attachments per report entry | Attach files to individual report entries (currently files attach to a report as a whole). | |
| Parameter-level analytics | Analytics over individual parameter readings, not just report aggregates. | |
| Dashboard widgets per report type | Per-type widgets on the dashboard. | |
| Shift validation polish | `@NotNull` on shift times and optional overlap validation. | |

## 4. Version 2 Ideas

Longer-horizon enhancements. These are ideas, not commitments.

- **Notifications** — push / email / SMS delivery channels.
- **AI Insights** — anomaly detection and reading-trend analysis over report data.
- **ERP Integration** — live data exchange with ERP / MES systems.
- **Offline Sync** — capture reports without connectivity and sync later.
- **Advanced Analytics** — richer time-series and cross-report analytics.
- **Custom Report Builder** — let factories define new report types at runtime.
- **Versioned Templates** — history of template and parameter configuration
  changes.

## 5. Production Roadmap

Hardening items recommended before production deployment. These are
config/quality changes, not new features.

| # | Item | Current state | Target |
|---|------|---------------|--------|
| 1 | Database credentials | Hardcoded in `application.properties` | Externalized to environment/config |
| 2 | `jwt.secret` | Hardcoded in-source | Environment variable (already supported via `.env`) |
| 3 | `ddl-auto` | `update` | `validate` (Flyway owns the schema) |
| 4 | SQL logging | `show-sql=true`, Hibernate `SQL`=DEBUG / `bind`=TRACE | Gated behind a debug profile |
| 5 | Connection pool | No explicit HikariCP settings | Configure max/min/queue size and timeouts |
| 6 | Test coverage | 1 Spring-context test | Controller / service / unit tests for core workflows |
| 7 | Observability | No metrics or health endpoint | `spring-boot-starter-actuator` + Micrometer |
| 8 | Default credentials | Seeded `ADMIN001` / `admin123` | Change on first production login |
| 9 | Environment profiles | Single `application.properties` | `dev` / `prod` profiles |
| 10 | CORS | Default configuration | Explicit allowed origins |
| 11 | Rate limiting | None | Brute-force protection on `/api/auth/login` |

---

*Roadmap status: Version 1 is feature-complete for the shipped scope. Report
edit/resume and reject → edit → resubmit are **not** in V1 (see §2 capability
table). Nothing in this document represents a committed delivery date.*
