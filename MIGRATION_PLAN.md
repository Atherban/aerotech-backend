# Migration Plan — Module-Driven Report Architecture (Clean Redesign)

> **Status:** REVISED PLAN ONLY — awaiting approval before any implementation.
> No code has been generated.
>
> **Supersedes:** the previous backward-compatible migration strategy in the
> earlier `MIGRATION_PLAN.md`. This project has NOT reached production. There is
> **no compatibility layer, no legacy adapter, no alias endpoint**. The
> ReportType architecture is removed entirely and replaced by a single
> configuration-driven Report domain.

---

## 0. Executive Summary

Replace the hardcoded six-report-type architecture with a fully
configuration-driven model:

```
Module Type → Module → Process → Process Parameter → Report → Report Process Progress → Recorded Values
```

- **Module Type** — configurable master data (Super Admin CRUD), seeded with
  Production, Quality, Maintenance, Costing. Organizations may add more (e.g.
  Warehouse, Logistics, Safety, HR). **Not hardcoded.**
- **Module** — configurable master data (Super Admin CRUD). A **Module is a
  reusable report Template**. Replaces the six hardcoded report types (Chemical
  Consumption, Process Monitoring, Daily Startup, First Piece Inspection, PDI,
  Daily Inspection become *data rows*, not code). Lifecycle:
  `DRAFT → ACTIVE → ARCHIVED` (archived modules remain readable for historical
  reports).
- **Template Versioning** — mandatory. A Module's Processes + Process
  Parameters are versioned together. After a Module changes, existing reports
  keep the old version; new reports use the latest version. Historical reports
  are **never mutated**. Exactly the specification that existed at report
  creation is always referenced.
- **Process** — ordered child of a Module. `displayOrder` is the **only** source
  of ordering.
- **Process Parameter** — a global reusable **Parameter** configured per process
  (displayOrder, mandatory, visible, defaultValue, unit, min, max, active).
- **Report** — single generic table; belongs to a Module. **One table for all
  modules** (no per-module report tables).
- **Report Process Progress** — server-side workflow state: current process,
  completed processes, remaining, percentage complete, status.
- **Recorded Values** — single table of observed values, keyed to
  (report process, process parameter).

The Report Engine is **kept as a concept but redesigned around Modules**: one
generic `ReportEngine` that serves *any* Module from configuration alone — no
new Java code required to add a Module.

---

## 1. Architecture Impact Analysis

### 1.1 The complete inventory of ReportType coupling (everything that goes away)

| Artifact | Today | Disposition |
|----------|-------|-------------|
| `common/enums/ReportType.java` | enum of 6 hardcoded types | **DELETE** |
| `report/support/ReportTypeMetadata.java` | per-type label/prefix/roles/template | **DELETE** |
| `master/reporttype/controller/ReportTypeController.java` + `ReportTypeResponse` | fixed catalog endpoint | **DELETE** |
| `report/{processmonitoring,chemical,dailyinspection,dailystartup,firstpieceinspection,predeliveryinspection}/**` (6 modules × controller, service, mapper, entity×2, repository×2, dto request×4, dto response×2) | per-type report stacks | **DELETE** |
| `BaseReport.reportType` field | type discriminator | **DELETE** (field; BaseReport replaced by single `Report`) |
| `common/util/ReportNumberGenerator.java` | prefixes from `ReportTypeMetadata` | **REWRITE** — generate from Module's configurable `prefix` |
| `master/parameter/**` (`ParameterMaster` + `report_type` column) | report-type-scoped parameter spec | **REPLACE** with `parameter` (global) + `process_parameter` (per-process spec) |
| `report/support/AbstractReportService.java` + `BaseReportMapper.java` | per-type-generic engine | **REDESIGN** into a single generic `ReportEngine` |
| `report/support/ReportFilterRequest.java` | filters by `reportType` | **REWRITE** — filter by `moduleId` |
| `report/dashboard/service/DashboardService.java` | 6-table SQL `UNION`s | **REWRITE** — single `reports` table + module join |
| `report/globalsearch/util/{GlobalSearch,UnifiedSearch}QueryBuilder.java` + services | hardcoded 6-table list + `report_type` columns | **REWRITE** — single `reports` table |
| `analytics/service/AnalyticsService.java` | 6-table unions + `report_type` grouping | **REWRITE** — single table |
| `notification/.../NotificationChannel` call sites (reportType().name()) | module string in notifications | **ADJUST** — send module name/code |

