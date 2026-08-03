# CED Operations — System Blueprint

> Version 1 — Predefined Reports Only

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

Version 1 supports **only predefined (standard) reports**. Users cannot create
custom report types, and there is **no generic report builder**.

Supported report types:

| Code | Report |
|------|--------|
| `PROCESS_MONITORING` | Process Monitoring |
| `CHEMICAL_CONSUMPTION` | Chemical Consumption |
| `DAILY_STARTUP` | Daily Startup Checklist |
| `DAILY_INSPECTION` | Daily Inspection |
| `FIRST_PIECE_INSPECTION` | First Piece Inspection |
| `PDI` | Pre Delivery Inspection |

Each report type **directly owns** the parameters (fields) that must be captured.
There is **no intermediate Process concept**.

> **Implementation status:** all six predefined report types are implemented on
> the shared report engine (`AbstractReportService` / `BaseReportMapper` /
> `ReportTypeMetadata`) and are **completed / frozen**.

> **Version 1 report capabilities:** ✓ create (stored as DRAFT), ✓ view/list/
> search/filter, ✓ submit, ✓ approve/reject with remarks, ✓ delete draft
> (SUPER_ADMIN only). ✗ **Edit existing draft / resume / save draft changes** and
> ✗ **edit rejected + resubmit** are **not supported** — there is no report
> update endpoint. See the lifecycle note in §2 and `FEATURES_ROADMAP.md`.

```
Report Type ──1:N──► Parameter
```

## 2. Business Workflow

### Phase 1 — Initial Setup (Super Admin)

1. Super Admin logs in.
2. Super Admin creates **Users**, **Shifts**, **Lines**.
3. Super Admin selects one of the predefined **Report Types**.
4. Super Admin configures the **Parameters** required by that report.

Example — Chemical Consumption:

```
Bath Temperature
Paint Added
UF Conductivity
Voltage
Remarks
```

Example — Daily Startup Checklist:

```
Pump Check
Air Pressure
Emergency Stop
Conveyor
Remarks
```

No additional process configuration exists. The report type itself represents the
business process.

### Phase 2 — Daily Operation (Staff)

1. Staff logs in.
2. Staff selects **Report Type**.
3. Staff selects **Production Line**.
4. **Shift is determined automatically** by the backend from the current
   timestamp (overnight shifts supported, e.g. `22:00 → 06:00`).
5. Backend loads all configured parameters for that report type.
6. Staff fills observations.
7. Staff **Saves Draft** or **Submits**.

### Phase 3 — Approval (Admin)

1. Admin reviews submitted reports.
2. Admin **Approves** or **Rejects**.
3. ⚠️ **Not implemented in V1:** if rejected, staff **edits** and **resubmits**.
   There is no report update endpoint — `REJECTED` is terminal in the current
   backend. See `CURRENT_STATE.md`.

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

### Master Data

- **User** — staff with a role (`SUPER_ADMIN`, `ADMIN`, `OPERATOR`).
- **Shift** — `name`, `startTime`, `endTime`, `active`. Used for automatic shift
  detection (including overnight shifts).
- **Line** — production line master data.
- **Parameter** — a field owned by exactly one report type. Carries
  `parameterName`, `minValue`, `maxValue`, `unit`, `testMethod`, `frequency`,
  `inputType`, `mandatory`, `visible`, `defaultValue`, `displayOrder`, `active`,
  and `reportType`. Together the parameters of a report type form its **report
  template**, configured by Super Admin (Feature Completion — Report Template
  Configuration).
- **ReportType** — a fixed enumeration of the six predefined report types.

### Reports

Every report extends the same base structure:

```
report_number, report_type, report_date, shift_id, line_id, status,
created_by, approved_by, approved_at, remarks
```

Each report has a set of **entries**:

```
parameter_id, observed_value, inspection_result, remark
```

Status lifecycle:

```
DRAFT → SUBMITTED → APPROVED
                ↘ REJECTED   (terminal in V1 — edit + resubmit NOT implemented)
```

> ⚠️ **V1 limitation:** a report is created in one step (stored as `DRAFT`) and
> has no update endpoint. Drafts cannot be edited/resumed, and a `REJECTED`
> report has no transition back to `SUBMITTED`. The intended `REJECTED →
> (edit + resubmit) → SUBMITTED` loop is a post-V1 roadmap item (§8).

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
│   ├── parameter/   # Parameter master (owned by report type)
│   └── reporttype/  # Fixed report type catalog (read-only)
├── report/
│   ├── chemical/               # Chemical Consumption
│   ├── processmonitoring/      # Process Monitoring
│   ├── dailystartup/           # Daily Startup Checklist
│   ├── dailyinspection/        # Daily Inspection
│   ├── firstpieceinspection/   # First Piece Inspection
│   ├── predeliveryinspection/  # Pre Delivery Inspection
│   ├── dashboard/              # Dashboard summaries
│   └── globalsearch/           # Report search + unified search (reports/users/parameters)
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
| `parameter_master` | `report_type`, `parameter_name`, `min_value`, `max_value`, `unit`, `test_method`, `frequency`, `input_type`, `mandatory`, `visible`, `default_value`, `display_order`, `active` |

> `process_master` has been **removed**. Parameters belong directly to a report
> type via `parameter_master.report_type`.

Report tables (one pair per predefined report type):

| Report | Report table | Entry table |
|--------|--------------|-------------|
| Process Monitoring | `process_monitoring_reports` | `process_monitoring_entries` |
| Chemical Consumption | `chemical_consumption_reports` | `chemical_consumption_entries` |
| Daily Startup | `daily_startup_reports` | `daily_startup_entries` |
| Daily Inspection | `daily_inspection_reports` | `daily_inspection_entries` |
| First Piece Inspection | `first_piece_inspection_reports` | `first_piece_inspection_entries` |
| PDI | `pre_delivery_inspection_reports` | `pre_delivery_inspection_entries` |

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
| Parameters | `/api/parameters` (by report type: `GET /api/parameters/report-type/{type}`) |
| Report types | `GET /api/report-types` (fixed catalog) |
| Reports | `/api/reports/chemical-consumption`, `/api/reports/process-monitoring`, `/api/reports/daily-startup`, `/api/reports/daily-inspection`, `/api/reports/first-piece-inspection`, `/api/reports/pre-delivery-inspection` |
| Dashboard | `/api/reports/dashboard` |
| Search | `/api/reports/search` (report-only) · `/api/search` (unified reports + users + parameters) |
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

Applied to: Users, Parameters, and all six report listings (which double as the
dashboard report history). Endpoints are **backward compatible**: without any
paging/filter param they return the legacy full list; supplying params returns a
`PageResponse<T>`. See `API_DOCUMENTATION.md` section 4.

See `API_DOCUMENTATION.md` for full request/response details.

## 7. Security & Roles

- `SUPER_ADMIN` — full access, manages master data, approves reports.
- `ADMIN` — manages master data, approves/rejects reports.
- `OPERATOR` — logs in, creates/submits reports.

JWT access + refresh tokens. RBAC enforced via `@PreAuthorize`.

## 8. Future Roadmap (post V1)

- Report **edit / resubmit** for rejected reports, plus **draft edit/resume**
  (backend enablers planned — no update endpoint exists in V1).
- Attachments per report entry.
- Dashboard widgets per report type.
- Parameter-level analytics.
- Notifications for approval workflow.
- Integration with external systems.
- (Out of scope by design) Custom report types and a generic report builder.
