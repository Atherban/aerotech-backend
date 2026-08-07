# CED Operations — Current State

> Snapshot of the **current** implementation of the `ced-ops-backend` service.
> This document describes the system as it exists today — the
> **configuration-driven Generic Report Engine** is the only report
> architecture. There is no legacy report code, no per-report-type tables, and
> no report-type catalog. (Migration history lives in `MIGRATION_PLAN.md`.)

## Table of Contents

1. [Architecture](#1-architecture)
2. [Module Hierarchy (Configuration)](#2-module-hierarchy-configuration)
3. [Report Engine Workflow](#3-report-engine-workflow)
4. [Completed Features](#4-completed-features)
5. [Implemented APIs](#5-implemented-apis)
6. [Current Database Structure](#6-current-database-structure)
7. [Known Limitations](#7-known-limitations)
8. [Pending Features](#8-pending-features)
9. [Test Status](#9-test-status)

---

## 1. Architecture

- **Single generic data model.** Reports are instances of configurable
  **Modules**. No report type is hardcoded — everything a report contains is
  defined by master data.
- **Template versioning.** A Module's processes and process parameters are
  versioned together. `POST /api/report-engine/start` freezes the module's
  latest `ACTIVE` template version; historical reports always reference the
  exact specification in use when the session started.
- **Immutable snapshots.** Completed reports and recorded values carry frozen
  copies (module name/prefix, template version number, module type, shift/line
  id + name; parameter name/unit/inputType/min/max) so historical data stays
  correct after configuration changes.
- **Backend-authoritative navigation.** The engine advances a session one
  process at a time by `displayOrder`; the frontend renders only the step the
  server returns.
- **No per-type code.** Dashboard, unified search, and analytics read the
  engine tables directly (`report`, `recorded_process`, `recorded_value`).

## 2. Module Hierarchy (Configuration)

Configured through master-data APIs by `SUPER_ADMIN` / `ADMIN`:

```
Module Type
   ──1:N──► Module ──1:N──► Template Version ──1:N──► Process
                                                         └──1:N──► Process Parameter ──M:1──► Parameter (global)
```

| Level | Description | Lifecycle |
|-------|-------------|-----------|
| **Module Type** | A configurable category of reports (e.g. Production, Quality). | `active` (soft-deleted) |
| **Module** | A reusable report template with a unique **prefix** used in report numbers. | `DRAFT` / `ACTIVE` / `ARCHIVED` |
| **Template Version** | A versioned snapshot of a module's processes and process parameters. | `DRAFT` / `ACTIVE` / `SUPERSEDED` |
| **Process** | An ordered step within a template version (`displayOrder`). | `DRAFT` / `ACTIVE` / `ARCHIVED` |
| **Process Parameter** | Binds a global Parameter to a Process with `displayOrder`, `mandatory`, `visible`, `defaultValue`, `unit`, `minimumValue`, `maximumValue`. | `active` (soft-deleted) |
| **Parameter** | A global reusable field definition (`name`, `inputType`, `description`, `active`). | `active` (soft-deleted) |

**Template publishing:** creating a module auto-creates an initial DRAFT version
(v1). Creating a new version snapshots the latest `ACTIVE` version's processes
and bindings into a new DRAFT. **Publishing** a DRAFT activates it, supersedes
all other `ACTIVE` versions, and activates a `DRAFT` module.

**Ordering:** `displayOrder` is the only ordering mechanism — for processes
(step order) and for process parameters (field order). It is never inferred.

## 3. Report Engine Workflow

Executed via `/api/report-engine` (backend-authoritative):

```
Start
  └─► freezes latest ACTIVE template version; shift auto-detected (or shiftId/lineId supplied)
  └─► returns first Process step (ProcessParameterFields in displayOrder)
Save / Next   (per process)
  └─► validates mandatory visible fields (min/max, inputType)
  └─► records values under a RecordedProcess (process-order snapshot)
  └─► advances to the next Process by displayOrder
Save & Submit (final process)
  └─► records the final process, completes the session
  └─► creates a Completed Report (SUBMITTED) with immutable snapshots
```

Sessions persist work-in-progress: a `ReportSession` may be left `IN_PROGRESS`
and resumed (forward) later via its saved session id.

## 4. Completed Features

- **Authentication & authorization** — JWT access + refresh tokens, BCrypt,
  roles `SUPER_ADMIN` / `ADMIN` / `OPERATOR`.
- **User management** — create, update, activate/deactivate, delete, profile,
  change password.
- **Master data** — shifts (with automatic overnight-aware detection), lines,
  module types, modules (with template versioning), processes, process
  parameters, global parameters.
- **Generic report engine** — start, save/next, save/submit, sessions, recorded
  values, completed reports, my-sessions/my-reports.
- **Dashboard** — 10 endpoints over the engine `report` table.
- **Unified search** — `/api/search` across reports, users, and parameters.
- **Analytics & KPIs** — 9 endpoints (overview, quality, consumption, process
  stability, productivity, time trends, line/shift/operator performance).
- **Attachments** — upload (single/multiple), view, download, preview, update,
  delete, list.
- **Notifications** — in-app inbox (list, unread, count, mark read/read-all,
  delete).
- **System settings** — CRUD + categories + bulk update.
- **Integration center** — CRUD + enable/disable + test + execution history.
- **Audit logs** — read-only listing, statistics, recent (no writer yet).
- **Unified pagination & filtering** — `PageRequest`/`PageResponse` across all
  list endpoints.

## 5. Implemented APIs

| Area | Base path |
|------|-----------|
| Authentication | `/api/auth` |
| Users | `/api/users` |
| Shifts | `/api/shifts` (+ `GET /api/shifts/current`) |
| Lines | `/api/lines` |
| Module types | `/api/module-types` |
| Modules (+ template versions) | `/api/modules` |
| Processes | `/api/processes` |
| Process parameters | `/api/processes/{processId}/parameters` |
| Global parameters | `/api/module-parameters` |
| Report engine | `/api/report-engine` |
| Dashboard | `/api/reports/dashboard` |
| Unified search | `/api/search` |
| Analytics | `/api/analytics` |
| Attachments | `/api/attachments` |
| Notifications | `/api/notifications` |
| System settings | `/api/settings` |
| Integration center | `/api/integrations` |
| Audit logs | `/api/audit-logs` |

118 HTTP endpoints across 18 controllers. Every endpoint, DTO, request/response
shape, validation, and status code is documented in `API_DOCUMENTATION.md` and
reflected in the committed `api-docs.json` (OpenAPI 3 snapshot, kept in sync).

## 6. Current Database Structure

| Table | Purpose |
|-------|---------|
| `users` / `roles` / `refresh_token` | Identity, roles, auth tokens |
| `shifts` / `line_master` | Master data (shift detection; production lines) |
| `module_type` / `module` | Module hierarchy |
| `module_template_version` | Versioned templates (DRAFT/ACTIVE/SUPERSEDED) |
| `module_process` | Ordered process steps of a template version |
| `parameter` | Global reusable parameter definitions |
| `process_parameter` | Per-process parameter bindings (order, mandatory, visible, unit, min/max, default) |
| `report_session` | Work-in-progress report sessions (frozen template version, shift/line, status) |
| `recorded_process` | A recorded process within a session (process-order snapshot) |
| `recorded_value` | Observed values + frozen parameter spec (name/unit/inputType/min/max) |
| `report` | Completed reports (SUBMITTED) with immutable module/version/shift/line snapshots |
| `system_settings` / `notifications` / `attachments` / `integration*` / `audit_logs` | Supporting platform tables |

## 7. Known Limitations

- **No approval workflow.** A completed report is created `SUBMITTED`;
  approve/reject endpoints do not exist (`approved_at`/`approved_by` columns are
  forward-compatible but unused).
- **No report update / delete.** A completed report cannot be edited or deleted;
  a session cannot step backwards to re-record a process.
- **Audit logs are never written** by any code path (read model only).
- **`contextLoads` integration test** requires a local PostgreSQL instance to
  run (not available in the offline dev environment); all unit tests pass.
- **Production hardening pending** — see `FEATURES_ROADMAP.md` §5.

## 8. Pending Features

Not implemented; not scheduled. See `FEATURES_ROADMAP.md`.

- Approval workflow (approve / reject with remarks).
- Report edit / reject → resubmit.
- External notification channels (email/SMS/push).
- Write-path audit logging.
- Attachments per report entry.
- Parameter-level analytics and per-module dashboard widgets.

## 9. Test Status

`./mvnw -o test`:

| Test class | Tests | Status |
|------------|-------|--------|
| `master.module.entity.ModuleDomainTest` | 4 | ✅ |
| `master.module.service.TemplateVersionServiceTest` | 5 | ✅ |
| `report.engine.service.GenericReportEngineServiceTest` | 10 | ✅ |
| `report.globalsearch.util.UnifiedSearchQueryBuilderTest` | 5 | ✅ |
| `CedOpsBackendApplicationTests.contextLoads` | 1 | ⚠️ requires local Postgres |
| **Total** | **25** | **24 pass, 1 env-blocked** |

`./mvnw -o compile` → BUILD SUCCESS.