### 1.2 What is kept (unchanged)

- **Module-independent domains:** users, roles, auth/JWT, shifts (incl. auto
  detection), lines, attachments, integrations, settings, audit logs,
  notifications infrastructure (`NotificationChannel`, `Notification`,
  `NotificationType`), `ValidationService`, pagination/spec builders.
- **Enums that stay:** `ReportStatus` (DRAFT/SUBMITTED/APPROVED/REJECTED),
  `InspectionResult`, `InputType`. (`InspectionFrequency` re-evaluated — see §9.)

### 1.3 Key design decisions

1. **One generic Report table, not one per module.** Modules are data; reports
   must therefore also be uniform data. The six entry tables collapse into one
   `recorded_value` table.
2. **No new Java per module.** Adding "Chemical Dosing" = inserting a Module
   row (+ processes + process parameters). Zero code changes.
3. **`displayOrder` is authoritative.** The engine always loads processes with
   `ORDER BY display_order`; progress is computed from that order. Never inferred
   from names or insertion.
4. **Progress is server-side.** The frontend never computes workflow state; it
   renders what `GET /reports/{id}` / `/current-process` return.

---

## 2. Database Migration Plan

### 2.1 Migration history strategy

The project is **pre-production** with no live data. Flyway + `ddl-auto=update`
is in use. Two options:

- **Option A (recommended): reset migration history.** Re-author the Flyway set
  into a clean `V1…Vn` (users/roles/auth, shifts/lines, module hierarchy,
  reports, integrations, notifications/settings/audit). Old DBs are recreated.
  Cleanest expression of "remove the old architecture".
- **Option B:** keep V1–V8 and add a destructive `V9` that `DROP`s the old
  report/parameter tables and creates the new schema.

Recommend **Option A** to match the clean-architecture mandate; dev/demo
databases are disposable. (If a populated DB must be preserved, Option B is the
fallback and would be documented accordingly.)

### 2.2 New schema (target)

