# CED Operations — Business Workflow Verification Report

> Version 1 · Backend feature-complete · Verification of every business action per
> role against the implemented backend. No code changes were made; this is an
> audit document. Companion to `CURRENT_STATE.md`.

---

## 1. Role — Super Admin

Business actions the Super Admin should perform (per `PROJECT_BLUEPRINT.md`
Phase 1 Initial Setup + Security & Roles), and their backend capability:

| # | Business action | Backend capability | DB | Entity | DTO | Repo | Service | Controller | Endpoint | Swagger | Validation | RBAC | Error handling | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | Log in | `POST /api/auth/login` | refresh_token | RefreshToken | LoginRequest/AuthResponse | RefreshTokenRepository | AuthService | AuthController | ✅ | ✅ | ✅ @NotBlank | public | ✅ | **Complete** |
| 2 | Create user | `POST /api/users` | users | User | CreateUserRequest | UserRepository | UserService | UserController | ✅ | ✅ | ✅ | SUPER_ADMIN | ✅ | **Complete** |
| 3 | Update user | `PUT /api/users/{id}` | users | User | UpdateUserRequest | UserRepository | UserService | UserController | ✅ | ✅ | ✅ | SUPER_ADMIN | ✅ | **Complete** |
| 4 | Delete user | `DELETE /api/users/{id}` | users | User | — | UserRepository | UserService | UserController | ✅ | ✅ | ✅ | SUPER_ADMIN | ✅ | **Complete** |
| 5 | Activate / deactivate user | `PATCH /api/users/{id}/status` | users | User | UpdateStatusRequest | UserRepository | UserService | UserController | ✅ | ✅ | ✅ @NotNull | SUPER_ADMIN | ✅ | **Complete** |
| 6 | List / view users | `GET /api/users` + `GET /api/users/{id}` | users | User | UserResponse/UserFilterRequest | UserRepository | UserService | UserController | ✅ | ✅ | ✅ | SUPER_ADMIN/ADMIN | ✅ | **Complete** |
| 7 | Create shift | `POST /api/shifts` | shifts | Shift | CreateShiftRequest | ShiftRepository | ShiftService | ShiftController | ✅ | ✅ | partial* | SUPER_ADMIN/ADMIN | ✅ | **Complete** |
| 8 | Update / delete shift | `PUT`/`DELETE /api/shifts/{id}` | shifts | Shift | UpdateShiftRequest | ShiftRepository | ShiftService | ShiftController | ✅ | ✅ | ✅ | PUT: S-A/ADMIN, DEL: SUPER_ADMIN | ✅ | **Complete** |
| 9 | Create line | `POST /api/lines` | line_master | Line | CreateLineRequest | LineRepository | LineService | LineController | ✅ | ✅ | ✅ | SUPER_ADMIN/ADMIN | ✅ | **Complete** |
| 10 | Update / delete line | `PUT`/`DELETE /api/lines/{id}` | line_master | Line | UpdateLineRequest | LineRepository | LineService | LineController | ✅ | ✅ | ✅ | PUT: S-A/ADMIN, DEL: SUPER_ADMIN | ✅ | **Complete** |
| 11 | Select predefined report type | `GET /api/report-types` | — (enum) | — | ReportTypeResponse | — | ReportTypeController (static) | ✅ | ✅ | ✅ | any auth | ✅ | **Complete** |
| 12 | Configure parameters per report type | `POST`/`PUT`/`DELETE /api/parameters` + `GET /api/parameters/report-type/{type}` | parameter_master | ParameterMaster | Create/UpdateParameterRequest | ParameterMasterRepository | ParameterMasterService | ParameterMasterController | ✅ | ✅ | ✅ | W: S-A/ADMIN, DEL: SUPER_ADMIN | ✅ | **Complete** |
| 13 | Approve / reject reports | `POST /api/reports/{module}/{id}/approve\|reject` (6 modules) | report tables | 6 × Report/Entry | Approve/Submit request DTOs | 12 report repos | AbstractReportService + 6 services | 6 controllers | ✅ | ✅ | ✅ @Valid | SUPER_ADMIN/ADMIN | ✅ | **Complete** |
| 14 | Delete reports | `DELETE /api/reports/{module}/{id}` (6) | report tables | 6 × Report | — | 6 report repos | AbstractReportService | 6 controllers | ✅ | ✅ | ✅ | SUPER_ADMIN | ✅ | **Complete** |
| 15 | View analytics | `GET /api/analytics/*` (9) | report tables | — | 12 response DTOs | — | AnalyticsService | AnalyticsController | ✅ | ✅ | ✅ | SUPER_ADMIN/ADMIN | ✅ | **Complete** |
| 16 | View audit logs | `GET /api/audit-logs*` (3) | audit_logs | AuditLog | AuditLogResponse/AuditFilterRequest | AuditRepository | AuditService | AuditController | ✅ | ✅ | ✅ | SUPER_ADMIN/ADMIN | ✅ | **Complete** |
| 17 | Manage system settings | `GET/POST/PUT/DELETE /api/settings*` (8) | system_settings | SystemSetting | Create/UpdateSettingRequest | SystemSettingRepository | SettingService | SettingController | ✅ | ✅ | ✅ | W: SUPER_ADMIN, R: S-A/ADMIN | ✅ | **Complete** |
| 18 | Manage integrations | `GET/POST/PUT/DELETE /api/integrations*` (9) | integrations + history | Integration + IntegrationExecutionHistory | Create/UpdateIntegrationRequest | IntegrationRepository + history repo | IntegrationService | IntegrationController | ✅ | ✅ | ✅ | SUPER_ADMIN | ✅ | **Complete** |
| 19 | Change own password | `PUT /api/users/change-password` | users | User | ChangePasswordRequest | UserRepository | UserService | UserController | ✅ | ✅ | ✅ | any auth | ✅ | **Complete** |

