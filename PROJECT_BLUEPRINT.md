# CED Operations — System Blueprint

> Version 1 — Configuration-Driven Reporting

This document is the authoritative architecture reference for the CED Operations
Management System. It reflects the client-approved business workflow.

**Related documents:** [BUSINESS_FLOW.md](BUSINESS_FLOW.md) (business functional
specification), [API_DOCUMENTATION.md](API_DOCUMENTATION.md) (full API
reference), [CURRENT_STATE.md](CURRENT_STATE.md) (implementation snapshot),
[FEATURES_ROADMAP.md](FEATURES_ROADMAP.md) (roadmap).

---

## Table of Contents

1. [Business Model](#1-business-model)
2. [Business Workflow](#2-business-workflow)
3. [Domain Model](#3-domain-model)
4. [Architecture](#4-architecture)
5. [Database Design](#5-database-design)
6. [API Overview](#6-api-overview)
7. [Security & Roles](#7-security--roles)
8. [Future Roadmap (post V1)](#8-future-roadmap-post-v1)

---

## 1. Business Model

The Generic **Report Engine is configuration-driven**: report types are not
hardcoded. Super Admin composes them from the module hierarchy
`Module Type → Module → Template Version → Process → Process Parameter → (global)
Parameter`. A report is executed through the engine as a versioned template of
ordered processes, each with configured fields.

> **Implementation status:** the module-driven engine is **live and the only
> report architecture** (Phase 5 complete). The legacy hardcoded ReportType
> engine and its six predefined report tables were removed.

> **Report capabilities:** ✓ start a session (freezes the active template
> version), ✓ save one process at a time (server-advanced by `displayOrder`),
> ✓ save & submit (produces a completed `report`), ✓ view/list, ✓ search/filter,
> ✓ dashboard & analytics. Approval columns are forward-compatible. ✗ Report
> **edit / resume** and ✗ approval & edit-rejected+resubmit are **not yet
> implemented**. See the lifecycle note in §2, `CURRENT_STATE.md`, and
> `FEATURES_ROADMAP.md`.

```
Module Type ──1:N──► Module ──1:N──► Template Version ──1:N──► Process
    └──────── Process ──1:N──► Process Parameter ──M:1──► Parameter (global)
```

## 2. Business Workflow

### Phase 1 — Initial Setup (Super Admin)

1. Super Admin logs in.
2. Super Admin creates **Users**, **Shifts**, **Lines**.
3. Super Admin composes the **Module hierarchy**: a Module Type → a Module (with
   a prefix) → its template version → ordered Processes → per-process
   Parameters (bound from the global parameter catalog, with unit/min/max).

Example — a Process Monitoring module's processes:

```
1. Bath Setup        → Bath Temperature, Voltage, Conductivity
2. Chemical Make-up  → Chemical A, Chemical B
3. Line Check        → Remarks
```

### Phase 2 — Daily Operation (Staff)

1. Staff logs in.
2. Staff selects a **Module** (and optionally the **Line** and **Shift**).
3. **Shift is determined automatically** by the backend from the current
   timestamp when not supplied (overnight shifts supported).
4. Backend freezes the module's **latest ACTIVE template version** and presents
   the first process step.
5. Staff fills each process; the backend advances one step at a time and records
   the values grouped under the process.
6. Staff **Save & Submits** the final step → a **Completed Report** is created
   with immutable snapshots of the configuration used.

### Phase 3 — Approval (Admin)

Forward-compatible `approved_at` / `approved_by` columns exist on the completed
report; the approval workflow ships in a later phase. **Not implemented yet:**
approve/reject and the rejected→edit→resubmit loop (no report update endpoint).

### Phase 4 — Reporting

Approved reports are available for:

- Dashboard
- Analytics
- History
- PDF Export
- Excel Export

> **Export note:** PDF / Excel / CSV / print export is implemented by the
> frontend. The backend provides structured JSON APIs only — there is no backend
> export module.

## 3. Domain Model

### Module-Driven Master Data

- **User** — staff with a role (`SUPER_ADMIN`, `ADMIN`, `OPERATOR`).
- **Shift** — `name`, `startTime`, `endTime`, `active`. Used for automatic shift
  detection (including overnight shifts).
- **Line** — production line master data.
- **Module Type** — a configurable category of reports (e.g. Production,
  Quality); `active`.
- **Module** — a reusable report template; `module_type`, `name`, `prefix`
  (used in the report number), `status` (`DRAFT`/`ACTIVE`/`ARCHIVED`).
- **Template Version** — a versioned snapshot of a module's processes; `status`
  (`DRAFT`/`ACTIVE`/`SUPERSEDED`). Publishing a DRAFT activates it.
- **Process** — an ordered step (`displayOrder`) within a template version.
- **Process Parameter** — binds a global Parameter to a Process with
  `displayOrder`, `mandatory`, `visible`, `unit`, `min`/`max`, `default`.
- **Parameter** — a global reusable field definition (`name`, `input_type`,
  `description`, `active`).

### Reports (Generic Report Engine)

A report is executed through the engine:

```text
ReportSession (work in progress)
   └── freezes the module's template version at start
   └── records each Process (RecordedProcess) with a process-order snapshot
        └── each with RecordedValue entries (frozen parameter name/unit/min/max)
   └── Save & Submit → Completed Report (report) with module/version/shift/line
       snapshots + immutable spec
```

Status lifecycle of a session / completed report:

```text
IN_PROGRESS → COMPLETED (→ SUBMITTED report)
```

Completed reports carry `startedAt`/`submittedAt`/`status` and forward-compatible
`approvedAt`/`approvedBy`.

> ⚠️ **V1 limitation:** a completed report has no update endpoint — it cannot be
> edited/resumed, and approval (approve/reject) is not yet implemented. The
> `REJECTED → (edit + resubmit) → SUBMITTED` loop is a post-V1 roadmap item (§8).

## 4. Architecture

```
┌────────────────────────────────────────────────────────────┐
│                        HTTP / REST / JWT                    │
└────────────────────────────────────────────────────────────┘
  Controller  ──►  Service  ──►  Repository  ──►  PostgreSQL
      │               │              │
      └── DTOs ───────┘              └── Flyway migrations
```

Package layout (`com.aerotech.ced_ops_backend`):

```
├── auth/            # Authentication & authorization
├── common/          # Base entities, enums, exceptions, config
├── master/
│   ├── line/        # Line master
│   ├── shift/       # Shift master + automatic shift detection
│   └── module/      # Module-driven hierarchy: module types, modules,
│                    #   template versions, processes, global parameters
├── report/
│   ├── engine/      # Generic report engine (sessions + completed reports)
│   ├── dashboard/   # Dashboard summaries
│   └── globalsearch/ # Unified search (reports/users/parameters) on the engine
├── analytics/       # KPI analytics
├── user/            # User management
├── role/            # Role management
├── security/        # JWT filter + security config
└── ...              # audit, notification, attachment, settings, integration
```

## 5. Database Design

Master data:

| Table | Columns |
|-------|---------|
| `users` | `employee_id`, names, `mobile_number`, `password`, `active`, `role_id` |
| `roles` | `name`, `description` |
| `shifts` | `name`, `start_time`, `end_time`, `active` |
| `line_master` | `name`, `description`, `display_order`, `active` |

Module-driven master data tables (from `V9__Module_architecture_schema.sql`):

| Table | Columns |
|-------|---------|
| `module_type` | `name`, `description`, `active` |
| `module` | `module_type_id`, `name`, `prefix`, `description`, `status` (`DRAFT/ACTIVE/ARCHIVED`) |
| `module_template_version` | `module_id`, `version_number`, `status` (`DRAFT/ACTIVE/SUPERSEDED`), `change_note` |
| `module_process` | `template_version_id`, `name`, `description`, `display_order`, `status` |
| `parameter` | `name`, `input_type`, `description`, `active` (global reusable) |
| `process_parameter` | `process_id`, `parameter_id`, `display_order`, `mandatory`, `visible`, `default_value`, `unit`, `min/max`, `active` |

Report engine tables (from `V10__Generic_report_engine.sql`):

| Table | Columns |
|-------|---------|
| `report_session` | `module_id`, `template_version_id` (frozen), `current_process_id`, `started_at`, `completed_process_count`, `submitted_at`, `status`, `created_by`, `shift_id`/`shift_name`/`line_id`/`line_name` (captured at start) |
| `recorded_process` | `session_id`, `process_id`, `process_order_snapshot`, `status`, `completed_at` |
| `recorded_value` | `recorded_process_id`, `process_parameter_id`, `parameter_id`, `observed_value`, `parameter_name`, `unit`, `input_type`, `minimum_value`, `maximum_value` (frozen spec snapshots) |
| `report` (CompletedReport) | `report_number`, `module_id`, `template_version_id`, `started_at`, `submitted_at`, `status`, `created_by`, `session_id`, `module_name`, `module_prefix`, `template_version_number`, `module_type_id`/`module_type_name`, `shift_id`/`shift_name`, `line_id`/`line_name`, `approved_at`/`approved_by` (forward-compatible) |

> The business-facing read models — **Dashboard**, **Unified Search**, and
> **Analytics** — read exclusively from the engine tables above
> (`report`/`completed_report`, `recorded_process`, `recorded_value`). Historical
> reports stay readable via the immutable snapshots even when master data
> changes. The legacy per-type report tables and the report-type parameter
> catalog were dropped by migration `V12`.

Additional infrastructure tables: `refresh_token`, `system_settings`,
`notifications`, `attachments`, `audit_logs`.

### Shift detection

A shift is active at time `T` when (handles overnight shifts):

```
start = shift.startTime
end   = shift.endTime
active = (start <= end) ? (start <= T < end)
                       : (T >= start || T < end)   // wraps past midnight
```

If multiple active shifts match, the first by `startTime` wins. If no shift
matches, the shift with the earliest `startTime` is used as a fallback.

## 6. API Overview

| Area | Base path |
|------|-----------|
| Auth | `/api/auth` |
| Users | `/api/users` |
| Shifts | `/api/shifts` (+ `GET /api/shifts/current`) |
| Lines | `/api/lines` |
| Module-driven master data | Module types `/api/module-types` · Modules `/api/modules` (incl. template versions) · Processes `/api/processes` · Process parameters `/api/processes/{id}/parameters` · Global parameters `/api/module-parameters` |
| Report engine | `/api/report-engine` — `POST /start`, `GET /sessions/{id}`, `GET /sessions/{id}/current`, `POST /sessions/{id}/save-next`, `POST /sessions/{id}/save-submit`, `GET /sessions/{id}/recorded`, `GET /reports/{id}`, `GET /reports/my`, `GET /sessions/my` |
| Dashboard | `/api/reports/dashboard` |
| Search | `/api/search` (unified reports + users + parameters) |
| Analytics | `/api/analytics` |

> The backend provides structured JSON APIs only; PDF/Excel/CSV/print export is
> implemented by the frontend client. There is no `/api/exports` endpoint.

### Unified pagination & filtering

A shared framework (`common/pagination`) backs every list endpoint in the system:

- **`PageRequest`** — single request DTO (`page`, `size`, `sortBy`,
  `sortDirection`, `keyword`); module filter DTOs extend it.
- **`PageableResolver`** — builds Spring `Pageable`, caps `size` at 200, resolves
  `sortBy` from a per-module whitelist with safe fallback.
- **`SpecificationBuilder`** — reusable JPA `Specification` builder.
- **`PageResponse<T>`** — the single paginated envelope.

Applied to: Users, module-driven master data lists, and the engine's report/session
lists. Endpoints are **backward compatible**: without any paging/filter param
they return the full list; supplying params returns a `PageResponse<T>`. See
`API_DOCUMENTATION.md` section 4.

See `API_DOCUMENTATION.md` for full request/response details.

## 7. Security & Roles

- `SUPER_ADMIN` — full access, manages master data and the module hierarchy.
- `ADMIN` — manages master data and the module hierarchy.
- `OPERATOR` — logs in, starts/saves report sessions and submits reports.

JWT access + refresh tokens. RBAC enforced via `@PreAuthorize`.

## 8. Future Roadmap (post V1)

- Report **approve / reject** workflow (columns are forward-compatible) and
  **edit / resubmit** for rejected reports, plus **draft edit/resume**.
- Attachments per report entry.
- Dashboard widgets per module.
- Parameter-level analytics.
- Notifications for the report workflow.
- Integration with external systems.
- (Out of scope by design) Custom report types and a generic report builder.