```sql
-- Reference data (seeded, read-only)
module_type (
  id BIGSERIAL PK, name VARCHAR(100) UNIQUE NOT NULL, active BOOLEAN default true,
  created_at, updated_at)

-- Configurable master data
module (
  id BIGSERIAL PK,
  module_type_id BIGINT NOT NULL REFERENCES module_type(id),
  name VARCHAR(150) UNIQUE NOT NULL,
  prefix VARCHAR(10) UNIQUE NOT NULL,          -- report-number prefix (e.g. CCR)
  description VARCHAR(300),
  display_order INT NOT NULL, active BOOLEAN default true,
  created_at, updated_at)

process (
  id BIGSERIAL PK,
  module_id BIGINT NOT NULL REFERENCES module(id),
  name VARCHAR(150) NOT NULL, description VARCHAR(300),
  display_order INT NOT NULL, active BOOLEAN default true,
  created_at, updated_at,
  UNIQUE(module_id, name))

-- Global reusable parameters (one definition per name)
parameter (
  id BIGSERIAL PK, name VARCHAR(150) UNIQUE NOT NULL,
  input_type VARCHAR(30) NOT NULL,             -- NUMBER/TEXT/BOOLEAN/DROPDOWN
  created_at, updated_at)

-- Per-process specification of a global parameter
process_parameter (
  id BIGSERIAL PK,
  process_id BIGINT NOT NULL REFERENCES process(id),
  parameter_id BIGINT NOT NULL REFERENCES parameter(id),
  display_order INT NOT NULL, mandatory BOOLEAN default true,
  visible BOOLEAN default true, default_value VARCHAR(255),
  unit VARCHAR(30), minimum_value NUMERIC(10,2), maximum_value NUMERIC(10,2),
  active BOOLEAN default true, created_at, updated_at,
  UNIQUE(process_id, parameter_id))

-- Generic report (one table for every module)
report (
  id BIGSERIAL PK,
  module_id BIGINT NOT NULL REFERENCES module(id),
  report_number VARCHAR(255) UNIQUE NOT NULL,
  report_date DATE NOT NULL,
  shift_id BIGINT NOT NULL REFERENCES shifts(id),
  line_id BIGINT NOT NULL REFERENCES line_master(id),
  status VARCHAR(255) NOT NULL,
  created_by BIGINT NOT NULL REFERENCES users(id),
  approved_by BIGINT REFERENCES users(id), approved_at TIMESTAMP,
  remarks VARCHAR(1000),
  created_at, updated_at)

-- Server-side workflow progress
report_process_progress (
  id BIGSERIAL PK,
  report_id BIGINT NOT NULL REFERENCES report(id),
  process_id BIGINT NOT NULL REFERENCES process(id),
  process_order INT NOT NULL,                  -- snapshot of displayOrder at completion
  completed_at TIMESTAMP NOT NULL, completed_by BIGINT NOT NULL REFERENCES users(id),
  created_at, updated_at,
  UNIQUE(report_id, process_id))

-- Recorded values (one entry per process parameter per report process)
recorded_value (
  id BIGSERIAL PK,
  report_id BIGINT NOT NULL REFERENCES report(id),
  process_id BIGINT NOT NULL REFERENCES process(id),
  process_parameter_id BIGINT NOT NULL REFERENCES process_parameter(id),
  observed_value VARCHAR(200) NOT NULL,
  inspection_result VARCHAR(255) NOT NULL,
  remark VARCHAR(500),
  created_at, updated_at,
  UNIQUE(report_id, process_id, process_parameter_id))
```

Indexes on `report(module_id, report_date)`, `report(status)`,
`report(shift_id)`, `report(line_id)`, `report(created_by)`,
`report_process_progress(report_id)`, `recorded_value(report_id, process_id)`.

### 2.3 Seed data

- **Module Types:** seed the initial set `Production`, `Quality`, `Maintenance`,
  `Costing` (configurable — Super Admin adds more, e.g. Warehouse, Logistics,
  Safety, HR).
- **Modules:** re-create the former report types as data rows under a module
  type (e.g. Chemical Consumption → Production; First Piece Inspection → Quality;
  PDI → Quality; Process Monitoring → Production/Quality per assignment):
  `Chemical Consumption` (CCR), `Process Monitoring` (PMR), `Daily Startup`
  (DSR), `Daily Inspection` (DIR), `First Piece Inspection` (FPI),
  `Pre Delivery Inspection` (PDI). Each seeded with a **v1 template**.
- **Template versions:** each seeded Module gets its first `ACTIVE` template
  version (v1). All processes/process parameters belong to a version.
- **Processes:** seed each module's v1 with one or more processes matching the
  real production flow (e.g. Process Monitoring → Shot Blasting, Alkaline
  Cleaner Tank, CED Coating, Final Inspection). Config-driven going forward.
- **Parameters:** create the global reusable set (`Temperature`, `Voltage`,
  `Current`, `Pressure`, `Bath Temperature`, `Conductivity`, …) from the former
  `parameter_master` names, deduplicated.
- **Process Parameters:** bind the old `parameter_master` specs to the v1
  processes (copy min/max/unit/display_order/mandatory/visible/default).

> Since the DB is recreated (Option A), seed data lives in the migration set or
> a `data.sql`/`CommandLineRunner` — no backfill of live rows required.

---

## 3. Entity Relationship Diagram

```
module_type (1) ──< (N) module (1) ──< (N) process (1) ──< (N) process_parameter (N) ──> (1) parameter

module (1) ──< (N) report (1) ──< (N) report_process_progress (N) ──> (1) process
report (1) ──< (N) recorded_value (N) ──> (1) process_parameter
```

Lifecycle: `Module Type → Module → Process → Process Parameter` is pure
configuration. `Report → Report Process Progress → Recorded Values` is the
runtime workflow data. Nothing is per-module.