\* `CreateShiftRequest.startTime`/`endTime` have no `@NotNull` (only `@JsonFormat`); shift times may be null at the API level — acceptable (auto-detection falls back), noted under Nice-to-have.

**Super Admin coverage: 19/19 = 100%**

---

## 2. Role — Admin

Business actions the Admin should perform (per Phase 3 Approval + Security &
Roles — manages master data, approves/rejects; does NOT delete master data or
users, by design):

| # | Business action | Backend capability | Status |
|---|---|---|---|
| 1 | Log in | `POST /api/auth/login` | **Complete** |
| 2 | Approve / reject submitted reports (all 6 types) | `POST /api/reports/{module}/{id}/approve\|reject` | **Complete** |
| 3 | View reports (list + by id) | `GET /api/reports/{module}` + `/{id}` | **Complete** |
| 4 | View dashboard | `GET /api/reports/dashboard/*` (10) | **Complete** |
| 5 | View analytics | `GET /api/analytics/*` (9) | **Complete** |
| 6 | Create / update master data (lines, shifts, parameters) | `POST`/`PUT` for lines, shifts, parameters | **Complete** |
| 7 | View users (not create/delete) | `GET /api/users*` | **Complete** |
| 8 | View settings | `GET /api/settings*` | **Complete** |
| 9 | View audit logs | `GET /api/audit-logs*` | **Complete** |
| 10 | Change own password | `PUT /api/users/change-password` | **Complete** |
| — | Delete users / master data / reports | NOT granted (SUPER_ADMIN only) | Correct by design |

**Admin coverage: 10/10 = 100%**

---

## 3. Role — Staff (OPERATOR)

Business actions the Staff should perform (per Phase 2 Daily Operation):

