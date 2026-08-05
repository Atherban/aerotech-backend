# CED Operations — Current State

> Snapshot of the current implementation state of the `ced-ops-backend` service.
> The six predefined report modules are built on the **report engine** and are
> considered **frozen** (no further architectural refactoring unless explicitly
> requested).

## Table of Contents

- [Legend](#legend)
- [Report modules](#report-modules)
- [Shared / frozen infrastructure](#shared--frozen-infrastructure)
- [Report Template Configuration (completed)](#feature-completion--report-template-configuration-completed)
- [Dashboard APIs (completed)](#feature-completion--dashboard-apis-completed)
- [Unified Pagination & Filtering (completed)](#feature-completion--unified-pagination--filtering-completed)
- [Export — frontend-implemented (completed)](#decision--export-is-frontend-implemented-completed)
- [Global Search (completed)](#feature-completion--global-search-completed)
- [Business Workflow Verification (completed)](#business-workflow-verification-completed)
- [Other areas](#other-areas)
- [Not implemented in Version 1 (documented)](#not-implemented-in-version-1-documented)
- [Planned (documented, NOT implemented)](#planned-documented-not-implemented)

## Legend

- **Completed** — implemented, tested (build + smoke test pass), and documented.
- **Frozen** — architecture is stable; extend only by adding report-specific
  components (service/mapper/entities/DTOs), never by modifying the engine.

## Report modules

| Module | ReportType / code | Prefix | Status |
|--------|-------------------|--------|--------|
| Process Monitoring | `PROCESS_MONITORING` / `PMR` | `process-monitoring` | Completed / Frozen |
| Chemical Consumption | `CHEMICAL_CONSUMPTION` / `CCR` | `chemical-consumption` | Completed / Frozen |
| Daily Startup Checklist | `DAILY_STARTUP` / `DSR` | `daily-startup` | **Completed** |
| Daily Inspection | `DAILY_INSPECTION` / `DIR` | `daily-inspection` | Completed / Frozen |
| First Piece Inspection | `FIRST_PIECE_INSPECTION` / `FPI` | `first-piece-inspection` | Completed / Frozen |
| Pre-Delivery Inspection | `PDI` / `PDI` | `pre-delivery-inspection` | Completed / Frozen |

### Daily Startup Checklist — implementation summary

- Entities: `DailyStartupReport` (`daily_startup_reports`), `DailyStartupEntry`
  (`daily_startup_entries`).
- Engine reuse: `DailyStartupService extends AbstractReportService`,
  `DailyStartupMapper extends BaseReportMapper`, metadata in
  `ReportTypeMetadata` (`DAILY_STARTUP`, prefix `DSR`).
- Request DTOs: `CreateDailyStartupRequest`, `SubmitDailyStartupRequest`,
  `ApproveDailyStartupRequest`, `DailyStartupEntryRequest`.
- Response DTOs: `DailyStartupResponse`, `DailyStartupEntryResponse`.
- Repositories: `DailyStartupReportRepository`, `DailyStartupEntryRepository`.
- Controller: `DailyStartupController` at `/api/reports/daily-startup`
  (create / getAll / getById / submit / approve / reject / delete), RBAC per the
  shared report convention.
- Reuses `ValidationService`, `ReportNumberGenerator`, the approval workflow, and
  `ApiResponse`/`ApiError` envelopes exactly as other report modules.

## Shared / frozen infrastructure

| Component | Status |
|-----------|--------|
| `report/support/AbstractReportService` | Frozen |
| `report/support/BaseReportMapper` | Frozen |
| `report/support/ReportTypeMetadata` | Frozen |
| `report/support/ReportNumberGenerator` (via metadata) | Frozen |
| Common approval workflow / response mapping | Frozen |

## Feature Completion — Report Template Configuration (completed)

Super Admin can now fully configure the parameters that make up each predefined
report type's template. The existing `master/parameter` module was extended
(additive; no report module or engine change):

- New attribute **`visible`** — whether the parameter shows on the report entry
  form (default `true`).
- New attribute **`defaultValue`** — value pre-filled when the parameter renders.
- Entity `ParameterMaster`, DTOs (`CreateParameterRequest`,
  `UpdateParameterRequest`, `ParameterResponse`) and `ParameterMasterService`
  wired for the new attributes; `visible` is partial-update safe (null ignored)
  and `defaultValue` always applied.
- DB: `V7__Parameter_template_visible_default.sql` adds
  `parameter_master.visible BOOLEAN NOT NULL DEFAULT TRUE` and
  `parameter_master.default_value VARCHAR(255)`.
- API unchanged (still `/api/parameters` CRUD + `GET /api/parameters/report-type/{type}`),
  fully backward compatible. Documented in `API_DOCUMENTATION.md` (section 10).

All 11 template attributes are now supported: `reportType`, `parameterName`,
`displayOrder`, `inputType`, `mandatory`, `visible`, `unit`, `defaultValue`,
`minValue`, `maxValue`, `active`.

## Feature Completion — Dashboard APIs (completed)

All eight requested dashboard endpoints are implemented under
`/api/reports/dashboard` (native SQL over a UNION of the six report tables;
read-only, any authenticated user). Six already existed
(`/summary`, `/reports-created-today`, `/reports-pending-approval`,
`/reports-by-type`, `/reports-by-shift`, `/reports-by-line`,
`/recent-reports`, `/monthly-statistics`); this sprint added the two gaps:

- **`GET /api/reports/dashboard/approval-summary`** — `ApprovalSummaryResponse`
  (pending / approved / rejected totals, today's approved+rejected, approval rate).
  Reuses the same `STATUS_UNION` table set as `/summary`; adds one
  `status, approved_at` union for today's buckets — no duplicated queries.
- **`GET /api/reports/dashboard/recent-activity?limit=N`** — `RecentActivityResponse`
  lifecycle feed (CREATED from `created_at`/`created_by`, APPROVED/REJECTED from
  `approved_at`/`approved_by`), ordered newest first.

Reusable aggregation helpers added to `DashboardService`: `unionFor(projection)`,
`reportTables()`, and `activityUnion()` centralize the six-table UNION so future
aggregations do not repeat table lists. All DTOs are lightweight and mobile-friendly.

Smoke-tested live: all 10 dashboard endpoints return 200; `./mvnw -o test` green.
Documented in `API_DOCUMENTATION.md` (section 19).

## Feature Completion — Unified Pagination & Filtering (completed)

A reusable pagination + filtering framework now backs the system's list
endpoints. Single contract, no duplicated pagination logic:

- **`common/pagination/PageRequest`** — the single request DTO (`page`, `size`,
  `sortBy`, `sortDirection`, `keyword`). Module filters extend it:
  `UserFilterRequest`, `ParameterFilterRequest`, `ReportFilterRequest`.
- **`common/pagination/PageableResolver`** — builds Spring `Pageable`, clamps
  `size` to 200, resolves `sortBy` from a per-module whitelist with fallback.
- **`common/pagination/SpecificationBuilder`** — reusable JPA `Specification`
  builder (keyword / equality / range / IN).
- **`common/response/PageResponse<T>`** — the single paginated envelope
  (unchanged, now shared everywhere).

Applied to:

| Endpoint | Filter | Notes |
|----------|--------|-------|
| `GET /api/users` | `UserFilterRequest` | keyword + role + active, sort whitelist |
| `GET /api/parameters` | `ParameterFilterRequest` | keyword + reportType/inputType/active/visible |
| `GET /api/reports/{module}` (6) | `ReportFilterRequest` | reportNumber/status/shift/line/date-range/approved/keyword; also serves the dashboard report-history listings |

**Backward compatible:** no paging/filter params → the exact legacy full-list
response; any paging/filter param → `PageResponse<T>`. The frozen report engine
was extended **additively** (`AbstractReportService.doSearch`) alongside the
untouched `doGetAll()`; report repositories were not merged.

Verified: `./mvnw -o test` green; live smoke test of users/parameters/all six
report endpoints (paged + legacy + keyword + role + status + date-range); Swagger
exposes the new query params on each endpoint.

## Decision — Export is frontend-implemented (completed)

The backend provides **structured JSON APIs only**. PDF / Excel / CSV / print
export is implemented by the frontend client. Applied by removing the backend
export module:

- Deleted the `export/` package (`ExportController`, `ExportService`,
  `ExportStrategy` + CSV/XLSX/PDF impls, `ExportJob`, `ExportJobRepository`,
  `ExportRequest`, `ExportJobResponse`) and the `ExportFormat` enum.
- DB: `V8__Drop_export_jobs.sql` drops the `export_jobs` table (created in V5).
- Purged export-only enum values: `AuditAction.EXPORT`,
  `NotificationType.EXPORT_COMPLETED`, `AttachmentCategory.EXPORT_FILE` (no
  existing DB rows referenced them) and their Swagger `allowableValues`.
- Removed the Export tag / "export functionality" from `OpenApiConfig`.
- Docs updated throughout. No report engine, dashboard, or completed module
  behavior changed.

## Feature Completion — Global Search (completed)

A unified enterprise search experience over the shared pagination framework:

- **`GET /api/search`** — unified search across **reports, users, and parameters**
  (new `UnifiedSearchController`, `UnifiedSearchRequest extends PageRequest`,
  `UnifiedSearchQueryBuilder`, `UnifiedSearchService`, lightweight
  `UnifiedSearchResultItem`). Federates the six report tables + `users` +
  `parameter_master` into one UNION with a `type` discriminator, paginated via
  `PageResponse`, whitelisted sorting, optional filters: `type`, `keyword`,
  `reportNumber`, `reportType`, `status`, `employeeName`, `role`, `shiftId`,
  `lineId`, `dateFrom`, `dateTo`.
- **`GET /api/reports/search`** — existing report-only search (legacy, unchanged
  and fully backward compatible).
- **`GET /api/reports/search/suggestions`** — now also returns **parameter name**
  suggestions (new `parameters` field on `SearchSuggestionsResponse`; additive).

Verified: `./mvnw -o test` green; live smoke test of unified search (all types,
USER/REPORT/PARAMETER scoping, reportType+status, pagination, sorting,
employeeName, suggestions incl. parameters); Swagger exposes `/api/search` with
its full query-param surface.

## Business Workflow Verification (completed)

A full audit of every documented business action per role was performed against
the feature-complete backend (no code changes). Full detail in
`VERIFICATION_REPORT.md`.

| Role | Capabilities verified | Implemented | Coverage |
|------|-----------------------|-------------|----------|
| Super Admin | 19 | 19 | **100%** |
| Admin | 10 | 10 | **100%** |
| Staff (OPERATOR) | 14 | 13 | **92.9%** |
| **Overall** | **43** | **42** | **97.7%** |

- **Single missing capability:** report **edit/resubmit** — no
  `PUT`/`PATCH` on any of the 6 report controllers, so the reject → edit →
  resubmit loop (Blueprint Phase 3) cannot run end-to-end. This also means
  existing **drafts cannot be edited or resumed** — a report is created in one
  request (stored as `DRAFT`) and thereafter only submitted, approved, rejected,
  or deleted. Documented as a post-V1 roadmap item (Blueprint §8); consciously
  deferred.
- **Partially implemented:** audit trail is read-only (nothing writes
  `audit_logs`); notifications are written by report and user/auth workflows
  (via `NotificationChannel`) but there are no external channels (email/SMS/push);
  `CreateShiftRequest` times lack `@NotNull`; no
  `submittedBy`/`submittedAt`; report numbers use `count()+1` (race-prone);
  MapStruct declared but unused.
- **Production readiness gaps (non-blocking for frontend):** hardcoded DB creds
  + `jwt.secret`, `ddl-auto=update` (should be `validate`), verbose SQL logging,
  only 1 Spring-context test, no observability config, default credentials
  `ADMIN001/admin123`.
- **Recommendation:** **Ready for Frontend** — unless the client requires report
  edit/resubmit in V1 (then that one item is **Needs Backend Work**). Clarify
  with the client; the rest of the API is complete.

## Other areas

Auth, users, roles, master data (lines, shifts, parameters, report types),
dashboard, global search, analytics, integrations, attachments,
notifications, settings, audit-log — implemented; documented in
`API_DOCUMENTATION.md` and `PROJECT_BLUEPRINT.md`.

## Not implemented in Version 1 (documented)

| Capability | Status |
|---|---|
| Edit an existing draft / resume draft / save draft changes | ✗ **Not supported** — no `PUT`/`PATCH` report endpoint |
| Edit rejected report and resubmit (reject → edit → resubmit loop) | ✗ **Not supported** — no update/resubmit endpoint; the single business-workflow gap (see verification above) |

## Planned (documented, NOT implemented)

- none in progress