---

## 4. API Impact

### 4.1 Configuration master-data APIs

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/module-types` | list module types | authenticated |
| GET | `/api/module-types/{id}` | module type detail | authenticated |
| POST | `/api/module-types` | create module type | SUPER_ADMIN |
| PUT | `/api/module-types/{id}` | edit module type | SUPER_ADMIN |
| POST | `/api/module-types/{id}/activate` / `/deactivate` | toggle module type | SUPER_ADMIN |
| GET | `/api/modules` | list modules (with module type, process count) | authenticated |
| GET | `/api/modules/{id}` | module detail + templates versions + processes | authenticated |
| POST | `/api/modules` | create module (initial template) | SUPER_ADMIN |
| PUT | `/api/modules/{id}` | edit module (header only; processes via versioning) | SUPER_ADMIN |
| POST | `/api/modules/{id}/activate` / `/deactivate` / `/archive` | module lifecycle DRAFT/ACTIVE/ARCHIVED | SUPER_ADMIN |
| POST | `/api/modules/{id}/versions` | publish a new template version | SUPER_ADMIN |
| GET | `/api/modules/{id}/latest-version` | latest ACTIVE template version | authenticated |
| GET | `/api/modules/{moduleId}/versions/{versionId}/processes` | ordered processes of a version | authenticated |
| POST | `/api/modules/{moduleId}/versions/{versionId}/processes` | add process (with displayOrder) | SUPER_ADMIN |
| PUT | `/api/modules/{moduleId}/versions/{versionId}/processes/{processId}` | edit process (incl. reorder) | SUPER_ADMIN |
| POST | `/api/processes/{id}/activate` / `/deactivate` | toggle process | SUPER_ADMIN |
| GET | `/api/parameters` | global reusable parameters | authenticated |
| POST | `/api/parameters` | create reusable parameter | SUPER_ADMIN |
| PUT | `/api/parameters/{id}` | edit parameter | SUPER_ADMIN |
| GET | `/api/processes/{id}/parameters` | process parameter form (ordered) | authenticated |
| POST | `/api/processes/{id}/parameters` | bind parameter + spec | SUPER_ADMIN |
| PUT | `/api/process-parameters/{id}` | edit per-process spec | SUPER_ADMIN |

### 4.2 Report workflow APIs (single generic engine)

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/reports` | start: `{moduleId, reportDate, shiftId?, lineId, remarks?}` → creates DRAFT, auto-detects shift, returns report + progress + first process form |
| GET | `/api/reports/{id}` | report + full server-side progress (current/completed/remaining/%/status) |
| GET | `/api/reports/{id}/current-process` | next process form (process_parameters ordered by displayOrder, only `visible`), or 409 if none left |
| POST | `/api/reports/{id}/processes/{processId}` | save a process's values → persists recorded_values + marks progress; returns next process form + updated progress (report stays DRAFT) |
| POST | `/api/reports/{id}/save-next` | alias combining save + return next (same as above) |
| POST | `/api/reports/{id}/submit` | Save & Submit — 400 if any process incomplete; else SUBMITTED |
| GET | `/api/reports` | paginated/filtered list (`moduleId`, status, shift, line, date range, keyword) |
| POST | `/api/reports/{id}/approve` / `/reject` | SUBMITTED → APPROVED/REJECTED (SUPER_ADMIN/ADMIN) |
| DELETE | `/api/reports/{id}` | DRAFT only (SUPER_ADMIN) |

Progress response shape (server-computed; frontend renders only):

```json
{
  "reportId": 42, "module": {"id":1,"name":"Process Monitoring"},
  "status": "DRAFT", "currentProcess": {"order":2,"id":9,"name":"CED Coating"},
  "completedProcesses": ["Shot Blasting"], "remainingProcesses": 3,
  "processCount": 4, "completedCount": 1, "percentageComplete": 25
}
```

### 4.3 Removed / replaced endpoints (breaking by design)