| # | Business action | Backend capability | Status |
|---|---|---|---|
| 1 | Log in | `POST /api/auth/login` | **Complete** |
| 2 | Select report type | `GET /api/report-types` | **Complete** |
| 3 | Backend loads configured parameters for report type | `GET /api/parameters/report-type/{type}` | **Complete** |
| 4 | Select production line | `GET /api/lines` | **Complete** |
| 5 | Shift auto-detection (incl. overnight) | `GET /api/shifts/current` + `ShiftService.findShiftFor` on create | **Complete** |
| 6 | Create report (draft) with entries | `POST /api/reports/{module}` (6) | **Complete** |
| 7 | Submit report for approval | `POST /api/reports/{module}/{id}/submit` | **Complete** |
| 8 | View reports / report history | `GET /api/reports/{module}` + `/{id}` + paginated search | **Complete** |
| 9 | View dashboard | `GET /api/reports/dashboard/*` (10) | **Complete** |
| 10 | Search reports / users / parameters | `GET /api/reports/search`, `GET /api/search` | **Complete** |
| 11 | Upload / view / download attachments | `POST`/`GET`/`GET download` `/api/attachments*` | **Complete** |
| 12 | View / read notifications | `GET`/`PATCH` `/api/notifications*` | **Complete** |
| 13 | Change own password | `PUT /api/users/change-password` | **Complete** |
| 14 | **Edit a rejected report and resubmit** | **NO endpoint exists** (`PUT`/`PATCH` on reports not implemented) | **MISSING** |

**Staff coverage: 13/14 = 92.9%**

---

## 4. Missing Business Capability

| # | Missing capability | Business impact | Backend evidence |
|---|---|---|---|
| 1 | **Edit / update a report** (needed for the rejected → resubmit loop in Blueprint Phase 3: *"If rejected, staff edits and resubmits"*) | A rejected report cannot be corrected; the only actions available are submit (blocked once not DRAFT), approve/reject, and delete (DRAFT only). The reject→edit→resubmit workflow is not possible end-to-end. | No `@PutMapping`/`@PatchMapping` in any of the 6 report controllers; `AbstractReportService` has create/submit/approve/reject/delete only. This is also listed in Blueprint §8 Future Roadmap ("Report edit / resubmit for rejected reports, plus draft edit/resume — backend enablers planned — no update endpoint exists in V1"), so it is a known, consciously deferred item. |

> Per instruction, this is **not implemented**. It is the single gap between the
> documented business workflow and the shipped backend.

---

## 5. Partially Implemented Items

| Area | What exists | What is missing / partial |
|---|---|---|
| **Report edit / resubmit** | Reject workflow writes status=REJECTED + `remarks`; report can be resubmitted only after edit — but there is no edit path. | No `PUT/PATCH /api/reports/{module}/{id}`. (See Missing Business Capability.) |
| **Audit trail** | Full read model (`audit_logs` table, `AuditService`, 3 endpoints, statistics, Specification filter). | **Read-only**: nothing in the codebase writes `audit_logs` (no `AuditLogRepository.save` caller anywhere). Auth/report/master actions do not record audit entries, so the audit log is effectively always empty. Documented as "(currently read-only) audit-log service". |
| **Notifications** | Full notification store + API (`notifications` table, `NotificationService`, 6 endpoints, unread count). | **Not triggered by any workflow**: no report/approval/user event calls `NotificationService`/`NotificationChannel` from outside the notification module. Approval workflow notifications are listed in Blueprint §8 as future work. |
| **Shift validation** | Shift CRUD + overnight detection works. | `CreateShiftRequest.startTime/endTime` are not `@NotNull`; empty times silently allowed. Also no overlap validation (by design — only name is unique). |
| **Submit metadata** | Submit transition enforced (DRAFT→SUBMITTED). | No `submittedBy`/`submittedAt` columns recorded (by design). |
| **Report number sequence** | Generated as `{PREFIX}-{yyyyMMdd}-%05d`. | Uses `count()+1` (no DB sequence); race-prone and shifts after deletes (documented in `API_DOCUMENTATION.md`). |
| **MapStruct** | Declared in `pom.xml`. | Not used anywhere — all mappers are manual `@Component` classes. Harmless; could be removed. |

---

## 6. Business Coverage Report

| Role | Capabilities verified | Implemented | Coverage |
|---|---|---|---|
| Super Admin | 19 | 19 | **100%** |
| Admin | 10 | 10 | **100%** |
| Staff | 14 | 13 | **92.9%** |
| **Overall** | **43** | **42** | **97.7%** |

The single shortfall is the report **edit/resubmit** capability (a documented
post-V1 roadmap item).

---

## 7. Missing Features

1. **Report edit / resubmit** — no update endpoint for any report type; the
   reject → edit → resubmit loop (Blueprint Phase 3) is incomplete.
2. **Workflow-triggered notifications** — approval workflow does not create
   notifications (Blueprint §8 future item).