- `GET /api/report-types` → **removed**; replaced by `GET /api/modules`.
- All six `/api/reports/{module}` type-specific endpoints (`process-monitoring`,
  `chemical`, `daily-startup`, `daily-inspection`, `first-piece-inspection`,
  `pre-delivery-inspection`) → **removed**; replaced by the single generic
  `/api/reports` API.
- Parameter APIs: `report_type` scoping removed → `/api/parameters` (global) +
  `/api/processes/{id}/parameters`.

---

## 5. Frontend Impact

- **Module picker** on dashboard → `GET /api/modules` (module-type aware,
  ordered by `displayOrder`). No hardcoded list.
- **Stepper workflow**: start report → load first process form → "Save & Next"
  → server returns next process → repeat → "Save & Submit" on final process.
  Frontend never shows all processes at once.
- **Parameter form** per process from `GET /api/reports/{id}/current-process`
  (respect `visible`, `mandatory`, `defaultValue`, `unit`; validation reflects
  per-process min/max).
- **Progress widget** renders server-provided `percentageComplete`,
  `currentProcess`, `completed/remaining` — no client-side state math.
- **Approval/review** screens: read single `/api/reports/{id}` (grouped by
  process) — no per-module DTOs.
- **Admin config UI** (Super Admin): module + process + process-parameter
  management screens.

---

## 6. Migration Strategy

Since the old architecture is being **removed**, the strategy is
**build-new-then-switch**, executed in one coherent change (not a live
side-by-side):

1. **Clean-slate schema** (Option A migration reset) that drops/replaces the
   report + parameter tables and introduces the module hierarchy.
2. **New master-data stack** (module type, module, process, parameter,
   process_parameter) with Super Admin CRUD.
3. **Generic `ReportEngine`** implementing start / progress / save / submit /
   approve / reject / delete / list against the single `report` +
   `report_process_progress` + `recorded_value` tables.
4. **Delete** every ReportType artifact (§1.1) and wire the single
   `/api/reports` controller.
5. **Rewrite** dashboard, global search, analytics, notification call-sites
   over the new tables/IDs.
6. **Seed** the former six modules + processes + parameters so the app is
   usable immediately.
7. **Update** all documentation + regenerate `api-docs.json`.

Because nothing in the codebase is production-critical yet, the switch is
atomic — no dual-write, no adapter, no alias.

---

## 7. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| Deletion scope is large (≈70 classes) | High | Medium | Enumerate precisely (§1.1); do deletions last, after new engine compiles; rely on compiler to surface dangling references. |
| Dashboard/search/analytics native-SQL rewrites | Medium | High | Single `report` table simplifies all three to one table; verify with focused tests against the new SQL. |
| Engine genericism (no new Java per module) | Medium | High | Engine consumes configuration only; add module-driven ordering/progress unit tests before cutting over. |
| Ordering regressions | Low | Medium | `displayOrder` is the single source; engine always `ORDER BY display_order`; snapshot `process_order` in progress rows. |
| Shift auto-detection interplay with new create | Low | Medium | Reuse existing `ShiftService.findShiftFor` unchanged in the generic `start`. |
| Seed completeness for former modules | Medium | Low | Seed all six former modules + realistic processes/parameters so UI demos work out of the box. |
| API consumers (frontend) rework | High (expected) | Medium | Documented breaking API in `API_DOCUMENTATION.md`; this is the intended outcome pre-production. |

---

## 8. Implementation Order (Approved Phases)

This migration is too large for one step. Work in **small, compile-safe phases**.
Every phase ends with: **1) compile, 2) run tests, 3) update documentation,
4) provide a migration summary — then STOP.** Never begin the next phase
automatically.

| Phase | Scope |
|-------|-------|
| **1** | **Domain & Database** — ModuleType, Module, Process, Parameter, ProcessParameter, Versioning entities, Flyway migration. STOP. |
| **2** | **Master Data APIs** — CRUD, validation, pagination, filtering, Swagger, tests. STOP. |
| **3** | **Generic Report Engine** — generic entities, repositories, DTOs, workflow, progress tracking, SaveNext, Submit. STOP. |
| **4** | **Dashboard / Search / Analytics / Notifications** — refactor to the generic engine. STOP. |
| **5** | **Remove legacy architecture** — delete ReportType, ReportTypeMetadata, old report modules, controllers, DTOs, repos, entities, services, migrations. STOP. |
| **6** | **Documentation** — rewrite README, PROJECT_BLUEPRINT, BUSINESS_FLOW, API_DOCUMENTATION, CURRENT_STATE, FEATURES_ROADMAP; regenerate Swagger. STOP. |

Rules:
- Do not implement more than one phase.
- Do not skip phases.
- **Do not start deleting old code until the replacement is implemented and verified.**
- Each phase ends with compile → tests → docs → summary, then stops.

### Implementation status

| Phase | Status |
|-------|--------|
| **1** — Domain & Database | ✅ **COMPLETE** — entities, enums, repos, `V9` migration; `ModuleDomainTest` (4) green. |
| **2** — Master Data APIs | ✅ **COMPLETE** — see summary below. |
| **3** — Generic Report Engine | ✅ **COMPLETE** — see summary below. |
| **4** — Dashboard / Search / Analytics | ✅ **COMPLETE** — see summary below. (Notifications deliberately **not** migrated.) |
| **5** — Remove legacy architecture | ✅ **COMPLETE** — legacy ReportType code + tables removed (`V12`); see summary below. |
| **6** — Documentation | ✅ **COMPLETE** — engine documented as the only architecture. |

**Phase 2 (complete) — what was delivered (all additive, split into a
`master/module` stack mirroring existing master-data conventions):**

- **Module Type APIs** — `/api/module-types` CRUD + deactivate + search.
- **Module (reusable report Template) APIs** — `/api/modules` CRUD + archive;
  list versions; **create version** (snapshots latest ACTIVE version's
  processes + process parameters into a new DRAFT, auto-incremented); **publish
  version** (DRAFT→ACTIVE, supersedes prior ACTIVE versions, activates module);
  list a version's processes and process-parameters.
- **Process APIs** — `/api/processes` CRUD + archive + search; the legacy
  `active` boolean is replaced by the `ProcessStatus` soft-lifecycle enum
  (DRAFT/ACTIVE/ARCHIVED).
- **ProcessParameter APIs** — `/api/processes/{id}/parameters` bind a global
  `Parameter` to a `Process`.
- **Global Parameter APIs** — `/api/module-parameters` CRUD for the global
  reusable parameter definitions.
- Repositories extend `JpaSpecificationExecutor` and reuse the shared
  `PageRequest` / `PageableResolver` / `SpecificationBuilder` pagination +
  filtering framework.
- Every response wrapped in the shared `ApiResponse<T>` envelope;
  `PageResponse<T>` for filtered lists; `Message`/`ResourceNotFound`/
  `BadRequest` exceptions; Lombok builders; Swagger annotations; RBAC
  (`SUPER_ADMIN`/`ADMIN` write, authenticated read).
- **Verification:** `./mvnw -o compile` BUILD SUCCESS; `ModuleDomainTest` (4) +
  `TemplateVersionServiceTest` (5) = **9 tests green**. No DB in this
  environment so the Postgres-backed `contextLoads` smoke test cannot run here.
- **STOPPED.** Phase 3 must not begin without explicit approval.

**Phase 3 (complete) — what was delivered (all additive; the legacy ReportType
engine is untouched and coexists until Phase 5):**

- New `report/engine` package with a **fully configuration-driven** engine:
  - **`ReportSession`** — work in progress; **freezes the module's latest ACTIVE
    template version at creation** (never switched mid-report); tracks module,
    frozen template version, `currentProcess`, `startedAt`,
    `completedProcessCount`, `status`, createdBy.
  - **`RecordedProcess`** — a template process recorded in the session, storing
    a **process-order snapshot** (`processOrderSnapshot`) for historical
    correctness.
  - **`RecordedValue`** — one value per process parameter, **grouped under its
    `RecordedProcess`** (never flattened); references `processParameterId` +
    `parameterId` (decision 9).
  - **`CompletedReport`** — the submitted report produced by **Save & Submit**.
  - Enums `ReportSessionStatus` (`IN_PROGRESS`/`COMPLETED`/`CANCELLED`) and
    `RecordedProcessStatus`; Flyway `V10__Generic_report_engine.sql`.