3. **Write-path audit logging** — audit log is read-only; no events are recorded
   (Blueprint §8 future item).

---

## 8. Nice-to-have Features

1. **`submittedBy`/`submittedAt`** columns on reports for a complete lifecycle trail.
2. **DB-backed report number sequence** to remove the `count()+1` race.
3. **Shift overlap validation** and `@NotNull` on `CreateShiftRequest` times.
4. **Attachments per report entry** (Blueprint §8) — currently attachments are
   free-form (module + entityId), not bound to report entries.
5. **Parameter-level analytics** (Blueprint §8).
6. **Dashboard widgets per report type** (Blueprint §8).
7. Remove unused **MapStruct** dependency (declared, never used).
8. **Role management UI/API** — roles exist (3 seeded) and are assigned via user
   DTOs, but there is no `RoleController`/role CRUD API (fine for V1).

---

## 9. Production Readiness Checklist

| Area | Status | Detail |
|---|---|---|
| Auth / JWT | ✅ | Access + refresh tokens, BCrypt, `@EnableMethodSecurity`, stateless, CORS enabled |
| RBAC | ✅ | `@PreAuthorize` on all protected endpoints; role matrix correct per blueprint |
| Validation | ✅ | `@Valid` on all request bodies; `jakarta.validation` on all DTOs; `GlobalExceptionHandler` returns 400 with field errors |
| Error handling | ✅ | Central `@RestControllerAdvice`: 400/401/403/404/409/405/500 with consistent `ApiError` envelope |
| Database schema | ✅ | Flyway migrations V1–V8; indexes on FK/status/date columns |
| **Secrets in source** | ⚠️ | `application.properties` hardcodes DB credentials (`postgres`/`postgres`) and `jwt.secret` — must move to env/config |
| **`ddl-auto=update`** | ⚠️ | Set to `update`; Flyway owns schema — should be `validate` for production |
| **SQL logging** | ⚠️ | `show-sql=true`, Hibernate SQL=DEBUG, bind=TRACE — verbose/leaky; gate behind a profile |
| Connection pool tuning | ⚠️ | No explicit HikariCP max/min/timeouts configured |
| Pagination | ✅ | Shared framework (`PageRequest`/`PageableResolver`/`PageResponse`) with size cap 200 + sort whitelist |
| API documentation | ✅ | SpringDoc OpenAPI; every endpoint has `@Operation` + `@SecurityRequirement` |
| Tests | ⚠️ | Only 1 Spring-context test exists (`CedOpsBackendApplicationTests`); no controller/service/unit tests |
| Observability | ⚠️ | No metrics/health endpoints beyond Spring defaults; structured logging is INFO/SLF4J |
| Data seeding | ✅ | `DataSeeder` seeds 3 roles + default SUPER_ADMIN (`ADMIN001/admin123`) — **default credentials should be changed in prod** |
| HTTPS / prod envs | ⚠️ | No profile-specific configs; single `application.properties` |

---

## 10. Recommendation

**Ready for Frontend** — with the caveat below.

Rationale: 42 of 43 documented business capabilities are fully implemented with
consistent entity/DTO/repository/service/controller/endpoint/Swagger/validation/
RBAC/error-handling coverage. All three roles can perform their core workflow
(Super Admin setup, Admin approval, Staff daily entry + submit + reporting).

**Caveats to resolve before/at handoff:**
- **Staff cannot edit a rejected report** (missing edit/resubmit). If the client
  expects the reject → edit → resubmit loop in V1, this needs backend work
  (**Needs Backend Work** for that one item).
- **Production hardening** (secrets, `ddl-auto=validate`, SQL logging, tests,
  default credentials) should be addressed before production deployment — it does
  not block frontend development.

**Decision path:**
- If the client treats report **edit/resubmit as V1 scope** → **Needs Backend Work** (single missing capability).
- If reject→edit→resubmit is accepted as post-V1 (as Blueprint §8 implies) → **Ready for Frontend**.
- Clarify with the client which of the two applies — the rest of the API is complete.

---

*Verification performed on the feature-complete V1 backend. No production code was
generated or modified. Results summarized in `CURRENT_STATE.md`.*