- **Workflow endpoints** under `/api/report-engine`: `POST /start`,
  `GET /sessions/{id}`, `GET /sessions/{id}/current`,
  `POST /sessions/{id}/save-next`, `POST /sessions/{id}/save-submit`,
  `GET /sessions/{id}/recorded`, `GET /reports/{id}`, `GET /reports/my`,
  `GET /sessions/my`.
- **Backend-authoritative navigation:** the frontend renders only the returned
  `ReportProcessStep`; the next process is always chosen by `displayOrder`.
- **Verification:** `./mvnw -o compile` BUILD SUCCESS; **16 tests green**
  (`ModuleDomainTest` 4 + `TemplateVersionServiceTest` 5 +
  `GenericReportEngineServiceTest` 7) covering template freeze, version
  rejection, save-and-next, save-and-submit, mandatory validation, snapshot
  order and session guards.
- **STOPPED.** Phase 4 (dashboard/search/analytics/notifications) must not begin
  without explicit approval.

**Phase 4 (complete) — business-facing read models migrated to the Generic
Report Engine (Dashboard, Global Search, Analytics; coexistence phase):**

- **Snapshots (`V11__Report_engine_snapshots.sql`, additive):**
  - `CompletedReport` (`report`) now carries immutable snapshots so historical
    reports stay readable after master-data changes: `module_name`,
    `module_prefix`, `template_version_number`, `module_type_id`,
    `module_type_name`, `shift_id`/`shift_name`, `line_id`/`line_name`, plus
    forward-compatible `approved_at`/`approved_by` (no approval workflow yet).
  - `RecordedValue` snapshots the spec in use at save time: `parameter_name`,
    `unit`, `input_type`, `minimum_value`, `maximum_value` — analytics derive
    **PASS/FAIL config-driven** (numeric observed value within the frozen
    min/max) instead of legacy inspection-entry results.
  - `report_session` captures optional `shiftId`/`lineId` at `start` (names
    resolved once) and carries them onto the completed report at submit.
- **Dashboard** (`/api/reports/dashboard`) rewritten to read **only** from the
  `report` table (CompletedReport) + its snapshots; legacy ReportType tables are
  no longer consulted. All 10 endpoints keep their exact response contracts:
  summary, reports-by-type (by module), by-shift, by-line, created-today,
  pending-approval, recent-reports, monthly-statistics, approval-summary,
  recent-activity.
- **Global Search** (`/api/search`) REPORT branch rewritten to query the engine
  `report` table; results now carry the module identity and support
  **Report Number, Module (moduleId), Module Type (moduleTypeId), User
  (employeeName), Shift, Line, Date, Status** filters with pagination and
  sorting preserved. USER/PARAMETER branches unchanged.
- **Analytics** (`/api/analytics`) rewritten over the engine tables
  (`report` + `recorded_value`/`recorded_process`/`report_session`), reusing the
  existing aggregation patterns. Report-level metrics (overview, quality
  approval/rejection, productivity, time trends, line/shift/operator
  performance) aggregate CompletedReport; entry-level metrics (quality pass/fail
  rates, chemical consumption sums, process monitoring stability) derive
  PASS/FAIL from the frozen `recorded_value` min/max snapshots. No legacy tables
  are consulted; no report-type-specific code.
- **Controller/DTO contracts unchanged** — legacy services' APIs are untouched;
  only their query sources moved to the engine.
- **Verification:** `./mvnw -o compile` BUILD SUCCESS; **24 unit tests green**
  (`ModuleDomainTest` 4, `TemplateVersionServiceTest` 5,
  `GenericReportEngineServiceTest` 10 — incl. shift/line capture, report
  snapshots, recorded-value snapshots, `UnifiedSearchQueryBuilderTest` 5). The
  Postgres-backed `contextLoads` smoke test cannot run in this environment (no
  DB) — unrelated to these changes.

**Phase 5 (complete) — legacy architecture removed**

Approved and executed. After migration verification, all obsolete ReportType
architecture was deleted:

- **Removed (104 Java files):** the six hardcoded report modules
  (`report/chemical`, `report/dailyinspection`, `report/dailystartup`,
  `report/firstpieceinspection`, `report/predeliveryinspection`,
  `report/processmonitoring`); `report/support` (`AbstractReportService`,
  `BaseReportMapper`, `ReportFilterRequest`, `ReportTypeMetadata`);
  `master/reporttype` (catalog endpoint); `master/parameter` (legacy
  report-type-based parameter catalog); the dead legacy search stack
  (`GlobalSearchController/Service/QueryBuilder`, `GlobalSearchRequest`,
  `GlobalSearchResultItem`, `SearchSuggestionsResponse`); legacy-only shared
  classes (`common/entity/BaseReport`, `common/service/ValidationService`,
  `common/util/ReportNumberGenerator`, `common/enums/ReportType`,
  `common/enums/InspectionResult`, `common/enums/InspectionFrequency`);
  `OpenApiConfig` legacy tags.
- **Modified:** `UnifiedSearchQueryBuilder` PARAMETER branch now reads the
  module architecture global `parameter` table; `UnifiedSearchQueryBuilderTest`
  updated; `V12__Remove_legacy_report_schema.sql` drops the six legacy
  report/entry tables and `parameter_master`.
- **Verification:** `./mvnw -o compile` BUILD SUCCESS; **24 unit tests green**
  (`ModuleDomainTest` 4, `TemplateVersionServiceTest` 5,
  `GenericReportEngineServiceTest` 10, `UnifiedSearchQueryBuilderTest` 5). The
  Postgres-backed `contextLoads` smoke test still requires a local DB.
- **Stopped.** Notifications migration and production hardening are separate,
  unapproved phases (not begun).

---

## 9. Architectural Decisions (Approved — Source of Truth)

The following decisions are approved and are the **authoritative** reference for
all implementation. Where this plan or the earlier strategy conflict with these
decisions, **these decisions win**.

1. **Module Types are NOT hardcoded.** Super Admin can create, edit, activate
   and deactivate Module Types. Current examples: Production, Quality,
   Maintenance, Costing. Future: Warehouse, Logistics, Safety, HR, etc. Module
   Types are configurable master data.
2. **Modules are Templates.** A Module is a reusable report template:
   `Module → Processes → Process Parameters → Report`. Every Report is an
   instance of a Module.
3. **Template Versioning is REQUIRED.** When a Module changes after reports
   exist: old reports keep the old template version; new reports use the latest
   version. **Never mutate historical reports.**
4. **Process Parameters are versioned together with the Module template.**
   Historical reports always reference the exact specification that existed
   when the report was created.
5. **displayOrder is the ONLY ordering mechanism.** Never infer order, never
   sort alphabetically. The Super Admin-configured order is always respected.
6. **Store a process-order snapshot for every completed process.** Future
   reordering must never affect historical reports.
7. **Progress is computed server-side.** Persist only the minimum required
   information. **Do not store derived values** (e.g. percentage). Compute and
   return percentage in responses if required.
8. **Module Prefix is configurable and unique** (e.g. PMR, CCR, FPI).
9. **RecordedValue references** `reportId`, `processId`, `parameterId`,
   `processParameterId` — simplifies analytics and debugging.
10. **Report entity carries workflow metadata**: `currentProcessId`,
    `completedProcessCount`, `startedAt`, `submittedAt`. Do not duplicate
    information unnecessarily.
11. **Module lifecycle** replaces the `active` boolean with `DRAFT`, `ACTIVE`,
    `ARCHIVED`. Archived modules remain available for historical reports.
12. **Frontend works as a wizard.** Backend workflow: Start Report → Load
    Current Process → Save Process → Return Next Process → repeat → Save &
    Submit. **The backend is authoritative; the frontend never computes
    workflow.**
