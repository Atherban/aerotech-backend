# CED Operations — REST API Documentation

> Complete reference for every HTTP endpoint implemented in the `ced-ops-backend` Spring Boot service (Java 21, Spring Boot 3). Sources of truth: the controller/service/entity source code (verified: **130 HTTP operations across 56 path templates** across 21 controllers) and the running application's OpenAPI 3 spec (regenerated at runtime from `/v3/api-docs`). Every successful JSON response is wrapped in the `ApiResponse<T>` envelope; every error uses the `ApiError` envelope.

## Table of Contents

1. [Overview](#1-overview)
2. [Authentication & Authorization](#2-authentication--authorization)
3. [Common Response Envelope](#3-common-response-envelope)
4. [Pagination](#4-pagination)
5. [Report Workflow (shared across the six report modules)](#5-report-workflow-shared-across-the-six-report-modules)
6. [Authentication](#6-authentication)
7. [Users](#7-users)
8. [Master Data — Lines](#8-master-data--lines)
9. [Master Data — Shifts](#9-master-data--shifts)
10. [Master Data — Parameters](#10-master-data--parameters)
11. [Report Types](#11-report-types)
12. [Settings](#12-settings)
13. [Reports — Process Monitoring](#13-reports--process-monitoring)
14. [Reports — Chemical Consumption](#14-reports--chemical-consumption)
15. [Reports — Daily Startup](#15-reports--daily-startup)
16. [Reports — Daily Inspection](#16-reports--daily-inspection)
17. [Reports — First Piece Inspection](#17-reports--first-piece-inspection)
18. [Reports — Pre-Delivery Inspection](#18-reports--pre-delivery-inspection)
19. [Dashboard](#19-dashboard)
20. [Global Search](#20-global-search)
21. [Analytics](#21-analytics)
22. [Integrations](#22-integrations)
23. [Attachments](#23-attachments)
24. [Notifications](#24-notifications)
25. [Audit Logs](#25-audit-logs)
26. [Appendix A — DTO Reference](#26-appendix-a--dto-reference)
27. [Appendix B — Status Codes](#27-appendix-b--status-codes)

## 1. Overview

The backend exposes a production-operations domain: user & role management, master data (lines, shifts, parameters), six report types covering the shop-floor workflow (Process Monitoring, Chemical Consumption, Daily Startup, Daily Inspection, First Piece Inspection, Pre-Delivery Inspection), a dashboard and unified search over reports, users and parameters, analytics, integrations, attachments, notifications, settings, and a (currently read-only) audit-log service. The backend provides structured JSON APIs only; PDF/Excel/CSV/print export is implemented by the frontend.

Key conventions:
- **Base URL:** `http://<host>:3000` (`application.properties`; PostgreSQL `ced_ops` on port 5432).
- **Content type:** `application/json` for JSON bodies/responses; `multipart/form-data` for uploads.
- **Response envelope:** every success is `ApiResponse<T>` (`{success, message, data}`); every failure is `ApiError` (section 3).
- **Pagination:** list endpoints return `PageResponse<T>` and accept `page`/`size` (section 4).
- **Report status lifecycle:** `DRAFT -> SUBMITTED -> APPROVED | REJECTED` (section 5).

Report-type catalog:

| API path segment | Report | Number prefix | Tables |
|---|---|---|---|
| `process-monitoring` | Process Monitoring | `PMR` | `process_monitoring_reports` / `process_monitoring_entries` |
| `chemical-consumption` | Chemical Consumption | `CCR` | `chemical_consumption_reports` / `chemical_consumption_entries` |
| `daily-startup` | Daily Startup | `DSR` | `daily_startup_reports` / `daily_startup_entries` |
| `daily-inspection` | Daily Inspection | `DIR` | `daily_inspection_reports` / `daily_inspection_entries` |
| `first-piece-inspection` | First Piece Inspection | `FPI` | `first_piece_inspection_reports` / `first_piece_inspection_entries` |
| `pre-delivery-inspection` | Pre-Delivery Inspection | `PDI` | `pre_delivery_inspection_reports` / `pre_delivery_inspection_entries` |

Other core tables: `users`, `roles`, `refresh_token`, `line_master`, `shifts`, `parameter_master`, `system_settings`, `integrations`, `integration_execution_histories`, `attachments`, `notifications`, `audit_logs`.

## 2. Authentication & Authorization

Authentication is **JWT bearer token** based (`bearerAuth` HTTP scheme, defined in `OpenApiConfig` and applied globally). Every endpoint requires `Authorization: Bearer <accessToken>` **except** `POST /api/auth/login` and `POST /api/auth/refresh`, which are `permitAll()` in `SecurityConfig`.

- **Access token:** signed JWT, subject = `employeeId`, one custom claim `role` = role name, expiry **15 minutes**.
- **Refresh token:** random opaque token persisted (hashed) in `refresh_token`; on refresh a **new access token** is issued and the same refresh token is returned (no rotation). Expiry **7 days**.
- **Roles:** seeded `SUPER_ADMIN`, `ADMIN`, `OPERATOR`; Spring authorities are `ROLE_<name>`.
- **Method security:** `@PreAuthorize` on controllers (`@EnableMethodSecurity`). Password hashing: BCrypt (strength 12).
- **Role matrix (per module):**

| Module | Roles allowed |
|---|---|
| Users create/update/delete/status | `SUPER_ADMIN` only |
| Users list/get | `SUPER_ADMIN`, `ADMIN` |
| Users profile / change-password / auth me / validate | any authenticated user |
| Lines, Shifts, Parameters create/update | `SUPER_ADMIN`, `ADMIN` |
| Lines, Shifts, Parameters delete | `SUPER_ADMIN` only |
| Lines, Shifts, Parameters, Report-types reads | any authenticated user |
| Settings create/update/delete | `SUPER_ADMIN` only |
| Settings reads | `SUPER_ADMIN`, `ADMIN` |
| Report create/list/get/submit | any authenticated user |
| Report approve/reject | `SUPER_ADMIN`, `ADMIN` |
| Report delete | `SUPER_ADMIN` only |
| Dashboard, Global Search, Attachments, Notifications | any authenticated user |
| Analytics | `SUPER_ADMIN`, `ADMIN` |
| Integrations (all) | `SUPER_ADMIN` only |
| Audit logs (all) | `SUPER_ADMIN`, `ADMIN` |

**Error semantics:** 401 = missing/invalid token or anonymous access to a guarded endpoint; 403 = authenticated but lacking the required role; 409 = data-integrity conflict.

## 3. Common Response Envelope

**Success — `ApiResponse<T>`** (class `common.response.ApiResponse`):

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { }
}
```

| Field | Type | Description |
|---|---|---|
| `success` | boolean | Always `true` on success |
| `message` | String | Short human message |
| `data` | T | The endpoint's payload (DTO, `List`, `PageResponse`, Boolean, or null) |

**Failure — `ApiError`** (class `common.exception` / `ApiError`):

```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-08-02T10:00:00",
  "errors": [ "entries: must not be empty" ],
  "data": null
}
```

| Field | Type | Description |
|---|---|---|
| `success` | boolean | Always `false` |
| `status` | int | HTTP status code |
| `message` | String | Top-level error message |
| `timestamp` | Instant | Server time of the error |
| `errors` | List&lt;String&gt; | Per-field validation messages when applicable |
| `data` | Object | Null on errors |

**HTTP status mapping** (`GlobalExceptionHandler`): `ResourceNotFoundException` -> 404; `BadRequestException` -> 400; `MethodArgumentNotValidException` / `ConstraintViolationException` -> 400; `MethodArgumentTypeMismatchException` -> 400; `IllegalArgumentException` -> 400; `UnsupportedOperationException` -> 400; `SecurityException`/`AuthorizationDeniedException` -> 403 (or 401 when anonymous); `DataIntegrityViolationException` -> 409; any other exception -> 500.

## 4. Pagination & Filtering (unified framework)

Every module uses the same pagination/filtering contract built on the shared
framework in `common/pagination`:

- **`PageRequest`** — the single request DTO (base). Optional query params:
  `page`, `size`, `sortBy`, `sortDirection`, `keyword`. Module-specific filter
  DTOs extend it (`UserFilterRequest`, `ParameterFilterRequest`,
  `ReportFilterRequest`).
- **`PageableResolver`** — builds a Spring `Pageable`, clamps `size` to a max of
  `200`, and resolves `sortBy` from a per-module **whitelist** (unknown sort
  fields fall back to the module default).
- **`SpecificationBuilder`** — reusable JPA `Specification` builder for
  keyword / equality / range filters.
- **`PageResponse<T>`** — the single paginated response envelope.

**Query params (optional, all endpoints below accept them):**

| Param | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | Zero-based page index |
| `size` | int | `20` | Page size (capped at 200) |
| `sortBy` | string | module default | Whitelisted sort field |
| `sortDirection` | string | `DESC` | `ASC` or `DESC` |
| `keyword` | string | — | Free-text search token |

**Backward compatibility:** when no paging/search params are supplied, the
legacy full-list (`data` = array) response is returned unchanged. Supplying any
paging or filter param opts into the paged response.

The `data` field for paged results is a `PageResponse<T>`:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

**Endpoints using the framework:**

| Endpoint | Module filter DTO | Extra filter params |
|---|---|---|
| `GET /api/users` | `UserFilterRequest` | `role`, `active` |
| `GET /api/parameters` | `ParameterFilterRequest` | `reportType`, `inputType`, `active`, `visible` |
| `GET /api/reports/{module}` (6 report types) | `ReportFilterRequest` | `reportNumber`, `status`, `shiftId`, `lineId`, `dateFrom`, `dateTo`, `approved` |

**Sort whitelists** — Users: `id`, `employeeId`, `firstName`, `lastName`,
`role`, `active`, `createdAt`. Parameters: `id`, `parameterName`,
`displayOrder`, `inputType`, `active`, `visible`, `createdAt`. Reports:
`id`, `reportNumber`, `reportDate`, `status`, `createdAt`, `updatedAt`.

## 5. Report Workflow (shared across the six report modules)

All six report modules share the same endpoint shape, state machine and validation logic; only the path, number prefix, tables and (for three modules) a few extra report header fields differ.

**State machine:**

```
        create                 submit                 approve
  (none) ─────► DRAFT ─────────────► SUBMITTED ──────────────► APPROVED
                     ▲                   │
                     │                   │ reject
                     │                   ▼
                     │               REJECTED  (terminal in V1)
                     └──── delete (physical) ──► removed
```

> ⚠️ **No update endpoint exists.** There is **no** `PUT`/`PATCH` for any report
> module: existing drafts cannot be edited or resumed, and a `REJECTED` report
> has no transition back to `SUBMITTED` (no edit/resubmit workflow). A report is
> created in one request (stored as `DRAFT`) and thereafter only submitted,
> approved, rejected, or deleted (DRAFT only, SUPER_ADMIN).

- **Create:** the report is stored as `DRAFT`. For every entry, `ValidationService.validate` computes the `inspectionResult` **once at creation time**: if the parameter's `inputType == NUMBER`, the observed value is parsed as `BigDecimal` and compared with the parameter's `minValue`/`maxValue` (`FAIL` below min or above max, `FAIL` on non-numeric, `PASS` in range); blank values and non-NUMBER inputs yield `NOT_APPLICABLE`.
- **Report number:** generated as `{PREFIX}-{yyyyMMdd}-%05d` (e.g. `PMR-20260802-00001`) where the sequence is `reportRepository.count()+1` for the module (no real DB sequence — race-prone and numbers shift after deletes).
- **Shift auto-detection:** when `shiftId` is omitted the service picks the active shift whose window covers the current server time (overnight wraps supported); if none covers, the first active shift is used; no active shifts -> 404 on `GET /api/shifts/current`.
- **Submit:** only `DRAFT -> SUBMITTED` (else 400). No `submittedBy`/`submittedAt` is recorded (columns do not exist).
- **Approve:** only `SUBMITTED -> APPROVED` (else 400). Stamps `approvedBy`/`approvedAt`. Requires `SUPER_ADMIN` or `ADMIN`.
- **Reject:** only `SUBMITTED -> REJECTED` (else 400). The rejection reason is stored in the shared `remarks` column (no dedicated `rejectionReason` field). Requires `SUPER_ADMIN` or `ADMIN`.
- **Delete:** only while `DRAFT` (else 400). **Physical** delete — entries first, then the report row. Requires `SUPER_ADMIN`.
- **Notifications:** report flows notify the report creator in-app. `create` sends `REPORT_CREATED`, `submit` sends `REPORT_SUBMITTED`, and the approval step sends `REPORT_APPROVED`/`REPORT_REJECTED` (recipient = report `createdBy`). No report flow writes audit rows; the only DB audit trail is JPA `created_at`/`updated_at`.

**Endpoints per module** (identical contract; `{module}` is one of the six path segments in section 1):

| # | Method | URL | Purpose | Roles |
|---|---|---|---|---|
| 1 | `POST` | `/api/reports/{module}` | Create report (DRAFT) | any authenticated |
| 2 | `GET` | `/api/reports/{module}` | List all reports (newest first); accepts optional pagination/filter params (`ReportFilterRequest`) → `PageResponse` when supplied, legacy `List` otherwise | any authenticated |
| 3 | `GET` | `/api/reports/{module}/{id}` | Get report by id | any authenticated |
| 4 | `POST` | `/api/reports/{module}/{id}/submit` | Submit for approval | any authenticated |
| 5 | `POST` | `/api/reports/{module}/{id}/approve` | Approve | `SUPER_ADMIN`/`ADMIN` |
| 6 | `POST` | `/api/reports/{module}/{id}/reject` | Reject | `SUPER_ADMIN`/`ADMIN` |
| 7 | `DELETE` | `/api/reports/{module}/{id}` | Delete (DRAFT only, physical) | `SUPER_ADMIN` |

**Shared request DTO fields** (per-module DTOs like `CreateProcessMonitoringRequest`):

| Field | Type | Required | Description / Validation |
|---|---|---|---|
| `reportDate` | LocalDate | Yes | Date of the report (`@NotNull` 'Report date is required') |
| `shiftId` | long | No | Shift; omitted -> auto-detect active shift covering now |
| `lineId` | long | Yes | Line the report belongs to (`@NotNull` 'Line ID is required'; must exist) |
| `remarks` | String | No | Free text, `@Size(max=1000)` |
| `entries` | List | Yes | At least one entry (`@Valid @NotEmpty`) |

**Entry DTO** (e.g. `ProcessMonitoringEntryRequest`):

| Field | Type | Required | Description / Validation |
|---|---|---|---|
| `parameterId` | long | Yes | Master parameter being measured (`@NotNull`; must exist) |
| `observedValue` | String | Yes | Raw value (`@NotBlank`, `@Size(max=500)`); validated against min/max when the parameter input type is NUMBER |
| `remark` | String | No | Per-entry remark, `@Size(max=1000)` |

**Submit/Approve DTOs** (e.g. `SubmitProcessMonitoringRequest`, `ApproveProcessMonitoringRequest` — approve DTO is also used for reject):

| Field | Type | Required | Description / Validation |
|---|---|---|---|
| `remarks` | String | No | `@Size(max=1000)`; on approve/reject it overwrites the report `remarks` |

**Shared response DTO** (per-module, e.g. `ProcessMonitoringResponse`): `id`, `reportNumber`, `reportDate`, `shift` (shift name), `line` (line name), `createdBy` (full name), `approvedBy` (full name, nullable), `status`, `remarks`, `approvedAt`, `createdAt`, `entries: List<EntryResponse>`. Each entry: `id`, `parameterId`, `parameterName`, `minValue`, `maxValue`, `observedValue`, `unit`, `inspectionResult` (`PASS`/`FAIL`/`NOT_APPLICABLE`), `remark`.

## 6. Authentication

### `POST /api/auth/login` — Authenticate user and return JWT tokens

**Purpose** — Public endpoint. Authenticates a user using employee ID and password and returns a JWT access token along with a refresh token.

**HTTP Method** — `POST`

**URL** — `/api/auth/login`

**Authorization** — **None** — public endpoint (`/api/auth/**` is `permitAll`); no `Authorization` header required.

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `employeeId` | String | Yes | Employee ID |
| `password` | String | Yes | User password |

**Validation rules** — employeeId: @NotBlank ('Employee ID is required'); password: @NotBlank ('Password is required').

**Example Request**

```json
{
  "employeeId": "EMP001",
  "password": "password123"
}
```

**Example Response** (200 Login successful, returns JWT tokens)

```json
{
  "success": true,
  "message": "Login Successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJFTVAwMDEiLCJyb2xlIjoiT1BFUkFUT1IifQ...",
    "refreshToken": "f0e1d2c3...",
    "tokenType": "Bearer",
    "employeeId": "EMP001",
    "fullName": "John Doe",
    "role": "OPERATOR"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - invalid employee ID or password |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `LoginRequest`

**Business Flow**

1. Client submits employeeId + password.
2. Spring's DaoAuthenticationProvider resolves the user via CustomUserDetailsService.loadUserByUsername (UserRepository.findByEmployeeId with JOIN FETCH role) and verifies the password with BCrypt (strength 12).
3. Bad credentials -> 401 'Invalid employee ID or password'. Note: an account with active=false fails in isEnabled() and currently surfaces as an unhandled 500 rather than a clean
401.
4. On success, JwtService generates an access token (subject = employeeId, claim 'role' = role name, 15 min expiry) and RefreshTokenService.create deletes the user's existing refresh tokens then persists a new random refresh token (7 day expiry, hashed row).
5. Returns AuthResponse {accessToken, refreshToken, tokenType='Bearer', employeeId, fullName, role}.

**Database Impact**

Read: users (JOIN FETCH role -> also reads roles), twice. Write: refresh_token (DELETE where user_id, then INSERT new token row). No audit_logs write.

---

### `POST /api/auth/refresh` — Refresh expired access token using refresh token

**Purpose** — Public endpoint. Exchanges a valid refresh token for a new JWT access token.

**HTTP Method** — `POST`

**URL** — `/api/auth/refresh`

**Authorization** — **None** — public endpoint (`/api/auth/**` is `permitAll`); no `Authorization` header required.

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `refreshToken` | String | Yes | Refresh token issued during login |

**Validation rules** — refreshToken: @NotBlank ('Refresh token is required').

**Example Request**

```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
}
```

**Example Response** (200 Token refreshed successfully)

```json
{
  "success": true,
  "message": "Token Refreshed",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJFTVAwMDEiLCJyb2xlIjoiT1BFUkFUT1IifQ...",
    "refreshToken": "f0e1d2c3...",
    "tokenType": "Bearer",
    "employeeId": "EMP001",
    "fullName": "John Doe",
    "role": "OPERATOR"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - invalid or expired refresh token |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `RefreshTokenRequest`

**Business Flow**

1. Client presents the stored refreshToken.
2. Service looks up the row in refresh_token; missing -> 401 'Invalid Refresh Token'.
3. JwtService.isTokenValid verifies signature + expiry; if invalid/expired the row is physically deleted and 401 'Refresh Token Expired' is returned.
4. On success a NEW access token is issued for the token's user; the SAME refresh token is returned unchanged (no rotation).
5. Returns AuthResponse with message 'Token Refreshed'.

**Database Impact**

Read: refresh_token (findByToken), users + roles (lazy). Write: refresh_token (DELETE) only on invalid/expired path. No audit write.

---

### `POST /api/auth/logout` — Invalidate refresh token and logout user

**Purpose** — Invalidates the refresh token of the currently authenticated user, effectively logging them out.

**HTTP Method** — `POST`

**URL** — `/api/auth/logout`

**Authorization** — Bearer token required in practice — no `@PreAuthorize` guard exists (an anonymous call fails with a 500 NPE); Swagger documents a bearer requirement.

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Logout successful)

```json
{
  "success": true,
  "message": "Logout Successful",
  "data": null
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. Uses the authenticated principal's employeeId (authentication.getName()).
2. Loads the user (404 if missing) and deletes ALL of the user's refresh_token rows.
3. JWT access tokens remain valid until their 15-minute expiry (stateless).
4. Note: no @PreAuthorize guard exists, so an anonymous call would NPE; the Swagger security requirement is documentation only.

**Database Impact**

Read: users (findByEmployeeId). Write: refresh_token (DELETE all rows for the user). No audit write.

---

### `GET /api/auth/me` — Get current authenticated user's profile

**Purpose** — Retrieves the profile of the currently authenticated user.

**HTTP Method** — `GET`

**URL** — `/api/auth/me`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Profile retrieved successfully)

```json
{
  "success": true,
  "message": "Current User",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "9876543210",
    "role": "OPERATOR",
    "active": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | User not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()') guards the call (401 if anonymous).
2. Loads the user by employeeId from the JWT subject (404 if missing) and maps to UserResponse. Message 'Current User'.

**Database Impact**

Read: users (JOIN FETCH role -> roles). No writes.

---

### `GET /api/auth/validate` — Validate JWT token validity

**Purpose** — Checks whether the supplied JWT access token is valid and not expired.

**HTTP Method** — `GET`

**URL** — `/api/auth/validate`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `Authorization` | header | String | Yes | Authorization header containing the Bearer token |

**Example Response** (200 Token is valid)

```json
{
  "success": true,
  "message": "Token Valid",
  "data": true
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Requires an 'Authorization' header; strips the 'Bearer ' prefix (malformed header -> 500).
3. JwtService.isTokenValid parses/verifies the JWT (no DB access). Returns data=true, message 'Token Valid'.

**Database Impact**

None (pure JWT parse).

---

## 7. Users

### `GET /api/users` — Get all users

**Purpose** — Retrieves the list of all users. Accessible to users with the SUPER_ADMIN or ADMIN role. Supports optional pagination and filtering via the shared framework (`UserFilterRequest`: `page`, `size`, `sortBy`, `sortDirection`, `keyword`, `role`, `active`); when any such param is present the `data` field is a `PageResponse<UserResponse>`, otherwise the legacy `UserResponse[]` list.

**HTTP Method** — `GET`

**URL** — `/api/users`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters** (all optional)

| Param | In | Type | Description |
|---|---|---|---|
| `page` | query | int | Zero-based page number (default 0) |
| `size` | query | int | Page size (default 20, max 200) |
| `sortBy` | query | string | Sort field: `id`, `employeeId`, `firstName`, `lastName`, `role`, `active`, `createdAt` |
| `sortDirection` | query | string | `ASC` or `DESC` (default `DESC`) |
| `keyword` | query | string | Free-text match on employeeId / firstName / lastName / mobileNumber |
| `role` | query | string | Filter by role name (e.g. `OPERATOR`) |
| `active` | query | boolean | Filter by active status |

**Example Response** (200 Users retrieved successfully — legacy list)

```json
{
  "success": true,
  "message": "Users fetched successfully",
  "data": [
    {
      "id": 1,
      "employeeId": "EMP001",
      "firstName": "John",
      "lastName": "Doe",
      "mobileNumber": "9876543210",
      "role": "OPERATOR",
      "active": true,
      "createdAt": "2026-08-02T08:00:00",
      "updatedAt": "2026-08-02T08:00:00"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UserResponse`, `UserFilterRequest`, `PageResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. No search criteria -> readOnly tx; repository.findAllByOrderByIdAsc() with @EntityGraph(role), mapped to List<UserResponse>. Message 'Users fetched successfully'.
3. With search criteria -> SpecificationBuilder (keyword on employeeId/firstName/lastName/mobileNumber, equality on role.name/active) + PageableResolver -> PageResponse<UserResponse>.

**Database Impact**

Read: users (entity graph -> roles). No writes.

---

### `POST /api/users` — Create a new user (Super Admin only)

**Purpose** — Creates a new user account. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `POST`

**URL** — `/api/users`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `employeeId` | String | Yes | Unique employee ID |
| `firstName` | String | Yes | First name |
| `lastName` | String | Yes | Last name |
| `mobileNumber` | String | No | Mobile number (10 digits, starting with 6-9) |
| `password` | String | No | Password (minimum 8 characters) |
| `role` | String | Yes | User role Enum: `SUPER_ADMIN`, `ADMIN`, `OPERATOR`. |

**Validation rules** — employeeId: @NotBlank (unique); firstName: @NotBlank; lastName: @NotBlank; mobileNumber: @Pattern('^[6-9]\\d{9}$') (10 digits starting 6-9, unique); password: @Size(min=8); role: @NotBlank (must be a seeded role name: SUPER_ADMIN/ADMIN/OPERATOR).

**Example Request**

```json
{
  "employeeId": "EMP002",
  "firstName": "Jane",
  "lastName": "Smith",
  "mobileNumber": "9876543210",
  "password": "password123",
  "role": "OPERATOR"
}
```

**Example Response** (201 User created successfully)

```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "9876543210",
    "role": "OPERATOR",
    "active": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateUserRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Service checks existsByEmployeeId -> 400 'Employee ID already exists', then existsByMobileNumber -> 400 'Mobile number already exists'.
3. Role is resolved by name via RoleRepository.findByName; unknown name -> 404 'Role Not Found'.
4. Password is BCrypt-encoded (empty password passes @Size and is encoded as-is), active=true.
5. Returns 201 Created with Location: /api/users/{id} and ApiResponse<UserResponse>.

**Database Impact**

Read: users (exists checks), roles (findByName). Write: users (INSERT). No audit write.

---

### `GET /api/users/{id}` — Get user by ID

**Purpose** — Retrieves a single user by their unique identifier. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/users/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Unique user identifier |

**Example Response** (200 User retrieved successfully)

```json
{
  "success": true,
  "message": "User fetched successfully",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "9876543210",
    "role": "OPERATOR",
    "active": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | User not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. findById -> 404 'User not found' if missing; maps to UserResponse. Message 'User fetched successfully'.

**Database Impact**

Read: users + roles. No writes.

---

### `PUT /api/users/{id}` — Update user details (Super Admin only)

**Purpose** — Updates the details of an existing user. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `PUT`

**URL** — `/api/users/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Unique user identifier |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `firstName` | String | Yes | First name |
| `lastName` | String | Yes | Last name |
| `mobileNumber` | String | No | Mobile number (10 digits, starting with 6-9) |
| `role` | String | Yes | User role Enum: `SUPER_ADMIN`, `ADMIN`, `OPERATOR`. |

**Validation rules** — firstName: @NotBlank; lastName: @NotBlank; mobileNumber: @Pattern('^[6-9]\\d{9}$') + unique (excluding self); role: @NotBlank (seeded role name).

**Example Request**

```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "mobileNumber": "9876543210",
  "role": "OPERATOR"
}
```

**Example Response** (200 User updated successfully)

```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "9876543210",
    "role": "OPERATOR",
    "active": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | User not found |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateUserRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. If mobileNumber changed, existsByMobileNumber -> 400 'Mobile number already exists'.
4. Role resolved by name (404 'Role Not Found' if unknown). employeeId and password are NOT updatable.
5. Saves and returns UserResponse. Message 'User updated successfully'.

**Database Impact**

Read: users, roles. Write: users (UPDATE first/last name, mobile, role_id). No audit write.

---

### `DELETE /api/users/{id}` — Delete a user (Super Admin only)

**Purpose** — Permanently deletes a user and their refresh tokens. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `DELETE`

**URL** — `/api/users/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Unique user identifier |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | User not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. Deletes all the user's refresh tokens, then physically deletes the user row (HARD delete, no soft flag).
4. Returns 204 No Content (no envelope).
5. If other tables FK-reference the user (e.g. attachments.uploaded_by) the delete fails with a 409 DataIntegrityViolationException.

**Database Impact**

Read: users. Write: refresh_token (DELETE by user), users (DELETE row - physical). No audit write.

---

### `PATCH /api/users/{id}/status` — Activate or deactivate a user (Super Admin only)

**Purpose** — Updates the active status of an existing user. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `PATCH`

**URL** — `/api/users/{id}/status`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Unique user identifier |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `active` | boolean | Yes | New active status |

**Validation rules** — active: @NotNull ('Status is required').

**Example Request**

```json
{
  "active": false
}
```

**Example Response** (200 User status updated successfully)

```json
{
  "success": true,
  "message": "User status updated successfully",
  "data": null
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | User not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateStatusRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. Sets active flag and saves (only that column).
4. Deactivation blocks future logins but does NOT revoke existing JWTs or refresh tokens. Message 'User status updated successfully'.

**Database Impact**

Read: users. Write: users (UPDATE active). No audit write.

---

### `GET /api/users/profile` — Get current user's profile

**Purpose** — Retrieves the profile of the currently authenticated user.

**HTTP Method** — `GET`

**URL** — `/api/users/profile`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Profile retrieved successfully)

```json
{
  "success": true,
  "message": "Profile fetched successfully",
  "data": {
    "id": 1,
    "employeeId": "EMP001",
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "9876543210",
    "role": "OPERATOR",
    "active": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | User not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Loads the current user by employeeId (JWT subject) -> 404 if missing; maps to UserResponse. Identical to /api/auth/me. Message 'Profile fetched successfully'.

**Database Impact**

Read: users + roles. No writes.

---

### `PUT /api/users/change-password` — Change current user's password

**Purpose** — Changes the password of the currently authenticated user after verifying the old password.

**HTTP Method** — `PUT`

**URL** — `/api/users/change-password`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `oldPassword` | String | Yes | Current password |
| `newPassword` | String | Yes | New password (minimum 8 characters) |

**Validation rules** — oldPassword: @NotBlank; newPassword: @NotBlank + @Size(min=8, 'Password must contain at least 8 characters').

**Example Request**

```json
{
  "oldPassword": "oldPassword123",
  "newPassword": "newPassword123"
}
```

**Example Response** (200 Password changed successfully)

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | User not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ChangePasswordRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Loads user by employeeId (404 if missing).
3. passwordEncoder.matches(oldPassword) -> 400 'Old password is incorrect' on mismatch.
4. New password BCrypt-encoded and saved.
5. Refresh tokens are NOT invalidated. Message 'Password changed successfully'.

**Database Impact**

Read: users. Write: users (UPDATE password). No audit write.

---

## 8. Master Data — Lines

Master data provides the reference entities referenced by reports. All line deletes are **soft deletes** (`active=false`); the row is never removed, so historical reports that reference a line remain valid.

### `GET /api/lines` — Get all production lines

**Purpose** — Fetches all production lines ordered by display order.

**HTTP Method** — `GET`

**URL** — `/api/lines`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Production lines fetched successfully)

```json
{
  "success": true,
  "message": "Lines fetched successfully.",
  "data": [
    {
      "id": 1,
      "name": "Line 1",
      "description": "Main production line",
      "displayOrder": 1,
      "active": true
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. readOnly tx; lineRepository.findAllByOrderByDisplayOrderAsc() maps to List<LineResponse>. NOTE: returns ALL rows including deactivated (soft-deleted) ones. Message 'Lines fetched successfully.'

**Database Impact**

Read: line_master (ORDER BY display_order). No writes.

---

### `POST /api/lines` — Create a new production line

**Purpose** — Creates a new production line with the provided details and returns the created line.

**HTTP Method** — `POST`

**URL** — `/api/lines`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | String | Yes | Name of the line |
| `description` | String | No | Optional description of the line |
| `displayOrder` | int | Yes | Display order for sorting |

**Validation rules** — name: @NotBlank ('Line name is required') + unique (case-insensitive); description: optional; displayOrder: @NotNull ('Display order is required') + @Min(1).

**Example Request**

```json
{
  "name": "Assembly Line 1",
  "description": "Main assembly line for product A",
  "displayOrder": 1
}
```

**Example Response** (201 Production line created successfully)

```json
{
  "success": true,
  "message": "Line created successfully.",
  "data": {
    "id": 1,
    "name": "Line 1",
    "description": "Main production line",
    "displayOrder": 1,
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateLineRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. existsByNameIgnoreCase(name.trim()) -> 400 'Line already exists.'.
3. Creates line with active=true default, saves.
4. 201 Created + Location: /api/lines/{id}.

**Database Impact**

Read: line_master (duplicate check). Write: line_master (INSERT). Duplicate -> 400 (service) or 409 (DB unique).

---

### `GET /api/lines/{id}` — Get production line by ID

**Purpose** — Fetches a single production line by its unique ID.

**HTTP Method** — `GET`

**URL** — `/api/lines/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the production line |

**Example Response** (200 Production line fetched successfully)

```json
{
  "success": true,
  "message": "Line fetched successfully.",
  "data": {
    "id": 1,
    "name": "Line 1",
    "description": "Main production line",
    "displayOrder": 1,
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Production line not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findById -> 404 'Line not found.' if missing (inactive rows still returned). Message 'Line fetched successfully.'

**Database Impact**

Read: line_master (by PK). No writes.

---

### `PUT /api/lines/{id}` — Update a production line

**Purpose** — Updates an existing production line with the provided details and returns the updated line.

**HTTP Method** — `PUT`

**URL** — `/api/lines/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the production line |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | String | Yes | Name of the line |
| `description` | String | No | Optional description of the line |
| `displayOrder` | int | Yes | Display order for sorting |
| `active` | boolean | No | Whether the line is active |

**Validation rules** — name: @NotBlank + unique (excluding self); description: optional; displayOrder: @NotNull + @Min(1); active: Boolean optional (applied only when non-null).

**Example Request**

```json
{
  "name": "Assembly Line 1",
  "description": "Main assembly line for product A",
  "displayOrder": 1,
  "active": true
}
```

**Example Response** (200 Production line updated successfully)

```json
{
  "success": true,
  "message": "Line updated successfully.",
  "data": {
    "id": 1,
    "name": "Line 1",
    "description": "Main production line",
    "displayOrder": 1,
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Production line not found |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateLineRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. findById ->
404.
3. If name changed, existsByNameIgnoreCase -> 400 'Line already exists.'.
4. Sets name (trimmed), description, displayOrder; sets active only if non-null (partial).
5. Saves, returns LineResponse. Message 'Line updated successfully.'

**Database Impact**

Read: line_master (by id + duplicate check). Write: line_master (UPDATE). 404/400 handling.

---

### `DELETE /api/lines/{id}` — Delete a production line (Super Admin only)

**Purpose** — Soft-deletes a production line by deactivating it. Only SUPER_ADMIN can perform this action.

**HTTP Method** — `DELETE`

**URL** — `/api/lines/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the production line |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Production line not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. SOFT delete: active=false (row kept).
4. Report tables FK to line_master so deactivation never blocks; the row can be re-activated via PUT.
5. Returns 204 No Content.

**Database Impact**

Read: line_master. Write: line_master (UPDATE active=false). No physical delete.

---

## 9. Master Data — Shifts

Shifts define work windows used for report attribution and the `/current` auto-detection. Deletes are **soft** (`active=false`); only `GET /api/shifts/current` filters to active shifts.

### `GET /api/shifts` — Get all shifts

**Purpose** — Fetches all shifts ordered by name.

**HTTP Method** — `GET`

**URL** — `/api/shifts`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Shifts fetched successfully)

```json
{
  "success": true,
  "message": "Shifts fetched successfully.",
  "data": [
    {
      "id": 1,
      "name": "Morning",
      "startTime": "06:00",
      "endTime": "14:00",
      "active": true
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findAllByOrderByNameAsc() -> List<ShiftResponse>. Returns ALL shifts including inactive. Message 'Shifts fetched successfully.'

**Database Impact**

Read: shifts (ORDER BY name). No writes.

---

### `POST /api/shifts` — Create a new shift

**Purpose** — Creates a new shift with the provided details and returns the created shift. Overnight shifts (startTime >= endTime) are supported.

**HTTP Method** — `POST`

**URL** — `/api/shifts`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | String | Yes | Name of the shift |
| `startTime` | String | No | Shift start time (24h). Overnight shifts supported, e.g. 22:00 |
| `endTime` | String | No | Shift end time (24h). Overnight shifts supported, e.g. 06:00 |

**Validation rules** — name: @NotBlank ('Shift name is required') + unique; startTime: LocalTime @JsonFormat('HH:mm'), optional; endTime: LocalTime @JsonFormat('HH:mm'), optional.

**Example Request**

```json
{
  "name": "Night",
  "startTime": "22:00",
  "endTime": "06:00"
}
```

**Example Response** (201 Shift created successfully)

```json
{
  "success": true,
  "message": "Shift created successfully.",
  "data": {
    "id": 1,
    "name": "Morning",
    "startTime": "06:00",
    "endTime": "14:00",
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateShiftRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. existsByNameIgnoreCase(name) -> 400 'Shift already exists.'.
3. Saves with active=true, name trimmed.
4. No overlap checking — overlapping shift windows are allowed (only name is UNIQUE).
5. 201 Created + Location: /api/shifts/{id}.

**Database Impact**

Read: shifts (duplicate check). Write: shifts (INSERT). Duplicate name -> 400/409.

---

### `GET /api/shifts/current` — Get the current shift based on the server time

**Purpose** — Resolves the shift that covers the current server time, handling overnight shifts that wrap past midnight.

**HTTP Method** — `GET`

**URL** — `/api/shifts/current`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Current shift fetched successfully)

```json
{
  "success": true,
  "message": "Current shift fetched successfully.",
  "data": {
    "id": 1,
    "name": "Morning",
    "startTime": "06:00",
    "endTime": "14:00",
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | No active shift configured |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Loads only ACTIVE shifts ordered by start_time.
3. No active shifts -> 404 'No active shift is configured. Please contact the administrator.'.
4. Picks the first shift whose window covers the current SERVER time (LocalTime.now()), handling overnight wraps; falls back to the first active shift if none covers.

**Database Impact**

Read: shifts WHERE active=true ORDER BY start_time. No writes.

---

### `GET /api/shifts/{id}` — Get shift by ID

**Purpose** — Fetches a single shift by its unique ID.

**HTTP Method** — `GET`

**URL** — `/api/shifts/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the shift |

**Example Response** (200 Shift fetched successfully)

```json
{
  "success": true,
  "message": "Shift fetched successfully.",
  "data": {
    "id": 1,
    "name": "Morning",
    "startTime": "06:00",
    "endTime": "14:00",
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Shift not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findById -> 404 'Shift not found.' (inactive rows returned too). Message 'Shift fetched successfully.'

**Database Impact**

Read: shifts (by PK). No writes.

---

### `PUT /api/shifts/{id}` — Update a shift

**Purpose** — Updates an existing shift with the provided details and returns the updated shift.

**HTTP Method** — `PUT`

**URL** — `/api/shifts/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the shift |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | String | Yes | Name of the shift |
| `startTime` | String | No | Shift start time (24h) |
| `endTime` | String | No | Shift end time (24h) |
| `active` | boolean | No | Whether the shift is active |

**Validation rules** — name: @NotBlank + unique (excluding self); startTime/endTime: LocalTime 'HH:mm', optional; active: Boolean optional.

**Example Request**

```json
{
  "name": "Night",
  "startTime": "22:00",
  "endTime": "06:00",
  "active": true
}
```

**Example Response** (200 Shift updated successfully)

```json
{
  "success": true,
  "message": "Shift updated successfully.",
  "data": {
    "id": 1,
    "name": "Morning",
    "startTime": "06:00",
    "endTime": "14:00",
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Shift not found |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateShiftRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. findById ->
404.
3. If name changed, existsByNameIgnoreCase -> 400 'Shift already exists.'.
4. Sets name, startTime, endTime; active applied only if non-null.
5. Saves. Message 'Shift updated successfully.'

**Database Impact**

Read: shifts (by id + duplicate check). Write: shifts (UPDATE). 404/400 handling.

---

### `DELETE /api/shifts/{id}` — Delete a shift (Super Admin only)

**Purpose** — Soft-deletes a shift by deactivating it. Only SUPER_ADMIN can perform this action.

**HTTP Method** — `DELETE`

**URL** — `/api/shifts/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the shift |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Shift not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. SOFT delete: active=false (row kept).
4. Report tables FK to shifts so deactivation never blocks; re-activatable via PUT.
5. 204 No Content.

**Database Impact**

Read: shifts. Write: shifts (UPDATE active=false). No physical delete.

---

## 10. Master Data — Parameters

Parameters are the measurable quality attributes per report type and form the **report template** a Super Admin configures for each predefined report type. `minValue`/`maxValue` (when `inputType=NUMBER`) drive the PASS/FAIL computation done by `ValidationService` at report-creation time. `visible` controls whether the parameter is shown on the report entry form; `defaultValue` is the value pre-filled when rendered. Deletes are **soft** (`active=false`).

### `GET /api/parameters` — Get all parameters

**Purpose** — Fetches all inspection parameters configured in the system. Supports optional pagination and filtering via the shared framework (`ParameterFilterRequest`: `page`, `size`, `sortBy`, `sortDirection`, `keyword`, `reportType`, `inputType`, `active`, `visible`); when any such param is present the `data` field is a `PageResponse<ParameterResponse>`, otherwise the legacy `ParameterResponse[]` list.

**HTTP Method** — `GET`

**URL** — `/api/parameters`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters** (all optional)

| Param | In | Type | Description |
|---|---|---|---|
| `page` | query | int | Zero-based page number (default 0) |
| `size` | query | int | Page size (default 20, max 200) |
| `sortBy` | query | string | Sort field: `id`, `parameterName`, `displayOrder`, `inputType`, `active`, `visible`, `createdAt` |
| `sortDirection` | query | string | `ASC` or `DESC` (default `DESC`) |
| `keyword` | query | string | Free-text match on parameterName / unit / testMethod |
| `reportType` | query | string | Filter by report type enum |
| `inputType` | query | string | Filter by input type enum (`NUMBER`/`TEXT`/`BOOLEAN`/`DROPDOWN`) |
| `active` | query | boolean | Filter by active status |
| `visible` | query | boolean | Filter by visible status |

**Example Response** (200 Parameters fetched successfully — legacy list)

```json
{
  "success": true,
  "message": "Parameters fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportType": "PROCESS_MONITORING",
      "parameterName": "Temperature",
      "minValue": 0.0,
      "maxValue": 100.0,
      "unit": "C",
      "testMethod": "Thermometer",
      "frequency": "HOURLY",
      "inputType": "NUMBER",
      "mandatory": true,
      "visible": true,
      "defaultValue": "25.0",
      "displayOrder": 1,
      "active": true
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ParameterResponse`, `ParameterFilterRequest`, `PageResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. No search criteria -> parameterRepository.findAll() -> List<ParameterResponse>. Returns ALL parameters including inactive; no ordering. Message 'Parameters fetched successfully.'
3. With search criteria -> SpecificationBuilder (keyword on parameterName/unit/testMethod, equality on reportType/inputType/active/visible) + PageableResolver (default sort displayOrder) -> PageResponse<ParameterResponse>.

**Database Impact**

Read: parameter_master. No writes.

---

### `POST /api/parameters` — Create a new inspection parameter

**Purpose** — Creates a new inspection parameter for a report type template and returns the created parameter.

**HTTP Method** — `POST`

**URL** — `/api/parameters`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `reportType` | String | Yes | Report type that owns this parameter Enum: `PROCESS_MONITORING`, `PDI`, `DAILY_STARTUP`, `CHEMICAL_CONSUMPTION`, `FIRST_PIECE_INSPECTION`, `DAILY_INSPECTION`. |
| `parameterName` | String | Yes | Name of the parameter |
| `minValue` | BigDecimal | No | Minimum acceptable value |
| `maxValue` | BigDecimal | No | Maximum acceptable value |
| `unit` | String | No | Unit of measurement |
| `testMethod` | String | No | Method used for testing |
| `frequency` | String | Yes | Inspection frequency Enum: `HOURLY`, `EVERY_2_HOURS`, `EVERY_4_HOURS`, `EVERY_SHIFT`, `DAILY`, `WEEKLY`, `MONTHLY`, `PER_BATCH`. |
| `inputType` | String | Yes | Type of input expected Enum: `NUMBER`, `TEXT`, `BOOLEAN`, `DROPDOWN`. |
| `mandatory` | boolean | No | Whether the parameter is mandatory |
| `visible` | boolean | No | Whether the parameter is visible in the report entry form (default true) |
| `defaultValue` | String | No | Default value pre-filled when the parameter is rendered |
| `displayOrder` | int | Yes | Display order for sorting |

**Validation rules** — reportType: @NotNull enum (PROCESS_MONITORING/PDI/DAILY_STARTUP/CHEMICAL_CONSUMPTION/FIRST_PIECE_INSPECTION/DAILY_INSPECTION); parameterName: @NotBlank + unique per report type; minValue/maxValue: BigDecimal optional; unit/testMethod: optional; frequency: @NotNull enum (HOURLY/EVERY_2_HOURS/EVERY_4_HOURS/EVERY_SHIFT/DAILY/WEEKLY/MONTHLY/PER_BATCH); inputType: @NotNull enum (NUMBER/TEXT/BOOLEAN/DROPDOWN); mandatory: Boolean optional (default true); visible: Boolean optional (default true); defaultValue: String optional; displayOrder: @NotNull + @Min(1).

**Example Request**

```json
{
  "reportType": "CHEMICAL_CONSUMPTION",
  "parameterName": "Bath Temperature",
  "minValue": 20.0,
  "maxValue": 40.0,
  "unit": "°C",
  "testMethod": "Thermometer",
  "frequency": "EVERY_SHIFT",
  "inputType": "NUMBER",
  "mandatory": true,
  "visible": true,
  "defaultValue": "25.0",
  "displayOrder": 1
}
```

**Example Response** (201 Inspection parameter created successfully)

```json
{
  "success": true,
  "message": "Parameter created successfully.",
  "data": {
    "id": 1,
    "reportType": "PROCESS_MONITORING",
    "parameterName": "Temperature",
    "minValue": 0.0,
    "maxValue": 100.0,
    "unit": "C",
    "testMethod": "Thermometer",
    "frequency": "HOURLY",
    "inputType": "NUMBER",
    "mandatory": true,
    "visible": true,
    "defaultValue": "25.0",
    "displayOrder": 1,
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateParameterRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. existsByReportTypeAndParameterNameIgnoreCase -> 400 'Parameter already exists for this report type.'.
3. Saves with active=true, mandatory default true, visible default true.
4. No min<=max check here (ValidationService applies bounds at report-entry time).
5. 201 Created + Location: /api/parameters/{id}.

**Database Impact**

Read: parameter_master (duplicate check). Write: parameter_master (INSERT). Duplicate (service) -> 400.

---

### `GET /api/parameters/report-type/{reportType}` — Get parameters by report type (the configured template)

**Purpose** — Fetches all inspection parameters configured for the given report type template.

**HTTP Method** — `GET`

**URL** — `/api/parameters/report-type/{reportType}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `reportType` | path | String (PROCESS_MONITORING,PDI,DAILY_STARTUP,CHEMICAL_CONSUMPTION,FIRST_PIECE_INSPECTION,DAILY_INSPECTION) | Yes | Report type (template) to filter parameters by |

**Example Response** (200 Parameters fetched successfully)

```json
{
  "success": true,
  "message": "Parameters fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportType": "PROCESS_MONITORING",
      "parameterName": "Temperature",
      "minValue": 0.0,
      "maxValue": 100.0,
      "unit": "C",
      "testMethod": "Thermometer",
      "frequency": "HOURLY",
      "inputType": "NUMBER",
      "mandatory": true,
      "visible": true,
      "defaultValue": "25.0",
      "displayOrder": 1,
      "active": true
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - invalid report type value |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. @PathVariable ReportType reportType (invalid enum value -> 400 type-mismatch).
3. findByReportTypeOrderByDisplayOrderAsc -> List<ParameterResponse>. Message 'Parameters fetched successfully.'

**Database Impact**

Read: parameter_master WHERE report_type ORDER BY display_order (uses idx). No writes.

---

### `GET /api/parameters/{id}` — Get parameter by ID

**Purpose** — Fetches a single inspection parameter by its unique ID.

**HTTP Method** — `GET`

**URL** — `/api/parameters/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the inspection parameter |

**Example Response** (200 Parameter fetched successfully)

```json
{
  "success": true,
  "message": "Parameter fetched successfully.",
  "data": {
    "id": 1,
    "reportType": "PROCESS_MONITORING",
    "parameterName": "Temperature",
    "minValue": 0.0,
    "maxValue": 100.0,
    "unit": "C",
    "testMethod": "Thermometer",
    "frequency": "HOURLY",
    "inputType": "NUMBER",
    "mandatory": true,
    "visible": true,
    "defaultValue": "25.0",
    "displayOrder": 1,
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Parameter not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findById -> 404 'Parameter not found.' Message 'Parameter fetched successfully.'

**Database Impact**

Read: parameter_master (by PK). No writes.

---

### `PUT /api/parameters/{id}` — Update an inspection parameter

**Purpose** — Updates an existing inspection parameter with the provided details and returns the updated parameter.

**HTTP Method** — `PUT`

**URL** — `/api/parameters/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the inspection parameter |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `parameterName` | String | Yes | Name of the parameter |
| `minValue` | BigDecimal | No | Minimum acceptable value |
| `maxValue` | BigDecimal | No | Maximum acceptable value |
| `unit` | String | No | Unit of measurement |
| `testMethod` | String | No | Method used for testing |
| `frequency` | String | Yes | Inspection frequency Enum: `HOURLY`, `EVERY_2_HOURS`, `EVERY_4_HOURS`, `EVERY_SHIFT`, `DAILY`, `WEEKLY`, `MONTHLY`, `PER_BATCH`. |
| `inputType` | String | Yes | Type of input expected Enum: `NUMBER`, `TEXT`, `BOOLEAN`, `DROPDOWN`. |
| `mandatory` | boolean | No | Whether the parameter is mandatory |
| `visible` | boolean | No | Whether the parameter is visible in the report entry form |
| `defaultValue` | String | No | Default value pre-filled when the parameter is rendered |
| `displayOrder` | int | Yes | Display order for sorting |
| `active` | boolean | No | Whether the parameter is active |

**Validation rules** — parameterName: @NotBlank + unique per report type (excluding self); minValue/maxValue: BigDecimal optional; unit/testMethod: optional; frequency: @NotNull; inputType: @NotNull; mandatory: Boolean optional; visible: Boolean optional; defaultValue: String optional; displayOrder: @NotNull + @Min(1); active: Boolean optional but IGNORED by the service.

**Example Request**

```json
{
  "parameterName": "Temperature",
  "minValue": 20.0,
  "maxValue": 100.0,
  "unit": "°C",
  "testMethod": "Visual Inspection",
  "frequency": "HOURLY",
  "inputType": "NUMBER",
  "mandatory": true,
  "visible": true,
  "defaultValue": "25.0",
  "displayOrder": 1,
  "active": true
}
```

**Example Response** (200 Parameter updated successfully)

```json
{
  "success": true,
  "message": "Parameter updated successfully.",
  "data": {
    "id": 1,
    "reportType": "PROCESS_MONITORING",
    "parameterName": "Temperature",
    "minValue": 0.0,
    "maxValue": 100.0,
    "unit": "C",
    "testMethod": "Thermometer",
    "frequency": "HOURLY",
    "inputType": "NUMBER",
    "mandatory": true,
    "visible": true,
    "defaultValue": "25.0",
    "displayOrder": 1,
    "active": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Parameter not found |
| 409 | Conflict - data constraint violation |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateParameterRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. findById ->
404.
3. Sets parameterName (trimmed), min/max, unit, inputType, displayOrder, testMethod, frequency; mandatory and visible applied only if non-null; defaultValue always applied.
4. NOTE: DTO 'active' field is never applied (dead field) — a soft-deleted parameter cannot be re-activated via this endpoint; reportType is immutable.
5. Saves. Message 'Parameter updated successfully.'

**Database Impact**

Read: parameter_master. Write: parameter_master (UPDATE). 404/400 handling.

---

### `DELETE /api/parameters/{id}` — Delete a parameter (Super Admin only)

**Purpose** — Soft-deletes an inspection parameter by deactivating it. Only SUPER_ADMIN can perform this action.

**HTTP Method** — `DELETE`

**URL** — `/api/parameters/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the inspection parameter |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Parameter not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. SOFT delete: active=false (row kept).
4. Entry tables FK to parameter_master so deactivation never blocks.
5. 204 No Content.

**Database Impact**

Read: parameter_master. Write: parameter_master (UPDATE active=false). No physical delete.

---

## 11. Report Types

A fixed, read-only catalog of the six report types served from the `ReportType` enum — no database access.

### `GET /api/report-types` — List all predefined report types (read-only catalog)

**Purpose** — Fetches the fixed, predefined list of report types that can be created in the system.

**HTTP Method** — `GET`

**URL** — `/api/report-types`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Report types fetched successfully)

```json
{
  "success": true,
  "message": "Report types fetched successfully.",
  "data": [
    {
      "code": "PROCESS_MONITORING",
      "name": "Process Monitoring"
    },
    {
      "code": "CHEMICAL_CONSUMPTION",
      "name": "Chemical Consumption"
    },
    {
      "code": "DAILY_STARTUP",
      "name": "Daily Startup Checklist"
    },
    {
      "code": "DAILY_INSPECTION",
      "name": "Daily Inspection"
    },
    {
      "code": "FIRST_PIECE_INSPECTION",
      "name": "First Piece Inspection"
    },
    {
      "code": "PDI",
      "name": "Pre Delivery Inspection"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Pure in-memory catalog — the controller builds 6 fixed {code,name} pairs from the ReportType enum (PROCESS_MONITORING, CHEMICAL_CONSUMPTION, DAILY_STARTUP, DAILY_INSPECTION, FIRST_PIECE_INSPECTION, PDI). No service/repository/DB access. Message 'Report types fetched successfully.'

**Database Impact**

None (static enum catalog).

---

## 12. Settings

Application configuration stored in the `system_settings` table, grouped by `SettingCategory`. Create/update/delete are `SUPER_ADMIN`-only; reads allow `SUPER_ADMIN`/`ADMIN`. NOTE: setting values are returned verbatim — there is no secret masking in this module. `DELETE` is a hard delete (unlike master data).

### `GET /api/settings` — Get all settings with optional search and pagination

**Purpose** — Retrieves system settings with optional keyword search on the setting key and filtering by category, returned in a paginated envelope. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/settings`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `keyword` | query | String | No | Search keyword in setting key |
| `category` | query | String | No | Filter by category |
| `page` | query | int | No | Page number (zero-based) |
| `size` | query | int | No | Page size |

**Example Response** (200 Settings fetched successfully)

```json
{
  "success": true,
  "message": "Settings fetched successfully.",
  "data": {
    "content": [
      {
        "id": 1,
        "settingKey": "app.name",
        "settingValue": "CED Ops",
        "category": "GENERAL",
        "dataType": "STRING",
        "description": "Application name",
        "isActive": true,
        "createdAt": "2026-08-02T08:00:00",
        "updatedAt": "2026-08-02T08:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Optional query params: keyword (case-insensitive match on setting key), category (valid SettingCategory name, else 400), page, size.
3. Branches: category present -> findByCategoryAndIsActiveTrue; keyword present -> findBySettingKeyContainingIgnoreCaseAndIsActiveTrue; else findByIsActiveTrue. Sorted by category, settingKey ASC.
4. Returns PageResponse<SystemSettingResponse>. NOTE: no secret masking — settingValue is returned verbatim.

**Database Impact**

Read: system_settings (active-only, paginated). No writes.

---

### `POST /api/settings` — Create a new system setting

**Purpose** — Creates a new system setting. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `POST`

**URL** — `/api/settings`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `settingKey` | String | Yes | Unique setting key |
| `settingValue` | String | Yes | Setting value |
| `category` | String | Yes | Setting category Enum: `GENERAL`, `REPORT_SETTINGS`, `NOTIFICATION_SETTINGS`, `ATTACHMENT_SETTINGS`, `SECURITY_SETTINGS`, `DASHBOARD_SETTINGS`. |
| `dataType` | String | Yes | Data type Enum: `STRING`, `INTEGER`, `LONG`, `BOOLEAN`, `DECIMAL`, `JSON`. |
| `description` | String | No | Description of the setting |

**Validation rules** — settingKey: @NotBlank + unique; settingValue: @NotBlank (type-validated); category: @NotNull enum; dataType: @NotBlank enum (STRING/INTEGER/LONG/BOOLEAN/DECIMAL/JSON); description: @Size(max=500) optional.

**Example Request**

```json
{
  "settingKey": "app.maintenance_mode",
  "settingValue": "false",
  "category": "GENERAL",
  "dataType": "BOOLEAN",
  "description": "Enable or disable maintenance mode"
}
```

**Example Response** (201 Setting created successfully)

```json
{
  "success": true,
  "message": "Setting created successfully.",
  "data": {
    "id": 1,
    "settingKey": "app.name",
    "settingValue": "CED Ops",
    "category": "GENERAL",
    "dataType": "STRING",
    "description": "Application name",
    "isActive": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 409 | Conflict - setting key already exists |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateSettingRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. existsBySettingKey -> 400 'Setting already exists with key: ...'.
3. Category and dataType parsed via enum valueOf (invalid -> 400).
4. validateValue enforces the declared dataType: INTEGER/LONG/DECIMAL parsed numerically, BOOLEAN must be 'true'/'false', JSON must start with { or [, STRING accepts anything (invalid -> 400).
5. Saves with isActive=true and manual timestamps; evicts settings cache.
6. 201 Created + Location: /api/settings/{key}.

**Database Impact**

Read: system_settings (exists check). Write: system_settings (INSERT). Duplicate -> 400/409.

---

### `GET /api/settings/all` — Get all active settings as a flat list

**Purpose** — Retrieves all active system settings as a flat list without pagination. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/settings/all`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 All active settings fetched successfully)

```json
{
  "success": true,
  "message": "Settings fetched successfully.",
  "data": [
    {
      "id": 1,
      "settingKey": "app.name",
      "settingValue": "CED Ops",
      "category": "GENERAL",
      "dataType": "STRING",
      "description": "Application name",
      "isActive": true,
      "createdAt": "2026-08-02T08:00:00",
      "updatedAt": "2026-08-02T08:00:00"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. findAllByIsActiveTrueOrderByCategoryAscSettingKeyAsc -> List<SystemSettingResponse>. Message 'Settings fetched successfully.'

**Database Impact**

Read: system_settings WHERE is_active=true ORDER BY category, setting_key. No writes.

---

### `GET /api/settings/categories` — Get all setting categories

**Purpose** — Retrieves the list of all available setting categories. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/settings/categories`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Categories fetched successfully)

```json
{
  "success": true,
  "message": "Categories fetched successfully.",
  "data": [
    "GENERAL",
    "REPORT_SETTINGS",
    "NOTIFICATION_SETTINGS",
    "ATTACHMENT_SETTINGS",
    "SECURITY_SETTINGS",
    "DASHBOARD_SETTINGS"
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Returns the 6 SettingCategory enum names (GENERAL, REPORT_SETTINGS, NOTIFICATION_SETTINGS, ATTACHMENT_SETTINGS, SECURITY_SETTINGS, DASHBOARD_SETTINGS). No DB access.

**Database Impact**

None (enum-derived).

---

### `GET /api/settings/{key}` — Get a single setting by key

**Purpose** — Retrieves a single system setting by its unique key. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/settings/{key}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `key` | path | String | Yes | Setting key |

**Example Response** (200 Setting fetched successfully)

```json
{
  "success": true,
  "message": "Setting fetched successfully.",
  "data": {
    "id": 1,
    "settingKey": "app.name",
    "settingValue": "CED Ops",
    "category": "GENERAL",
    "dataType": "STRING",
    "description": "Application name",
    "isActive": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 404 | Setting not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. findBySettingKey -> 404 'Setting not found with key: <key>'. Cached under @Cacheable('settings'). NOTE: no active filter — inactive settings are returned. Message 'Setting fetched successfully.'

**Database Impact**

Read: system_settings (by setting_key). No writes.

---

### `PUT /api/settings/{key}` — Update an existing system setting

**Purpose** — Updates the value, data type, description and active status of an existing system setting. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `PUT`

**URL** — `/api/settings/{key}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `key` | path | String | Yes | Setting key |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `settingValue` | String | Yes | New setting value |
| `dataType` | String | Yes | Data type Enum: `STRING`, `INTEGER`, `LONG`, `BOOLEAN`, `DECIMAL`, `JSON`. |
| `description` | String | No | Description of the setting |
| `isActive` | boolean | No | Is active |

**Validation rules** — settingValue: @NotBlank + type-validated; dataType: @NotBlank + @Size(max=20) enum; description: @Size(max=500) optional; isActive: Boolean optional.

**Example Request**

```json
{
  "settingValue": "true",
  "dataType": "BOOLEAN",
  "description": "Enable or disable maintenance mode",
  "isActive": true
}
```

**Example Response** (200 Setting updated successfully)

```json
{
  "success": true,
  "message": "Setting updated successfully.",
  "data": {
    "id": 1,
    "settingKey": "app.name",
    "settingValue": "CED Ops",
    "category": "GENERAL",
    "dataType": "STRING",
    "description": "Application name",
    "isActive": true,
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Setting not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateSettingRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findBySettingKey ->
404.
3. New settingValue is validated against the (new or existing) dataType.
4. dataType/description/isActive applied only when non-null.
5. Saves with updatedAt; evicts cache for key. Message 'Setting updated successfully.'

**Database Impact**

Read: system_settings. Write: system_settings (UPDATE). 404/400 handling.

---

### `PUT /api/settings/category/{category}` — Bulk update settings for a specific category

**Purpose** — Bulk updates multiple system settings belonging to a specific category in a single request. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `PUT`

**URL** — `/api/settings/category/{category}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `category` | path | String | Yes | Setting category |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `settings` | List<BulkUpdateItem> | Yes | List of settings to update |

**Validation rules** — settings: @Valid @NotEmpty list; each item: {settingKey @NotBlank, settingValue @NotBlank}.

**Example Request**

```json
{
  "settings": [
    {
      "settingKey": "app.maintenance_mode",
      "settingValue": "true"
    },
    {
      "settingKey": "app.session_timeout_minutes",
      "settingValue": "30"
    }
  ]
}
```

**Example Response** (200 Settings updated successfully)

```json
{
  "success": true,
  "message": "Settings updated successfully.",
  "data": [
    {
      "id": 1,
      "settingKey": "app.name",
      "settingValue": "CED Ops",
      "category": "GENERAL",
      "dataType": "STRING",
      "description": "Application name",
      "isActive": true,
      "createdAt": "2026-08-02T08:00:00",
      "updatedAt": "2026-08-02T08:00:00"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Category not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `BulkUpdateSettingsRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. parseCategory -> 400 if invalid.
3. Loads ACTIVE settings of the category; for each BulkUpdateItem the value is validated against that setting's dataType.
4. Keys not present in the category are silently skipped.
5. saveAll + evicts the whole 'settings' cache; returns the updated active list.
6. Only values change here — dataType/description/isActive are not touchable.

**Database Impact**

Read: system_settings (by category + active). Write: system_settings (UPDATE for matched keys).

---

### `DELETE /api/settings/{key}` — Delete a system setting

**Purpose** — Permanently deletes a system setting by its unique key. Only accessible to users with the SUPER_ADMIN role.

**HTTP Method** — `DELETE`

**URL** — `/api/settings/{key}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `key` | path | String | Yes | Setting key |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Setting not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findBySettingKey ->
404.
3. HARD delete (row physically removed) — unlike lines/shifts/parameters. Evicts cache for key.
4. 204 No Content.

**Database Impact**

Read: system_settings. Write: system_settings (DELETE row - physical).

---

## 13. Reports — Process Monitoring

Path base `/api/reports/process-monitoring`; number prefix `PMR`; tables `process_monitoring_reports` / `process_monitoring_entries`. Contract follows section 5 exactly.

### `POST /api/reports/process-monitoring` — Create process monitoring report

**Purpose** — Creates a new process monitoring report in DRAFT status

**HTTP Method** — `POST`

**URL** — `/api/reports/process-monitoring`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `reportDate` | LocalDate | Yes | Date of the monitoring report |
| `shiftId` | long | No | ID of the shift. Omitted to auto-detect from current time |
| `lineId` | long | Yes | ID of the production line |
| `remarks` | String | No | Additional remarks |
| `entries` | List<ProcessMonitoringEntryRequest> | Yes | List of monitoring entries |

**Validation rules** — reportDate: @NotNull ('Report date is required') LocalDate; shiftId: Long optional (null -> auto-detect active shift covering now); lineId: @NotNull ('Line ID is required'); remarks: @Size(max=1000) optional; entries: @Valid @NotEmpty ('At least one entry is required').

**Example Request**

```json
{
  "reportDate": "2025-01-15",
  "shiftId": 1,
  "lineId": 1,
  "remarks": "All processes running normally",
  "entries": [
    {
      "parameterId": 1,
      "observedValue": "12.5",
      "remark": "Within specification"
    }
  ]
}
```

**Example Response** (201 Process monitoring report created successfully)

```json
{
  "success": true,
  "message": "Report created successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PMR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateProcessMonitoringRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. nextReportNumber() = reportRepository.count()+1, then ReportNumberGenerator generates PMR-{yyyyMMdd}-%05d (e.g. PMR-20260802-00001).
3. status=DRAFT, createdBy=currentUser, shift = resolveShift(shiftId): if shiftId null, ShiftService picks the active shift covering now (fallback: first active); line resolved by lineId -> 404 'Line not found.' if missing.
4. For each entry: parameter resolved by parameterId -> 404 'Parameter not found.'; inspectionResult computed once by ValidationService.validate — if inputType==NUMBER the BigDecimal value is compared against min/max (below min or above max -> FAIL, non-numeric -> FAIL), blank value or non-NUMBER input -> NOT_APPLICABLE.
5. Saves the report header then all entries; returns 201 Created + Location header.

**Database Impact**

Read: users (current user), line_master, shifts (auto-detect when shiftId null), N x parameter_master, COUNT(*) on process_monitoring_reports (for the report number). Write: process_monitoring_reports (INSERT header) + process_monitoring_entries (INSERT one row per entry incl. computed inspectionResult). No audit/notification write.

---

### `GET /api/reports/process-monitoring` — Fetch all process monitoring reports

**Purpose** — Returns a list of all process monitoring reports. Supports the shared pagination/filtering contract (`ReportFilterRequest`: `page`, `size`, `sortBy`, `sortDirection`, `keyword`, `reportNumber`, `status`, `shiftId`, `lineId`, `dateFrom`, `dateTo`, `approved`); when any such param is present the `data` field is a `PageResponse<ProcessMonitoringResponse>`, otherwise the legacy list.

**HTTP Method** — `GET`

**URL** — `/api/reports/process-monitoring`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Process monitoring reports fetched successfully)

```json
{
  "success": true,
  "message": "Reports fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportNumber": "PMR-20260802-00001",
      "reportDate": "2026-08-02",
      "shift": "Morning",
      "line": "Line 1",
      "createdBy": "John Doe",
      "approvedBy": null,
      "status": "DRAFT",
      "remarks": null,
      "approvedAt": null,
      "createdAt": "2026-08-02T08:00:00",
      "entries": [
        {
          "id": 1,
          "parameterId": 1,
          "parameterName": "Temperature",
          "minValue": 0.0,
          "maxValue": 100.0,
          "observedValue": "25.5",
          "unit": "C",
          "inspectionResult": "PASS",
          "remark": null
        }
      ]
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findAllWithDetails (@EntityGraph on shift/line/createdBy/approvedBy, ORDER BY id DESC) plus entries fetched in one pass by report ids.
3. Returns List<Response> ordered newest first. No filter parameters.

**Database Impact**

Read: process_monitoring_reports (entity graph over shift/line/createdBy/approvedBy) + process_monitoring_entries (IN report ids). No writes.

---

### `GET /api/reports/process-monitoring/{id}` — Fetch process monitoring report by id

**Purpose** — Returns a single process monitoring report by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/reports/process-monitoring/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the process monitoring report |

**Example Response** (200 Process monitoring report fetched successfully)

```json
{
  "success": true,
  "message": "Report fetched successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PMR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Process monitoring report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByIdWithDetails -> 404 'Process monitoring report not found.' if missing; entries fetched via findByReport (@EntityGraph parameter).
3. Returns the report with all entries and their stored inspectionResult.

**Database Impact**

Read: process_monitoring_reports (by PK) + process_monitoring_entries (by report). No writes.

---

### `POST /api/reports/process-monitoring/{id}/submit` — Submit process monitoring report for approval

**Purpose** — Submits a DRAFT process monitoring report for approval

**HTTP Method** — `POST`

**URL** — `/api/reports/process-monitoring/{id}/submit`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the process monitoring report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Submission remarks |

**Example Request**

```json
{
  "remarks": "Report is ready for review"
}
```

**Example Response** (200 Process monitoring report submitted successfully)

```json
{
  "success": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PMR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Process monitoring report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `SubmitReportRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Guard: status must be DRAFT, else 400 'Only draft reports can be submitted.'.
3. Sets status=SUBMITTED; if the optional remarks is provided it overwrites the report remarks.
4. NO submittedBy/submittedAt is recorded (columns do not exist).
5. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: process_monitoring_reports (status check) + process_monitoring_entries. Write: process_monitoring_reports (UPDATE status=SUBMITTED, remarks, updated_at). No submitted stamp. No audit write.

---

### `POST /api/reports/process-monitoring/{id}/approve` — Approve process monitoring report

**Purpose** — Approves a SUBMITTED process monitoring report

**HTTP Method** — `POST`

**URL** — `/api/reports/process-monitoring/{id}/approve`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the process monitoring report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval or rejection remarks |

**Example Request**

```json
{
  "remarks": "Approved - all checks passed"
}
```

**Example Response** (200 Process monitoring report approved successfully)

```json
{
  "success": true,
  "message": "Report approved successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PMR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": "Admin User",
    "status": "APPROVED",
    "remarks": "Approved",
    "approvedAt": "2026-08-02T10:30:00",
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Process monitoring report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveReportRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=APPROVED, approvedBy=currentUser, approvedAt=now; optional remarks overwrite the report remarks.
4. Saves; returns the report + entries.
5. No notification or audit row is written.

**Database Impact**

Read: process_monitoring_reports. Write: process_monitoring_reports (UPDATE status=APPROVED, approved_by, approved_at, remarks, updated_at). No audit write.

---

### `POST /api/reports/process-monitoring/{id}/reject` — Reject process monitoring report

**Purpose** — Rejects a SUBMITTED process monitoring report

**HTTP Method** — `POST`

**URL** — `/api/reports/process-monitoring/{id}/reject`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the process monitoring report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval or rejection remarks |

**Example Request**

```json
{
  "remarks": "Rejected - corrective action required"
}
```

**Example Response** (200 Process monitoring report rejected successfully)

```json
{
  "success": true,
  "message": "Report rejected successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PMR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "REJECTED",
    "remarks": "Out of tolerance",
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Process monitoring report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveReportRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=REJECTED; approvedBy/approvedAt are NOT set. The rejection reason is stored in the shared remarks field (overwritten when provided).
4. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: process_monitoring_reports. Write: process_monitoring_reports (UPDATE status=REJECTED, remarks, updated_at). approved_by/approved_at not set. No audit write.

---

### `DELETE /api/reports/process-monitoring/{id}` — Delete draft process monitoring report

**Purpose** — Deletes a DRAFT process monitoring report by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/reports/process-monitoring/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the process monitoring report |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Process monitoring report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Guard: status must be DRAFT, else 400 'Only draft reports can be deleted.'.
3. Physical delete: entries deleted by report_id first, then the report row.
4. Returns 204 No Content.

**Database Impact**

Write: process_monitoring_entries (DELETE entries by report_id) then process_monitoring_reports (DELETE header - physical). Only DRAFT allowed, else 400 and no change.

---

## 14. Reports — Chemical Consumption

Path base `/api/reports/chemical-consumption`; number prefix `CCR`; tables `chemical_consumption_reports` / `chemical_consumption_entries`. Contract follows section 5 exactly.

### `POST /api/reports/chemical-consumption` — Create chemical consumption report

**Purpose** — Creates a new chemical consumption report in DRAFT status

**HTTP Method** — `POST`

**URL** — `/api/reports/chemical-consumption`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `reportDate` | LocalDate | Yes | Date of the report |
| `shiftId` | long | No | ID of the shift. Omitted to auto-detect from current time |
| `lineId` | long | Yes | ID of the production line |
| `remarks` | String | No | Additional remarks |
| `entries` | List<ChemicalConsumptionEntryRequest> | Yes | List of consumption entries |

**Validation rules** — Same shape as CreateProcessMonitoringRequest (reportDate @NotNull, shiftId optional auto-detect, lineId @NotNull, remarks @Size(max=1000), entries @Valid @NotEmpty).

**Example Request**

```json
{
  "reportDate": "2025-01-15",
  "shiftId": 1,
  "lineId": 1,
  "remarks": "All chemicals consumed within limit",
  "entries": [
    {
      "parameterId": 1,
      "observedValue": "25.5",
      "remark": "Within specification"
    }
  ]
}
```

**Example Response** (201 Chemical consumption report created successfully)

```json
{
  "success": true,
  "message": "Report created successfully.",
  "data": {
    "id": 1,
    "reportNumber": "CCR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateChemicalConsumptionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. nextReportNumber() = reportRepository.count()+1, then ReportNumberGenerator generates CCR-{yyyyMMdd}-%05d (e.g. PMR-20260802-00001).
3. status=DRAFT, createdBy=currentUser, shift = resolveShift(shiftId): if shiftId null, ShiftService picks the active shift covering now (fallback: first active); line resolved by lineId -> 404 'Line not found.' if missing.
4. For each entry: parameter resolved by parameterId -> 404 'Parameter not found.'; inspectionResult computed once by ValidationService.validate — if inputType==NUMBER the BigDecimal value is compared against min/max (below min or above max -> FAIL, non-numeric -> FAIL), blank value or non-NUMBER input -> NOT_APPLICABLE.
5. Saves the report header then all entries; returns 201 Created + Location header.

**Database Impact**

Read: users (current user), line_master, shifts (auto-detect when shiftId null), N x parameter_master, COUNT(*) on chemical_consumption_reports (for the report number). Write: chemical_consumption_reports (INSERT header) + chemical_consumption_entries (INSERT one row per entry incl. computed inspectionResult). No audit/notification write.

---

### `GET /api/reports/chemical-consumption` — Fetch all chemical consumption reports

**Purpose** — Returns a list of all chemical consumption reports. Supports the shared pagination/filtering contract (`ReportFilterRequest`); when any such param is present the `data` field is a `PageResponse<ChemicalConsumptionResponse>`, otherwise the legacy list.

**HTTP Method** — `GET`

**URL** — `/api/reports/chemical-consumption`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Chemical consumption reports fetched successfully)

```json
{
  "success": true,
  "message": "Reports fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportNumber": "CCR-20260802-00001",
      "reportDate": "2026-08-02",
      "shift": "Morning",
      "line": "Line 1",
      "createdBy": "John Doe",
      "approvedBy": null,
      "status": "DRAFT",
      "remarks": null,
      "approvedAt": null,
      "createdAt": "2026-08-02T08:00:00",
      "entries": [
        {
          "id": 1,
          "parameterId": 1,
          "parameterName": "Temperature",
          "minValue": 0.0,
          "maxValue": 100.0,
          "observedValue": "25.5",
          "unit": "C",
          "inspectionResult": "PASS",
          "remark": null
        }
      ]
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findAllWithDetails (@EntityGraph on shift/line/createdBy/approvedBy, ORDER BY id DESC) plus entries fetched in one pass by report ids.
3. Returns List<Response> ordered newest first. No filter parameters.

**Database Impact**

Read: chemical_consumption_reports (entity graph over shift/line/createdBy/approvedBy) + chemical_consumption_entries (IN report ids). No writes.

---

### `GET /api/reports/chemical-consumption/{id}` — Fetch chemical consumption report by id

**Purpose** — Returns a single chemical consumption report by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/reports/chemical-consumption/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the chemical consumption report |

**Example Response** (200 Chemical consumption report fetched successfully)

```json
{
  "success": true,
  "message": "Report fetched successfully.",
  "data": {
    "id": 1,
    "reportNumber": "CCR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Chemical consumption report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByIdWithDetails -> 404 'Chemical consumption report not found.' if missing; entries fetched via findByReport (@EntityGraph parameter).
3. Returns the report with all entries and their stored inspectionResult.

**Database Impact**

Read: chemical_consumption_reports (by PK) + chemical_consumption_entries (by report). No writes.

---

### `POST /api/reports/chemical-consumption/{id}/submit` — Submit chemical consumption report for approval

**Purpose** — Submits a DRAFT chemical consumption report for approval

**HTTP Method** — `POST`

**URL** — `/api/reports/chemical-consumption/{id}/submit`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the chemical consumption report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Submission remarks |

**Validation rules** — remarks: @Size(max=1000) optional.

**Example Request**

```json
{
  "remarks": "Ready for review"
}
```

**Example Response** (200 Chemical consumption report submitted successfully)

```json
{
  "success": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": 1,
    "reportNumber": "CCR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Chemical consumption report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `SubmitChemicalConsumptionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Guard: status must be DRAFT, else 400 'Only draft reports can be submitted.'.
3. Sets status=SUBMITTED; if the optional remarks is provided it overwrites the report remarks.
4. NO submittedBy/submittedAt is recorded (columns do not exist).
5. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: chemical_consumption_reports (status check) + chemical_consumption_entries. Write: chemical_consumption_reports (UPDATE status=SUBMITTED, remarks, updated_at). No submitted stamp. No audit write.

---

### `POST /api/reports/chemical-consumption/{id}/approve` — Approve chemical consumption report

**Purpose** — Approves a SUBMITTED chemical consumption report

**HTTP Method** — `POST`

**URL** — `/api/reports/chemical-consumption/{id}/approve`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the chemical consumption report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Approved - all consumption within specification"
}
```

**Example Response** (200 Chemical consumption report approved successfully)

```json
{
  "success": true,
  "message": "Report approved successfully.",
  "data": {
    "id": 1,
    "reportNumber": "CCR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": "Admin User",
    "status": "APPROVED",
    "remarks": "Approved",
    "approvedAt": "2026-08-02T10:30:00",
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Chemical consumption report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveChemicalConsumptionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=APPROVED, approvedBy=currentUser, approvedAt=now; optional remarks overwrite the report remarks.
4. Saves; returns the report + entries.
5. No notification or audit row is written.

**Database Impact**

Read: chemical_consumption_reports. Write: chemical_consumption_reports (UPDATE status=APPROVED, approved_by, approved_at, remarks, updated_at). No audit write.

---

### `POST /api/reports/chemical-consumption/{id}/reject` — Reject chemical consumption report

**Purpose** — Rejects a SUBMITTED chemical consumption report

**HTTP Method** — `POST`

**URL** — `/api/reports/chemical-consumption/{id}/reject`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the chemical consumption report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Rejected - values out of specification"
}
```

**Example Response** (200 Chemical consumption report rejected successfully)

```json
{
  "success": true,
  "message": "Report rejected successfully.",
  "data": {
    "id": 1,
    "reportNumber": "CCR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "REJECTED",
    "remarks": "Out of tolerance",
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Chemical consumption report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveChemicalConsumptionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=REJECTED; approvedBy/approvedAt are NOT set. The rejection reason is stored in the shared remarks field (overwritten when provided).
4. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: chemical_consumption_reports. Write: chemical_consumption_reports (UPDATE status=REJECTED, remarks, updated_at). approved_by/approved_at not set. No audit write.

---

### `DELETE /api/reports/chemical-consumption/{id}` — Delete draft chemical consumption report

**Purpose** — Deletes a DRAFT chemical consumption report by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/reports/chemical-consumption/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the chemical consumption report |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Chemical consumption report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Guard: status must be DRAFT, else 400 'Only draft reports can be deleted.'.
3. Physical delete: entries deleted by report_id first, then the report row.
4. Returns 204 No Content.

**Database Impact**

Write: chemical_consumption_entries (DELETE entries by report_id) then chemical_consumption_reports (DELETE header - physical). Only DRAFT allowed, else 400 and no change.

---

## 15. Reports — Daily Startup

> **Status: implemented (completed).** Built on the frozen report engine
> (`AbstractReportService`, `BaseReportMapper`, `ReportTypeMetadata`).

Path base `/api/reports/daily-startup`; number prefix `DSR`; tables `daily_startup_reports` / `daily_startup_entries`. Contract follows section 5 exactly.

### `POST /api/reports/daily-startup` — Create daily startup report

**Purpose** — Creates a new daily startup report in DRAFT status

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-startup`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `reportDate` | LocalDate | Yes | Date of the startup report |
| `shiftId` | long | No | ID of the shift. Omitted to auto-detect from current time |
| `lineId` | long | Yes | ID of the production line |
| `remarks` | String | No | Additional remarks |
| `entries` | List<DailyStartupEntryRequest> | Yes | List of startup check entries |

**Validation rules** — Same shape as CreateProcessMonitoringRequest (reportDate @NotNull, shiftId optional auto-detect, lineId @NotNull, remarks @Size(max=1000), entries @Valid @NotEmpty).

**Example Request**

```json
{
  "reportDate": "2025-01-15",
  "shiftId": 1,
  "lineId": 1,
  "remarks": "Startup completed successfully",
  "entries": [
    {
      "parameterId": 1,
      "observedValue": "OK",
      "remark": "Machine ready"
    }
  ]
}
```

**Example Response** (201 Daily startup report created successfully)

```json
{
  "success": true,
  "message": "Report created successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DSR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateDailyStartupRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. nextReportNumber() = reportRepository.count()+1, then ReportNumberGenerator generates DSR-{yyyyMMdd}-%05d (e.g. PMR-20260802-00001).
3. status=DRAFT, createdBy=currentUser, shift = resolveShift(shiftId): if shiftId null, ShiftService picks the active shift covering now (fallback: first active); line resolved by lineId -> 404 'Line not found.' if missing.
4. For each entry: parameter resolved by parameterId -> 404 'Parameter not found.'; inspectionResult computed once by ValidationService.validate — if inputType==NUMBER the BigDecimal value is compared against min/max (below min or above max -> FAIL, non-numeric -> FAIL), blank value or non-NUMBER input -> NOT_APPLICABLE.
5. Saves the report header then all entries; returns 201 Created + Location header.

**Database Impact**

Read: users (current user), line_master, shifts (auto-detect when shiftId null), N x parameter_master, COUNT(*) on daily_startup_reports (for the report number). Write: daily_startup_reports (INSERT header) + daily_startup_entries (INSERT one row per entry incl. computed inspectionResult). No audit/notification write.

---

### `GET /api/reports/daily-startup` — Fetch all daily startup reports

**Purpose** — Returns a list of all daily startup reports. Supports the shared pagination/filtering contract (`ReportFilterRequest`); when any such param is present the `data` field is a `PageResponse<DailyStartupResponse>`, otherwise the legacy list.

**HTTP Method** — `GET`

**URL** — `/api/reports/daily-startup`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Daily startup reports fetched successfully)

```json
{
  "success": true,
  "message": "Reports fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportNumber": "DSR-20260802-00001",
      "reportDate": "2026-08-02",
      "shift": "Morning",
      "line": "Line 1",
      "createdBy": "John Doe",
      "approvedBy": null,
      "status": "DRAFT",
      "remarks": null,
      "approvedAt": null,
      "createdAt": "2026-08-02T08:00:00",
      "entries": [
        {
          "id": 1,
          "parameterId": 1,
          "parameterName": "Temperature",
          "minValue": 0.0,
          "maxValue": 100.0,
          "observedValue": "25.5",
          "unit": "C",
          "inspectionResult": "PASS",
          "remark": null
        }
      ]
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findAllWithDetails (@EntityGraph on shift/line/createdBy/approvedBy, ORDER BY id DESC) plus entries fetched in one pass by report ids.
3. Returns List<Response> ordered newest first. No filter parameters.

**Database Impact**

Read: daily_startup_reports (entity graph over shift/line/createdBy/approvedBy) + daily_startup_entries (IN report ids). No writes.

---

### `GET /api/reports/daily-startup/{id}` — Fetch daily startup report by id

**Purpose** — Returns a single daily startup report by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/reports/daily-startup/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily startup report |

**Example Response** (200 Daily startup report fetched successfully)

```json
{
  "success": true,
  "message": "Report fetched successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DSR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Daily startup report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByIdWithDetails -> 404 'Daily startup report not found.' if missing; entries fetched via findByReport (@EntityGraph parameter).
3. Returns the report with all entries and their stored inspectionResult.

**Database Impact**

Read: daily_startup_reports (by PK) + daily_startup_entries (by report). No writes.

---

### `POST /api/reports/daily-startup/{id}/submit` — Submit daily startup report for approval

**Purpose** — Submits a DRAFT daily startup report for approval

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-startup/{id}/submit`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily startup report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Submission remarks |

**Validation rules** — remarks: @Size(max=1000) optional.

**Example Request**

```json
{
  "remarks": "Ready for review"
}
```

**Example Response** (200 Daily startup report submitted successfully)

```json
{
  "success": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DSR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Daily startup report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `SubmitDailyStartupRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Guard: status must be DRAFT, else 400 'Only draft reports can be submitted.'.
3. Sets status=SUBMITTED; if the optional remarks is provided it overwrites the report remarks.
4. NO submittedBy/submittedAt is recorded (columns do not exist).
5. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: daily_startup_reports (status check) + daily_startup_entries. Write: daily_startup_reports (UPDATE status=SUBMITTED, remarks, updated_at). No submitted stamp. No audit write.

---

### `POST /api/reports/daily-startup/{id}/approve` — Approve daily startup report

**Purpose** — Approves a SUBMITTED daily startup report

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-startup/{id}/approve`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily startup report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Approved - startup checks passed"
}
```

**Example Response** (200 Daily startup report approved successfully)

```json
{
  "success": true,
  "message": "Report approved successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DSR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": "Admin User",
    "status": "APPROVED",
    "remarks": "Approved",
    "approvedAt": "2026-08-02T10:30:00",
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Daily startup report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveDailyStartupRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=APPROVED, approvedBy=currentUser, approvedAt=now; optional remarks overwrite the report remarks.
4. Saves; returns the report + entries.
5. No notification or audit row is written.

**Database Impact**

Read: daily_startup_reports. Write: daily_startup_reports (UPDATE status=APPROVED, approved_by, approved_at, remarks, updated_at). No audit write.

---

### `POST /api/reports/daily-startup/{id}/reject` — Reject daily startup report

**Purpose** — Rejects a SUBMITTED daily startup report

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-startup/{id}/reject`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily startup report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Rejected - startup checks failed"
}
```

**Example Response** (200 Daily startup report rejected successfully)

```json
{
  "success": true,
  "message": "Report rejected successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DSR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "REJECTED",
    "remarks": "Out of tolerance",
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Daily startup report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveDailyStartupRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=REJECTED; approvedBy/approvedAt are NOT set. The rejection reason is stored in the shared remarks field (overwritten when provided).
4. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: daily_startup_reports. Write: daily_startup_reports (UPDATE status=REJECTED, remarks, updated_at). approved_by/approved_at not set. No audit write.

---

### `DELETE /api/reports/daily-startup/{id}` — Delete draft daily startup report

**Purpose** — Deletes a DRAFT daily startup report by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/reports/daily-startup/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily startup report |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Daily startup report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Guard: status must be DRAFT, else 400 'Only draft reports can be deleted.'.
3. Physical delete: entries deleted by report_id first, then the report row.
4. Returns 204 No Content.

**Database Impact**

Write: daily_startup_entries (DELETE entries by report_id) then daily_startup_reports (DELETE header - physical). Only DRAFT allowed, else 400 and no change.

---

## 16. Reports — Daily Inspection

Path base `/api/reports/daily-inspection`; number prefix `DIR`; tables `daily_inspection_reports` / `daily_inspection_entries`. Contract follows section 5 exactly. Extra header fields: `inspectorName` (max 200), `correctiveAction` (max 1000) — both optional.

### `POST /api/reports/daily-inspection` — Create daily inspection report

**Purpose** — Creates a new daily inspection report in DRAFT status

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-inspection`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `reportDate` | LocalDate | Yes | Date of the inspection report |
| `shiftId` | long | No | ID of the shift. Omitted to auto-detect from current time |
| `lineId` | long | Yes | ID of the production line |
| `inspectorName` | String | No | Name of the inspector |
| `correctiveAction` | String | No | Corrective action taken |
| `remarks` | String | No | Additional remarks |
| `entries` | List<DailyInspectionEntryRequest> | Yes | List of inspection entries |

**Validation rules** — reportDate: @NotNull LocalDate; shiftId: optional (auto-detect); lineId: @NotNull; remarks: @Size(max=1000); inspectorName: @Size(max=200) optional; correctiveAction: @Size(max=1000) optional; entries: @Valid @NotEmpty.

**Example Request**

```json
{
  "reportDate": "2025-01-15",
  "shiftId": 1,
  "lineId": 1,
  "inspectorName": "Jane Smith",
  "correctiveAction": "Re-adjusted the machine",
  "remarks": "All checks completed",
  "entries": [
    {
      "parameterId": 1,
      "observedValue": "12.5",
      "remark": "Within specification"
    }
  ]
}
```

**Example Response** (201 Daily inspection report created successfully)

```json
{
  "success": true,
  "message": "Report created successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DIR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "inspectorName": "John Doe",
    "correctiveAction": "None"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateDailyInspectionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. nextReportNumber() = reportRepository.count()+1, then ReportNumberGenerator generates DIR-{yyyyMMdd}-%05d (e.g. PMR-20260802-00001).
3. status=DRAFT, createdBy=currentUser, shift = resolveShift(shiftId): if shiftId null, ShiftService picks the active shift covering now (fallback: first active); line resolved by lineId -> 404 'Line not found.' if missing.
4. For each entry: parameter resolved by parameterId -> 404 'Parameter not found.'; inspectionResult computed once by ValidationService.validate — if inputType==NUMBER the BigDecimal value is compared against min/max (below min or above max -> FAIL, non-numeric -> FAIL), blank value or non-NUMBER input -> NOT_APPLICABLE.
5. Saves the report header then all entries; returns 201 Created + Location header. The DailyInspection report also records optional inspectorName and correctiveAction fields at creation.

**Database Impact**

Read: users (current user), line_master, shifts (auto-detect when shiftId null), N x parameter_master, COUNT(*) on daily_inspection_reports (for the report number). Write: daily_inspection_reports (INSERT header) + daily_inspection_entries (INSERT one row per entry incl. computed inspectionResult). No audit/notification write.

---

### `GET /api/reports/daily-inspection` — Fetch all daily inspection reports

**Purpose** — Returns a list of all daily inspection reports. Supports the shared pagination/filtering contract (`ReportFilterRequest`); when any such param is present the `data` field is a `PageResponse<DailyInspectionResponse>`, otherwise the legacy list.

**HTTP Method** — `GET`

**URL** — `/api/reports/daily-inspection`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Daily inspection reports fetched successfully)

```json
{
  "success": true,
  "message": "Reports fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportNumber": "DIR-20260802-00001",
      "reportDate": "2026-08-02",
      "shift": "Morning",
      "line": "Line 1",
      "createdBy": "John Doe",
      "approvedBy": null,
      "status": "DRAFT",
      "remarks": null,
      "approvedAt": null,
      "createdAt": "2026-08-02T08:00:00",
      "entries": [
        {
          "id": 1,
          "parameterId": 1,
          "parameterName": "Temperature",
          "minValue": 0.0,
          "maxValue": 100.0,
          "observedValue": "25.5",
          "unit": "C",
          "inspectionResult": "PASS",
          "remark": null
        }
      ],
      "inspectorName": "John Doe",
      "correctiveAction": "None"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findAllWithDetails (@EntityGraph on shift/line/createdBy/approvedBy, ORDER BY id DESC) plus entries fetched in one pass by report ids.
3. Returns List<Response> ordered newest first. No filter parameters.

**Database Impact**

Read: daily_inspection_reports (entity graph over shift/line/createdBy/approvedBy) + daily_inspection_entries (IN report ids). No writes.

---

### `GET /api/reports/daily-inspection/{id}` — Fetch daily inspection report by id

**Purpose** — Returns a single daily inspection report by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/reports/daily-inspection/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily inspection report |

**Example Response** (200 Daily inspection report fetched successfully)

```json
{
  "success": true,
  "message": "Report fetched successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DIR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "inspectorName": "John Doe",
    "correctiveAction": "None"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Daily inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByIdWithDetails -> 404 'Daily inspection report not found.' if missing; entries fetched via findByReport (@EntityGraph parameter).
3. Returns the report with all entries and their stored inspectionResult.

**Database Impact**

Read: daily_inspection_reports (by PK) + daily_inspection_entries (by report). No writes.

---

### `POST /api/reports/daily-inspection/{id}/submit` — Submit daily inspection report for approval

**Purpose** — Submits a DRAFT daily inspection report for approval

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-inspection/{id}/submit`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Submission remarks |

**Validation rules** — remarks: @Size(max=1000) optional.

**Example Request**

```json
{
  "remarks": "Ready for review"
}
```

**Example Response** (200 Daily inspection report submitted successfully)

```json
{
  "success": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DIR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "inspectorName": "John Doe",
    "correctiveAction": "None"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Daily inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `SubmitDailyInspectionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Guard: status must be DRAFT, else 400 'Only draft reports can be submitted.'.
3. Sets status=SUBMITTED; if the optional remarks is provided it overwrites the report remarks.
4. NO submittedBy/submittedAt is recorded (columns do not exist).
5. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: daily_inspection_reports (status check) + daily_inspection_entries. Write: daily_inspection_reports (UPDATE status=SUBMITTED, remarks, updated_at). No submitted stamp. No audit write.

---

### `POST /api/reports/daily-inspection/{id}/approve` — Approve daily inspection report

**Purpose** — Approves a SUBMITTED daily inspection report

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-inspection/{id}/approve`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Approved - all checks passed"
}
```

**Example Response** (200 Daily inspection report approved successfully)

```json
{
  "success": true,
  "message": "Report approved successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DIR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": "Admin User",
    "status": "APPROVED",
    "remarks": "Approved",
    "approvedAt": "2026-08-02T10:30:00",
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "inspectorName": "John Doe",
    "correctiveAction": "None"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Daily inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveDailyInspectionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=APPROVED, approvedBy=currentUser, approvedAt=now; optional remarks overwrite the report remarks.
4. Saves; returns the report + entries.
5. No notification or audit row is written.

**Database Impact**

Read: daily_inspection_reports. Write: daily_inspection_reports (UPDATE status=APPROVED, approved_by, approved_at, remarks, updated_at). No audit write.

---

### `POST /api/reports/daily-inspection/{id}/reject` — Reject daily inspection report

**Purpose** — Rejects a SUBMITTED daily inspection report

**HTTP Method** — `POST`

**URL** — `/api/reports/daily-inspection/{id}/reject`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Rejected - measurements out of specification"
}
```

**Example Response** (200 Daily inspection report rejected successfully)

```json
{
  "success": true,
  "message": "Report rejected successfully.",
  "data": {
    "id": 1,
    "reportNumber": "DIR-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "REJECTED",
    "remarks": "Out of tolerance",
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "inspectorName": "John Doe",
    "correctiveAction": "None"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Daily inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveDailyInspectionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=REJECTED; approvedBy/approvedAt are NOT set. The rejection reason is stored in the shared remarks field (overwritten when provided).
4. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: daily_inspection_reports. Write: daily_inspection_reports (UPDATE status=REJECTED, remarks, updated_at). approved_by/approved_at not set. No audit write.

---

### `DELETE /api/reports/daily-inspection/{id}` — Delete draft daily inspection report

**Purpose** — Deletes a DRAFT daily inspection report by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/reports/daily-inspection/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the daily inspection report |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Daily inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Guard: status must be DRAFT, else 400 'Only draft reports can be deleted.'.
3. Physical delete: entries deleted by report_id first, then the report row.
4. Returns 204 No Content.

**Database Impact**

Write: daily_inspection_entries (DELETE entries by report_id) then daily_inspection_reports (DELETE header - physical). Only DRAFT allowed, else 400 and no change.

---

## 17. Reports — First Piece Inspection

Path base `/api/reports/first-piece-inspection`; number prefix `FPI`; tables `first_piece_inspection_reports` / `first_piece_inspection_entries`. Contract follows section 5 exactly. Extra header fields: `productCastingNumber`, `operatorName`, `inspectorName` (each max 200, optional).

### `POST /api/reports/first-piece-inspection` — Create first piece inspection report

**Purpose** — Creates a new first piece inspection report in DRAFT status

**HTTP Method** — `POST`

**URL** — `/api/reports/first-piece-inspection`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `reportDate` | LocalDate | Yes | Date of the inspection report |
| `shiftId` | long | No | ID of the shift. Omitted to auto-detect from current time |
| `lineId` | long | Yes | ID of the production line |
| `productCastingNumber` | String | No | Product casting number |
| `operatorName` | String | No | Name of the operator |
| `inspectorName` | String | No | Name of the inspector |
| `remarks` | String | No | Additional remarks |
| `entries` | List<FirstPieceInspectionEntryRequest> | Yes | List of inspection entries |

**Validation rules** — reportDate: @NotNull LocalDate; shiftId: optional (auto-detect); lineId: @NotNull; remarks: @Size(max=1000); productCastingNumber: @Size(max=200) optional; operatorName: @Size(max=200) optional; inspectorName: @Size(max=200) optional; entries: @Valid @NotEmpty.

**Example Request**

```json
{
  "reportDate": "2025-01-15",
  "shiftId": 1,
  "lineId": 1,
  "productCastingNumber": "CAST-001",
  "operatorName": "John Doe",
  "inspectorName": "Jane Smith",
  "remarks": "All measurements within tolerance",
  "entries": [
    {
      "parameterId": 1,
      "observedValue": "12.5",
      "remark": "Within tolerance"
    }
  ]
}
```

**Example Response** (201 First piece inspection report created successfully)

```json
{
  "success": true,
  "message": "Report created successfully.",
  "data": {
    "id": 1,
    "reportNumber": "FPI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productCastingNumber": "CAST-001",
    "operatorName": "Jane Smith",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateFirstPieceInspectionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. nextReportNumber() = reportRepository.count()+1, then ReportNumberGenerator generates FPI-{yyyyMMdd}-%05d (e.g. PMR-20260802-00001).
3. status=DRAFT, createdBy=currentUser, shift = resolveShift(shiftId): if shiftId null, ShiftService picks the active shift covering now (fallback: first active); line resolved by lineId -> 404 'Line not found.' if missing.
4. For each entry: parameter resolved by parameterId -> 404 'Parameter not found.'; inspectionResult computed once by ValidationService.validate — if inputType==NUMBER the BigDecimal value is compared against min/max (below min or above max -> FAIL, non-numeric -> FAIL), blank value or non-NUMBER input -> NOT_APPLICABLE.
5. Saves the report header then all entries; returns 201 Created + Location header. The First Piece Inspection report also records optional productCastingNumber, operatorName and inspectorName fields at creation.

**Database Impact**

Read: users (current user), line_master, shifts (auto-detect when shiftId null), N x parameter_master, COUNT(*) on first_piece_inspection_reports (for the report number). Write: first_piece_inspection_reports (INSERT header) + first_piece_inspection_entries (INSERT one row per entry incl. computed inspectionResult). No audit/notification write.

---

### `GET /api/reports/first-piece-inspection` — Fetch all first piece inspection reports

**Purpose** — Returns a list of all first piece inspection reports. Supports the shared pagination/filtering contract (`ReportFilterRequest`); when any such param is present the `data` field is a `PageResponse<FirstPieceInspectionResponse>`, otherwise the legacy list.

**HTTP Method** — `GET`

**URL** — `/api/reports/first-piece-inspection`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 First piece inspection reports fetched successfully)

```json
{
  "success": true,
  "message": "Reports fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportNumber": "FPI-20260802-00001",
      "reportDate": "2026-08-02",
      "shift": "Morning",
      "line": "Line 1",
      "createdBy": "John Doe",
      "approvedBy": null,
      "status": "DRAFT",
      "remarks": null,
      "approvedAt": null,
      "createdAt": "2026-08-02T08:00:00",
      "entries": [
        {
          "id": 1,
          "parameterId": 1,
          "parameterName": "Temperature",
          "minValue": 0.0,
          "maxValue": 100.0,
          "observedValue": "25.5",
          "unit": "C",
          "inspectionResult": "PASS",
          "remark": null
        }
      ],
      "productCastingNumber": "CAST-001",
      "operatorName": "Jane Smith",
      "inspectorName": "John Doe"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findAllWithDetails (@EntityGraph on shift/line/createdBy/approvedBy, ORDER BY id DESC) plus entries fetched in one pass by report ids.
3. Returns List<Response> ordered newest first. No filter parameters.

**Database Impact**

Read: first_piece_inspection_reports (entity graph over shift/line/createdBy/approvedBy) + first_piece_inspection_entries (IN report ids). No writes.

---

### `GET /api/reports/first-piece-inspection/{id}` — Fetch first piece inspection report by id

**Purpose** — Returns a single first piece inspection report by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/reports/first-piece-inspection/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the first piece inspection report |

**Example Response** (200 First piece inspection report fetched successfully)

```json
{
  "success": true,
  "message": "Report fetched successfully.",
  "data": {
    "id": 1,
    "reportNumber": "FPI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productCastingNumber": "CAST-001",
    "operatorName": "Jane Smith",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | First piece inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByIdWithDetails -> 404 'First piece inspection report not found.' if missing; entries fetched via findByReport (@EntityGraph parameter).
3. Returns the report with all entries and their stored inspectionResult.

**Database Impact**

Read: first_piece_inspection_reports (by PK) + first_piece_inspection_entries (by report). No writes.

---

### `POST /api/reports/first-piece-inspection/{id}/submit` — Submit first piece inspection report for approval

**Purpose** — Submits a DRAFT first piece inspection report for approval

**HTTP Method** — `POST`

**URL** — `/api/reports/first-piece-inspection/{id}/submit`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the first piece inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Submission remarks |

**Validation rules** — remarks: @Size(max=1000) optional.

**Example Request**

```json
{
  "remarks": "Report is ready for review"
}
```

**Example Response** (200 First piece inspection report submitted successfully)

```json
{
  "success": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": 1,
    "reportNumber": "FPI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productCastingNumber": "CAST-001",
    "operatorName": "Jane Smith",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | First piece inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `SubmitFirstPieceInspectionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Guard: status must be DRAFT, else 400 'Only draft reports can be submitted.'.
3. Sets status=SUBMITTED; if the optional remarks is provided it overwrites the report remarks.
4. NO submittedBy/submittedAt is recorded (columns do not exist).
5. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: first_piece_inspection_reports (status check) + first_piece_inspection_entries. Write: first_piece_inspection_reports (UPDATE status=SUBMITTED, remarks, updated_at). No submitted stamp. No audit write.

---

### `POST /api/reports/first-piece-inspection/{id}/approve` — Approve first piece inspection report

**Purpose** — Approves a SUBMITTED first piece inspection report

**HTTP Method** — `POST`

**URL** — `/api/reports/first-piece-inspection/{id}/approve`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the first piece inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval or rejection remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Approved - all checks passed"
}
```

**Example Response** (200 First piece inspection report approved successfully)

```json
{
  "success": true,
  "message": "Report approved successfully.",
  "data": {
    "id": 1,
    "reportNumber": "FPI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": "Admin User",
    "status": "APPROVED",
    "remarks": "Approved",
    "approvedAt": "2026-08-02T10:30:00",
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productCastingNumber": "CAST-001",
    "operatorName": "Jane Smith",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | First piece inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveFirstPieceInspectionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=APPROVED, approvedBy=currentUser, approvedAt=now; optional remarks overwrite the report remarks.
4. Saves; returns the report + entries.
5. No notification or audit row is written.

**Database Impact**

Read: first_piece_inspection_reports. Write: first_piece_inspection_reports (UPDATE status=APPROVED, approved_by, approved_at, remarks, updated_at). No audit write.

---

### `POST /api/reports/first-piece-inspection/{id}/reject` — Reject first piece inspection report

**Purpose** — Rejects a SUBMITTED first piece inspection report

**HTTP Method** — `POST`

**URL** — `/api/reports/first-piece-inspection/{id}/reject`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the first piece inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval or rejection remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Rejected - corrective action required"
}
```

**Example Response** (200 First piece inspection report rejected successfully)

```json
{
  "success": true,
  "message": "Report rejected successfully.",
  "data": {
    "id": 1,
    "reportNumber": "FPI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "REJECTED",
    "remarks": "Out of tolerance",
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productCastingNumber": "CAST-001",
    "operatorName": "Jane Smith",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | First piece inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApproveFirstPieceInspectionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=REJECTED; approvedBy/approvedAt are NOT set. The rejection reason is stored in the shared remarks field (overwritten when provided).
4. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: first_piece_inspection_reports. Write: first_piece_inspection_reports (UPDATE status=REJECTED, remarks, updated_at). approved_by/approved_at not set. No audit write.

---

### `DELETE /api/reports/first-piece-inspection/{id}` — Delete draft first piece inspection report

**Purpose** — Deletes a DRAFT first piece inspection report by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/reports/first-piece-inspection/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the first piece inspection report |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | First piece inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Guard: status must be DRAFT, else 400 'Only draft reports can be deleted.'.
3. Physical delete: entries deleted by report_id first, then the report row.
4. Returns 204 No Content.

**Database Impact**

Write: first_piece_inspection_entries (DELETE entries by report_id) then first_piece_inspection_reports (DELETE header - physical). Only DRAFT allowed, else 400 and no change.

---

## 18. Reports — Pre-Delivery Inspection

Path base `/api/reports/pre-delivery-inspection`; number prefix `PDI`; tables `pre_delivery_inspection_reports` / `pre_delivery_inspection_entries`. Contract follows section 5 exactly. Extra header fields: `productPartNumber`, `batchNumber`, `inspectorName` (each max 200, optional).

### `POST /api/reports/pre-delivery-inspection` — Create pre-delivery inspection report

**Purpose** — Creates a new pre-delivery inspection report in DRAFT status

**HTTP Method** — `POST`

**URL** — `/api/reports/pre-delivery-inspection`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `reportDate` | LocalDate | Yes | Date of the inspection report |
| `shiftId` | long | No | ID of the shift. Omitted to auto-detect from current time |
| `lineId` | long | Yes | ID of the production line |
| `productPartNumber` | String | No | Product part number |
| `batchNumber` | String | No | Batch number |
| `inspectorName` | String | No | Name of the inspector |
| `remarks` | String | No | Additional remarks |
| `entries` | List<PreDeliveryInspectionEntryRequest> | Yes | List of inspection entries |

**Validation rules** — reportDate: @NotNull LocalDate; shiftId: optional (auto-detect); lineId: @NotNull; remarks: @Size(max=1000); productPartNumber: @Size(max=200) optional; batchNumber: @Size(max=200) optional; inspectorName: @Size(max=200) optional; entries: @Valid @NotEmpty.

**Example Request**

```json
{
  "reportDate": "2025-01-15",
  "shiftId": 1,
  "lineId": 1,
  "productPartNumber": "PART-001",
  "batchNumber": "BATCH-001",
  "inspectorName": "Jane Smith",
  "remarks": "All checks completed",
  "entries": [
    {
      "parameterId": 1,
      "observedValue": "12.5",
      "remark": "Within tolerance"
    }
  ]
}
```

**Example Response** (201 Pre-delivery inspection report created successfully)

```json
{
  "success": true,
  "message": "Report created successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PDI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productPartNumber": "PART-001",
    "batchNumber": "B-100",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreatePreDeliveryInspectionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. nextReportNumber() = reportRepository.count()+1, then ReportNumberGenerator generates PDI-{yyyyMMdd}-%05d (e.g. PMR-20260802-00001).
3. status=DRAFT, createdBy=currentUser, shift = resolveShift(shiftId): if shiftId null, ShiftService picks the active shift covering now (fallback: first active); line resolved by lineId -> 404 'Line not found.' if missing.
4. For each entry: parameter resolved by parameterId -> 404 'Parameter not found.'; inspectionResult computed once by ValidationService.validate — if inputType==NUMBER the BigDecimal value is compared against min/max (below min or above max -> FAIL, non-numeric -> FAIL), blank value or non-NUMBER input -> NOT_APPLICABLE.
5. Saves the report header then all entries; returns 201 Created + Location header. The Pre-Delivery Inspection report also records optional productPartNumber, batchNumber and inspectorName fields at creation.

**Database Impact**

Read: users (current user), line_master, shifts (auto-detect when shiftId null), N x parameter_master, COUNT(*) on pre_delivery_inspection_reports (for the report number). Write: pre_delivery_inspection_reports (INSERT header) + pre_delivery_inspection_entries (INSERT one row per entry incl. computed inspectionResult). No audit/notification write.

---

### `GET /api/reports/pre-delivery-inspection` — Fetch all pre-delivery inspection reports

**Purpose** — Returns a list of all pre-delivery inspection reports. Supports the shared pagination/filtering contract (`ReportFilterRequest`); when any such param is present the `data` field is a `PageResponse<PreDeliveryInspectionResponse>`, otherwise the legacy list.

**HTTP Method** — `GET`

**URL** — `/api/reports/pre-delivery-inspection`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Pre-delivery inspection reports fetched successfully)

```json
{
  "success": true,
  "message": "Reports fetched successfully.",
  "data": [
    {
      "id": 1,
      "reportNumber": "PDI-20260802-00001",
      "reportDate": "2026-08-02",
      "shift": "Morning",
      "line": "Line 1",
      "createdBy": "John Doe",
      "approvedBy": null,
      "status": "DRAFT",
      "remarks": null,
      "approvedAt": null,
      "createdAt": "2026-08-02T08:00:00",
      "entries": [
        {
          "id": 1,
          "parameterId": 1,
          "parameterName": "Temperature",
          "minValue": 0.0,
          "maxValue": 100.0,
          "observedValue": "25.5",
          "unit": "C",
          "inspectionResult": "PASS",
          "remark": null
        }
      ],
      "productPartNumber": "PART-001",
      "batchNumber": "B-100",
      "inspectorName": "John Doe"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findAllWithDetails (@EntityGraph on shift/line/createdBy/approvedBy, ORDER BY id DESC) plus entries fetched in one pass by report ids.
3. Returns List<Response> ordered newest first. No filter parameters.

**Database Impact**

Read: pre_delivery_inspection_reports (entity graph over shift/line/createdBy/approvedBy) + pre_delivery_inspection_entries (IN report ids). No writes.

---

### `GET /api/reports/pre-delivery-inspection/{id}` — Fetch pre-delivery inspection report by id

**Purpose** — Returns a single pre-delivery inspection report by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/reports/pre-delivery-inspection/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the pre-delivery inspection report |

**Example Response** (200 Pre-delivery inspection report fetched successfully)

```json
{
  "success": true,
  "message": "Report fetched successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PDI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productPartNumber": "PART-001",
    "batchNumber": "B-100",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Pre-delivery inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByIdWithDetails -> 404 'Pre-delivery inspection report not found.' if missing; entries fetched via findByReport (@EntityGraph parameter).
3. Returns the report with all entries and their stored inspectionResult.

**Database Impact**

Read: pre_delivery_inspection_reports (by PK) + pre_delivery_inspection_entries (by report). No writes.

---

### `POST /api/reports/pre-delivery-inspection/{id}/submit` — Submit pre-delivery inspection report for approval

**Purpose** — Submits a DRAFT pre-delivery inspection report for approval

**HTTP Method** — `POST`

**URL** — `/api/reports/pre-delivery-inspection/{id}/submit`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the pre-delivery inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Submission remarks |

**Validation rules** — remarks: @Size(max=1000) optional.

**Example Request**

```json
{
  "remarks": "Report is ready for review"
}
```

**Example Response** (200 Pre-delivery inspection report submitted successfully)

```json
{
  "success": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PDI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "DRAFT",
    "remarks": null,
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productPartNumber": "PART-001",
    "batchNumber": "B-100",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Pre-delivery inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `SubmitPreDeliveryInspectionRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Guard: status must be DRAFT, else 400 'Only draft reports can be submitted.'.
3. Sets status=SUBMITTED; if the optional remarks is provided it overwrites the report remarks.
4. NO submittedBy/submittedAt is recorded (columns do not exist).
5. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: pre_delivery_inspection_reports (status check) + pre_delivery_inspection_entries. Write: pre_delivery_inspection_reports (UPDATE status=SUBMITTED, remarks, updated_at). No submitted stamp. No audit write.

---

### `POST /api/reports/pre-delivery-inspection/{id}/approve` — Approve pre-delivery inspection report

**Purpose** — Approves a SUBMITTED pre-delivery inspection report

**HTTP Method** — `POST`

**URL** — `/api/reports/pre-delivery-inspection/{id}/approve`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the pre-delivery inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval or rejection remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Approved - all checks passed"
}
```

**Example Response** (200 Pre-delivery inspection report approved successfully)

```json
{
  "success": true,
  "message": "Report approved successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PDI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": "Admin User",
    "status": "APPROVED",
    "remarks": "Approved",
    "approvedAt": "2026-08-02T10:30:00",
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productPartNumber": "PART-001",
    "batchNumber": "B-100",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Pre-delivery inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApprovePreDeliveryInspectionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=APPROVED, approvedBy=currentUser, approvedAt=now; optional remarks overwrite the report remarks.
4. Saves; returns the report + entries.
5. No notification or audit row is written.

**Database Impact**

Read: pre_delivery_inspection_reports. Write: pre_delivery_inspection_reports (UPDATE status=APPROVED, approved_by, approved_at, remarks, updated_at). No audit write.

---

### `POST /api/reports/pre-delivery-inspection/{id}/reject` — Reject pre-delivery inspection report

**Purpose** — Rejects a SUBMITTED pre-delivery inspection report

**HTTP Method** — `POST`

**URL** — `/api/reports/pre-delivery-inspection/{id}/reject`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the pre-delivery inspection report |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `remarks` | String | No | Approval or rejection remarks |

**Validation rules** — remarks: @Size(max=1000) optional (also reused for reject).

**Example Request**

```json
{
  "remarks": "Rejected - corrective action required"
}
```

**Example Response** (200 Pre-delivery inspection report rejected successfully)

```json
{
  "success": true,
  "message": "Report rejected successfully.",
  "data": {
    "id": 1,
    "reportNumber": "PDI-20260802-00001",
    "reportDate": "2026-08-02",
    "shift": "Morning",
    "line": "Line 1",
    "createdBy": "John Doe",
    "approvedBy": null,
    "status": "REJECTED",
    "remarks": "Out of tolerance",
    "approvedAt": null,
    "createdAt": "2026-08-02T08:00:00",
    "entries": [
      {
        "id": 1,
        "parameterId": 1,
        "parameterName": "Temperature",
        "minValue": 0.0,
        "maxValue": 100.0,
        "observedValue": "25.5",
        "unit": "C",
        "inspectionResult": "PASS",
        "remark": null
      }
    ],
    "productPartNumber": "PART-001",
    "batchNumber": "B-100",
    "inspectorName": "John Doe"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN or ADMIN role |
| 404 | Pre-delivery inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `ApprovePreDeliveryInspectionRequest`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Guard: status must be SUBMITTED, else 400 'Only submitted reports can be approved or rejected.'.
3. Sets status=REJECTED; approvedBy/approvedAt are NOT set. The rejection reason is stored in the shared remarks field (overwritten when provided).
4. Saves; returns the report + entries. No notification or audit row is written.

**Database Impact**

Read: pre_delivery_inspection_reports. Write: pre_delivery_inspection_reports (UPDATE status=REJECTED, remarks, updated_at). approved_by/approved_at not set. No audit write.

---

### `DELETE /api/reports/pre-delivery-inspection/{id}` — Delete draft pre-delivery inspection report

**Purpose** — Deletes a DRAFT pre-delivery inspection report by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/reports/pre-delivery-inspection/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | ID of the pre-delivery inspection report |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Pre-delivery inspection report not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Guard: status must be DRAFT, else 400 'Only draft reports can be deleted.'.
3. Physical delete: entries deleted by report_id first, then the report row.
4. Returns 204 No Content.

**Database Impact**

Write: pre_delivery_inspection_entries (DELETE entries by report_id) then pre_delivery_inspection_reports (DELETE header - physical). Only DRAFT allowed, else 400 and no change.

---

## 19. Dashboard

Dashboard KPIs are computed with native SQL over a UNION ALL of the six report tables. All endpoints are read-only and any authenticated user may call them. No query parameters are used except `GET /api/reports/dashboard/recent-activity?limit=N`.

### `GET /api/reports/dashboard/summary` — Get dashboard summary with report counts by status

**Purpose** — Returns aggregated counts of reports grouped by status (draft, submitted, approved, rejected) along with the total number of reports.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/summary`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Dashboard summary fetched successfully)

```json
{
  "success": true,
  "message": "Dashboard summary fetched successfully.",
  "data": {
    "totalReports": 120,
    "draftReports": 20,
    "submittedReports": 30,
    "approvedReports": 60,
    "rejectedReports": 10
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

SELECT status, COUNT(*) FROM (union of six report tables) GROUP BY status -> buckets for DRAFT/SUBMITTED/APPROVED/REJECTED plus total. Returns DashboardSummaryResponse {totalReports, draftReports, submittedReports, approvedReports, rejectedReports}.

**Database Impact**

Read: 1 native aggregate SELECT over the 6 report tables. No writes.

---

### `GET /api/reports/dashboard/reports-by-type` — Get report counts grouped by type

**Purpose** — Returns the number of reports for each report type.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/reports-by-type`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Reports by type fetched successfully)

```json
{
  "success": true,
  "message": "Reports by type fetched successfully.",
  "data": [
    {
      "reportType": "PROCESS_MONITORING",
      "count": 40
    },
    {
      "reportType": "CHEMICAL_CONSUMPTION",
      "count": 20
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

SELECT report_type, COUNT(*) GROUP BY report_type -> counts per report type (labels hard-coded). Returns List of {reportType, count}.

**Database Impact**

Read: 1 native aggregate SELECT. No writes.

---

### `GET /api/reports/dashboard/reports-by-shift` — Get report counts grouped by shift

**Purpose** — Returns the number of reports for each shift.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/reports-by-shift`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Reports by shift fetched successfully)

```json
{
  "success": true,
  "message": "Reports by shift fetched successfully.",
  "data": [
    {
      "shiftId": 1,
      "shiftName": "Morning",
      "count": 60
    },
    {
      "shiftId": 2,
      "shiftName": "Evening",
      "count": 60
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

SELECT shifts.id, shifts.name, COUNT(*) GROUP BY shift, JOIN shifts, ORDER BY count DESC -> reports per shift. Returns List of {shiftId, shiftName, count}.

**Database Impact**

Read: native SELECT joining the union to shifts. No writes.

---

### `GET /api/reports/dashboard/reports-by-line` — Get report counts grouped by line

**Purpose** — Returns the number of reports for each production line.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/reports-by-line`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Reports by line fetched successfully)

```json
{
  "success": true,
  "message": "Reports by line fetched successfully.",
  "data": [
    {
      "lineId": 1,
      "lineName": "Line 1",
      "count": 80
    },
    {
      "lineId": 2,
      "lineName": "Line 2",
      "count": 40
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Same pattern joined to line_master -> reports per line. Returns List of {lineId, lineName, count}.

**Database Impact**

Read: native SELECT joining the union to line_master. No writes.

---

### `GET /api/reports/dashboard/reports-created-today` — Get count of reports created today

**Purpose** — Returns the total number of reports created today.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/reports-created-today`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Reports created today count fetched successfully)

```json
{
  "success": true,
  "message": "Reports created today fetched successfully.",
  "data": {
    "count": 5
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

SELECT COUNT(*) over the union WHERE created_at::date = CURRENT_DATE (PostgreSQL-specific) -> reports created today. Returns {count}.

**Database Impact**

Read: 1 native SELECT (date cast). No writes.

---

### `GET /api/reports/dashboard/reports-pending-approval` — Get count of reports pending approval

**Purpose** — Returns the total number of reports currently pending approval.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/reports-pending-approval`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Reports pending approval count fetched successfully)

```json
{
  "success": true,
  "message": "Pending approval count fetched successfully.",
  "data": {
    "count": 30
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

SELECT COUNT(*) over the union WHERE status='SUBMITTED' -> pending approval count. Returns {count}.

**Database Impact**

Read: 1 native SELECT. No writes.

---

### `GET /api/reports/dashboard/recent-reports` — Get the 10 most recent reports across all types

**Purpose** — Returns the 10 most recently created reports across all report types.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/recent-reports`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Recent reports fetched successfully)

```json
{
  "success": true,
  "message": "Recent reports fetched successfully.",
  "data": [
    {
      "id": 12,
      "reportNumber": "PMR-20260802-00012",
      "reportType": "PROCESS_MONITORING",
      "reportDate": "2026-08-02",
      "status": "SUBMITTED",
      "shiftName": "Morning",
      "lineName": "Line 1",
      "createdBy": "John Doe",
      "createdAt": "2026-08-02T08:00:00"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Recent union LEFT JOIN shifts/line_master/users, ORDER BY created_at DESC LIMIT 10; created_by = COALESCE(CONCAT(first_name,' ',last_name), employee_id). Returns List of {id, reportNumber, reportType, reportDate, status, shiftName, lineName, createdBy, createdAt}.

**Database Impact**

Read: 1 native SELECT over the union + join tables, LIMIT 10. No writes.

---

### `GET /api/reports/dashboard/monthly-statistics` — Get monthly report statistics

**Purpose** — Returns monthly statistics for reports, including total, approved and rejected counts per month and year.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/monthly-statistics`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Monthly statistics fetched successfully)

```json
{
  "success": true,
  "message": "Monthly statistics fetched successfully.",
  "data": [
    {
      "year": 2026,
      "month": 8,
      "totalReports": 120,
      "approvedReports": 60,
      "rejectedReports": 10
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

EXTRACT(YEAR, MONTH FROM report_date), COUNT(*), SUM(CASE status='APPROVED'), SUM(CASE status='REJECTED') GROUP BY year, month ORDER BY year DESC, month DESC. Returns List of {year, month, totalReports, approvedReports, rejectedReports}.

**Database Impact**

Read: 1 native aggregate SELECT. No writes.

---

### `GET /api/reports/dashboard/approval-summary` — Get approval summary

**Purpose** — Returns aggregated approval activity: pending, approved and rejected totals, today's approvals/rejections and the overall approval rate.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/approval-summary`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Approval summary fetched successfully)

```json
{
  "success": true,
  "message": "Approval summary fetched successfully.",
  "data": {
    "pendingApprovals": 12,
    "approvedReports": 90,
    "rejectedReports": 10,
    "approvedToday": 3,
    "rejectedToday": 1,
    "approvalRate": 90.0
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. Group `status` over the union of the six report tables -> `pendingApprovals` (SUBMITTED), `approvedReports`, `rejectedReports`.
2. Group `status` over the same union filtered to `approved_at::date = CURRENT_DATE` -> `approvedToday` / `rejectedToday`.
3. `approvalRate = approved / (approved + rejected) * 100` rounded to 2 decimals (0 when nothing decided).
4. Reuses the same `STATUS_UNION` table set as `/summary` (no duplicated queries).

**Database Impact**

Read: 2 native aggregate SELECTs. No writes.

---

### `GET /api/reports/dashboard/recent-activity` — Get recent report activity

**Purpose** — Returns the most recent report lifecycle events (created, approved, rejected) across all report types, with the acting user and timestamp.

**HTTP Method** — `GET`

**URL** — `/api/reports/dashboard/recent-activity`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `limit` | query | int | No | Maximum number of activity events to return (default `10`, max `50`) |

**Example Response** (200 Recent activity fetched successfully)

```json
{
  "success": true,
  "message": "Recent activity fetched successfully.",
  "data": [
    {
      "id": 41,
      "reportNumber": "PDI-20260802-00031",
      "reportType": "PDI",
      "action": "REJECTED",
      "status": "REJECTED",
      "actor": "Adm2 Test2",
      "timestamp": "2026-08-02T13:39:21.531603"
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. Builds a UNION of lifecycle events over the six report tables: a `CREATED` event from each row (`created_by`/`created_at`) plus an `APPROVED`/`REJECTED` event (derived from `status`) where `approved_at` is not null.
2. Left-joins `users` for the actor's display name, orders by event time descending, applies `LIMIT :limit`.
3. Returns `RecentActivityResponse` items (lightweight for mobile feeds).

**Database Impact**

Read: 1 native SELECT over a UNION of the six report tables + `users` join. No writes.

---

## 20. Global Search

Full-text-ish search across all six report tables using PostgreSQL-specific SQL (ILIKE, CONCAT, casts). Read-only. Two search surfaces:

- `GET /api/reports/search` — report-only search (legacy, unchanged).
- `GET /api/search` — **unified enterprise search** across reports, users, and parameters, built on the shared pagination framework (`PageRequest` / `PageResponse`).

### `GET /api/reports/search` — Global search across all report modules with filtering, pagination and sorting

**Purpose** — Searches across all report modules using the provided filters and returns paginated results. All filter, pagination and sorting criteria are passed as query parameters.

**HTTP Method** — `GET`

**URL** — `/api/reports/search`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Search completed successfully)

```json
{
  "success": true,
  "message": "Search completed successfully.",
  "data": {
    "content": [
      {
        "id": 12,
        "reportNumber": "PMR-20260802-00012",
        "reportType": "PROCESS_MONITORING",
        "reportDate": "2026-08-02",
        "shiftName": "Morning",
        "lineName": "Line 1",
        "status": "SUBMITTED",
        "createdBy": "John Doe",
        "approvedBy": null,
        "summary": ""
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - invalid filter, date format or pagination parameter |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Query params bind to GlobalSearchRequest (no @Valid, so DTO validation is NOT enforced).
3. GlobalSearchQueryBuilder builds a COUNT query then a DATA query over the UNION of the six report tables LEFT JOIN shifts, line_master and users (created/approved).
4. Filters applied: reportNumber, reportType, status, employeeName, employeeId, shiftId, lineId, dateFrom/dateTo, remarks, approved (true->APPROVED / false->DRAFT,SUBMITTED,REJECTED), keyword (OR ILIKE on report_number/remarks/employee name), createdBy, approvedBy.
5. Sorting default created_at DESC (sortBy whitelist reportDate|reportNumber|createdAt|updatedAt|status; direction ASC/DESC).
6. Returns PageResponse<GlobalSearchResultItem>.

**Database Impact**

Read: 2 native SELECTs per request (count + page) over the 6 report tables + shifts, line_master, users. PostgreSQL-specific SQL (::varchar, ILIKE, CONCAT). No writes.

---

### `GET /api/reports/search/suggestions` — Get search suggestions for report numbers, employee names, lines, and parameters

**Purpose** — Returns autocomplete suggestions for report numbers, employee names, production lines, and parameter names based on the given search query text.

**HTTP Method** — `GET`

**URL** — `/api/reports/search/suggestions`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `q` | query | String | Yes | Search query text |

**Example Response** (200 Suggestions fetched successfully)

```json
{
  "success": true,
  "message": "Suggestions fetched successfully.",
  "data": {
    "reportNumbers": [
      "PMR-20260802-00012"
    ],
    "employeeNames": [
      "John Doe"
    ],
    "lines": [
      "Line 1"
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - missing or malformed query parameter |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Requires query param q (@RequestParam, required).
3. Runs four native queries with pattern %q%: report numbers ILIKE from the six tables (LIMIT 10), distinct employee names from users matching name or employee_id (LIMIT 10), line names ILIKE from line_master (LIMIT 10), parameter names ILIKE from parameter_master (LIMIT 10).
4. Returns SearchSuggestionsResponse {reportNumbers, employeeNames, lines, parameters}.

**Database Impact**

Read: 4 native SELECTs (report tables, users, line_master, parameter_master). No writes.

---

### `GET /api/search` — Unified search across reports, users and parameters

**Purpose** — Unified enterprise search. Searches reports, users and parameters with optional entity-type, report, employee, role, shift, line, status and date filters, returning paginated lightweight results built on the shared pagination framework.

**HTTP Method** — `GET`

**URL** — `/api/search`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters** (all optional)

| Param | In | Type | Description |
|---|---|---|---|
| `type` | query | string | Restrict to one entity type: `REPORT`, `USER`, `PARAMETER` (default: all) |
| `keyword` | query | string | Free-text match on title / subtitle / actor / report type |
| `reportNumber` | query | string | Partial report number / parameter name / employee ID |
| `reportType` | query | string | Report type filter |
| `status` | query | string | Report status filter |
| `employeeName` | query | string | Employee name filter (report creator / user full name) |
| `role` | query | string | User role filter (users only) |
| `shiftId` | query | long | Shift ID filter (reports only) |
| `lineId` | query | long | Line ID filter (reports only) |
| `dateFrom` | query | date | Earliest report date (ISO yyyy-MM-dd) |
| `dateTo` | query | date | Latest report date (ISO yyyy-MM-dd) |
| `page` | query | int | Zero-based page number (default 0) |
| `size` | query | int | Page size (default 20, max 200) |
| `sortBy` | query | string | Sort field: `createdAt`, `reportDate`, `title`, `reportType`, `status` |
| `sortDirection` | query | string | `ASC` or `DESC` (default `DESC`) |

**Example Response** (200 Search completed successfully)

```json
{
  "success": true,
  "message": "Search completed successfully.",
  "data": {
    "content": [
      {
        "type": "REPORT",
        "id": 41,
        "title": "PDI-20260802-00031",
        "subtitle": "PDI",
        "reportType": "PDI",
        "status": "REJECTED",
        "shiftName": "Morning",
        "lineName": "Line-1",
        "actor": "Adm2 Test2",
        "reportDate": "2026-08-02",
        "createdAt": "2026-08-02T13:39:21"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - invalid filter, date format or pagination parameter |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `PageResponse`, `UnifiedSearchRequest`, `UnifiedSearchResultItem`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. `UnifiedSearchQueryBuilder` builds a UNION of the six report tables, the `users` table and `parameter_master`, each normalized to a common shape (`type, id, title, subtitle, report_type, status, shift_name, line_name, actor, report_date, created_at, role_name, shift_id, line_id`).
3. Filters (`keyword`, `reportNumber`, `reportType`, `status`, `employeeName`, `role`, `shiftId`, `lineId`, `dateFrom`, `dateTo`) are applied as optional parameterized conditions; `type` restricts which branches are unioned.
4. Count query for `totalElements`; data query with `LIMIT :size OFFSET :offset` and a whitelisted `sortBy`.
5. Returns `PageResponse<UnifiedSearchResultItem>` (lightweight, mobile-friendly).

**Database Impact**

Read: 1-2 native SELECTs (count + data) over the union. No writes.

---

## 21. Analytics

Analytics aggregates are read-only native SQL over the six report + six entry tables. Restricted to `SUPER_ADMIN`/`ADMIN`. Optional filters: `dateFrom`, `dateTo`, `shiftId`, `lineId` (per endpoint). No validation that `dateFrom <= dateTo`; a malformed date -> 400.

### `GET /api/analytics/report-overview` — Get report overview with counts by type, status, shift, and line

**Purpose** — Returns an overview of report counts grouped by report type, status, shift, and line, optionally filtered by date range, shift, and line.

**HTTP Method** — `GET`

**URL** — `/api/analytics/report-overview`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |
| `shiftId` | query | long | No | Shift ID to filter by |
| `lineId` | query | long | No | Line ID to filter by |

**Example Response** (200 Report overview fetched successfully)

```json
{
  "success": true,
  "message": "Report overview fetched successfully.",
  "data": {
    "totalReports": 120,
    "reportsByType": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "reportsByStatus": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "reportsByShift": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "reportsByLine": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Filters by dateFrom/dateTo/shiftId/lineId. Computes totalReports (COUNT) plus grouped aggregates: reportsByType (GROUP BY report_type), reportsByStatus (GROUP BY status), reportsByShift (JOIN shifts), reportsByLine (JOIN line_master). Returns ReportOverviewResponse.

**Database Impact**

Read: 5 aggregate SELECTs over the union + shifts + line_master. No writes.

---

### `GET /api/analytics/quality-kpis` — Get quality KPIs including approval, rejection, pass, and fail rates

**Purpose** — Returns quality KPI summary cards, daily inspection trends, and pass/fail counts by inspection type, optionally filtered by date range, shift, and line.

**HTTP Method** — `GET`

**URL** — `/api/analytics/quality-kpis`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |
| `shiftId` | query | long | No | Shift ID to filter by |
| `lineId` | query | long | No | Line ID to filter by |

**Example Response** (200 Quality KPIs fetched successfully)

```json
{
  "success": true,
  "message": "Quality KPIs fetched successfully.",
  "data": {
    "kpiCards": [
      {
        "label": "Total Reports",
        "value": "120",
        "unit": "count",
        "change": null,
        "trend": null
      }
    ],
    "dailyInspectionTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "passFailByType": [
      {
        "label": "PROCESS_MONITORING - PASS",
        "value": 35
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Counts total, APPROVED, REJECTED, SUBMITTED over the report union; computes approvalRate/rejectionRate. Then aggregates PASS/FAIL/NOT_APPLICABLE over the entry union; passFailByType per <TYPE> - <RESULT>; dailyInspectionTrend = report count per report_date (shiftId/lineId ignored here). Returns QualityKPIResponse.

**Database Impact**

Read: aggregate SELECTs over the 6 report + 6 entry tables. No writes.

---

### `GET /api/analytics/chemical-consumption` — Get chemical consumption KPIs with trends and line breakdown

**Purpose** — Returns chemical consumption KPI summary cards, daily/weekly/monthly trends, and consumption grouped by line, optionally filtered by date range and line.

**HTTP Method** — `GET`

**URL** — `/api/analytics/chemical-consumption`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |
| `lineId` | query | long | No | Line ID to filter by |

**Example Response** (200 Chemical consumption KPIs fetched successfully)

```json
{
  "success": true,
  "message": "Chemical consumption analytics fetched successfully.",
  "data": {
    "kpiCards": [
      {
        "label": "Total Reports",
        "value": "120",
        "unit": "count",
        "change": null,
        "trend": null
      }
    ],
    "dailyTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "weeklyTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "monthlyTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "consumptionByLine": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Restricted to chemical_consumption_reports. KPIs: total (filtered) + today's reports (note: dailyCount ignores date filters in code). Trends daily/weekly(DATE_TRUNC week)/monthly; consumptionByLine JOIN line_master. Returns ChemicalConsumptionKPIResponse.

**Database Impact**

Read: aggregate SELECTs over chemical_consumption_reports + line_master. No writes.

---

### `GET /api/analytics/process-monitoring` — Get process monitoring KPIs including stability and failure analysis

**Purpose** — Returns process monitoring KPI summary cards, out-of-specification parameters, and failure frequency data, optionally filtered by date range and line.

**HTTP Method** — `GET`

**URL** — `/api/analytics/process-monitoring`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |
| `lineId` | query | long | No | Line ID to filter by |

**Example Response** (200 Process monitoring KPIs fetched successfully)

```json
{
  "success": true,
  "message": "Process monitoring analytics fetched successfully.",
  "data": {
    "kpiCards": [
      {
        "label": "Total Reports",
        "value": "120",
        "unit": "count",
        "change": null,
        "trend": null
      }
    ],
    "outOfSpecParameters": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "failureFrequency": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

process_monitoring_entries JOIN reports JOIN parameter_master grouped by inspection_result + parameter_name. Computes pass/fail counts, process stability (PASS %), failureFrequency (FAIL rows per parameter), outOfSpecParameters (FAIL counts per parameter), totalReports. Returns ProcessMonitoringKPIResponse.

**Database Impact**

Read: process_monitoring_entries + process_monitoring_reports + parameter_master. No writes.

---

### `GET /api/analytics/line-performance` — Get line performance analytics with rejection and approval rates

**Purpose** — Returns line performance analytics with reports, rejections, and approval rate grouped by line, optionally filtered by date range.

**HTTP Method** — `GET`

**URL** — `/api/analytics/line-performance`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |

**Example Response** (200 Line performance fetched successfully)

```json
{
  "success": true,
  "message": "Line performance fetched successfully.",
  "data": {
    "reportsByLine": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "rejectionsByLine": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "approvalRateByLine": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Over the report union JOIN line_master: reportsByLine (count), rejectionsByLine (status='REJECTED'), approvalRateByLine (ROUND of APPROVED/total*100). Returns LinePerformanceResponse.

**Database Impact**

Read: aggregate SELECTs over the union + line_master. No writes.

---

### `GET /api/analytics/shift-performance` — Get shift performance analytics with pass and failure rates

**Purpose** — Returns shift performance analytics with reports, pass rate, and failure rate grouped by shift, optionally filtered by date range.

**HTTP Method** — `GET`

**URL** — `/api/analytics/shift-performance`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |

**Example Response** (200 Shift performance fetched successfully)

```json
{
  "success": true,
  "message": "Shift performance fetched successfully.",
  "data": {
    "reportsByShift": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "passRateByShift": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "failureRateByShift": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Over the report union JOIN shifts: reportsByShift (count), passRateByShift (APPROVED %), failureRateByShift (REJECTED %). Returns ShiftPerformanceResponse.

**Database Impact**

Read: aggregate SELECTs over the union + shifts. No writes.

---

### `GET /api/analytics/operator-performance` — Get operator performance analytics with approval and rejection percentages

**Purpose** — Returns operator performance analytics with reports submitted, approval percentage, and rejection percentage per operator, optionally filtered by date range.

**HTTP Method** — `GET`

**URL** — `/api/analytics/operator-performance`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |

**Example Response** (200 Operator performance fetched successfully)

```json
{
  "success": true,
  "message": "Operator performance fetched successfully.",
  "data": {
    "reportsSubmitted": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "approvalPercentage": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "rejectionPercentage": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

JOIN users on created_by: label = COALESCE(CONCAT(first_name,' ',last_name), employee_id). Computes reportsSubmitted (count), approvalPercentage, rejectionPercentage per operator. Returns OperatorPerformanceResponse.

**Database Impact**

Read: aggregate SELECTs over the union + users. No writes.

---

### `GET /api/analytics/productivity` — Get productivity KPIs including reports per day, shift, operator, and approval time

**Purpose** — Returns productivity KPI summary cards, reports per day trend, and reports grouped by shift and operator, optionally filtered by date range, shift, and line.

**HTTP Method** — `GET`

**URL** — `/api/analytics/productivity`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |
| `shiftId` | query | long | No | Shift ID to filter by |
| `lineId` | query | long | No | Line ID to filter by |

**Example Response** (200 Productivity KPIs fetched successfully)

```json
{
  "success": true,
  "message": "Productivity analytics fetched successfully.",
  "data": {
    "kpiCards": [
      {
        "label": "Total Reports",
        "value": "120",
        "unit": "count",
        "change": null,
        "trend": null
      }
    ],
    "reportsPerDay": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "reportsPerShift": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ],
    "reportsPerOperator": [
      {
        "label": "PROCESS_MONITORING",
        "value": 40
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

reportsPerDay (per report_date), reportsPerShift (JOIN shifts; note this sub-query drops the shiftId filter), reportsPerOperator (JOIN users). KPIs: total reports + avg approval time (AVG hours between approved_at and created_at for APPROVED reports). Returns ProductivityKPIResponse.

**Database Impact**

Read: aggregate SELECTs over the union + shifts + users. No writes.

---

### `GET /api/analytics/time-trends` — Get time-based analytics with daily, weekly, monthly, and yearly trends

**Purpose** — Returns time-based analytics trends at daily, weekly, monthly, and yearly granularity, optionally filtered by date range, shift, and line.

**HTTP Method** — `GET`

**URL** — `/api/analytics/time-trends`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `dateFrom` | query | LocalDate | No | Start date (ISO format, e.g. 2025-01-01) |
| `dateTo` | query | LocalDate | No | End date (ISO format, e.g. 2025-12-31) |
| `shiftId` | query | long | No | Shift ID to filter by |
| `lineId` | query | long | No | Line ID to filter by |

**Example Response** (200 Time trends fetched successfully)

```json
{
  "success": true,
  "message": "Time trends fetched successfully.",
  "data": {
    "dailyTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "weeklyTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "monthlyTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ],
    "yearlyTrend": [
      {
        "date": "2026-08-02",
        "value": 5
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Invalid date range or parameter format |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

Counts the report union grouped by report_date (daily), DATE_TRUNC week/month/year. Returns TimeAnalyticsResponse {dailyTrend, weeklyTrend, monthlyTrend, yearlyTrend}.

**Database Impact**

Read: 4 aggregate SELECTs over the union. No writes.

---

> **Export — not implemented in the backend.** The backend serves structured
> JSON APIs only; PDF / Excel / CSV / print export is implemented by the
> frontend client. The former backend export module (`/api/exports`,
> `ExportJob`, `ExportService`) was removed and the `export_jobs` table dropped
> (`V8__Drop_export_jobs.sql`).

---

## 22. Integrations

External-system integration configs. `SUPER_ADMIN` only. Secret keys in `configJson` (password, secret, apiKey, token, authToken, accessKey, privateKey, clientSecret, appSecret, etc.) are masked as `****` in every response. Only the `WEBHOOK` connector is implemented; `test` on any other type -> 400. DELETE is physical and can hit an FK conflict (409) when execution history exists.

**Enum reference**

- `IntegrationType` (the `type` field / filter): `ERP`, `SAP`, `MES`, `PLC_SCADA`, `IOT_DEVICE`, `EMAIL`, `SMS`, `TEAMS`, `SLACK`, `WEBHOOK`, `REST_API`, `GRAPHQL_API`, `FTP_SFTP`, `CLOUD_STORAGE`, `OTHER`.
- `IntegrationStatus` (the `status` field, returned by the API; not settable via requests): `ACTIVE`, `INACTIVE`, `CONNECTED`, `DISCONNECTED`, `ERROR`, `TESTING`.

### `GET /api/integrations` — List all integrations with optional filtering and pagination

**Purpose** — Returns a paginated list of integrations, optionally filtered by type and search keyword, and sorted by the given field and direction

**HTTP Method** — `GET`

**URL** — `/api/integrations`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `type` | query | String (ERP,SAP,MES,PLC_SCADA,IOT_DEVICE,EMAIL,SMS,TEAMS,SLACK,WEBHOOK,REST_API,GRAPHQL_API,FTP_SFTP,CLOUD_STORAGE,OTHER) | No | Filter by integration type |
| `search` | query | String | No | Search by name or description |
| `page` | query | int | No | Page number |
| `size` | query | int | No | Page size |
| `sortBy` | query | String | No | Sort field |
| `sortDirection` | query | String | No | Sort direction |

**Example Response** (200 Integrations fetched successfully)

```json
{
  "success": true,
  "message": "Integrations fetched successfully.",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "SAP ERP",
        "description": "ERP sync",
        "type": "ERP",
        "status": "ACTIVE",
        "configJson": "{\"baseUrl\":\"https://sap.example.com\",\"apiKey\":\"****\"}",
        "retryCount": 3,
        "timeoutSeconds": 30,
        "isActive": true,
        "lastTestedAt": null,
        "lastTestStatus": null,
        "createdBy": "SUPER001",
        "createdAt": "2026-08-02T08:00:00",
        "updatedAt": "2026-08-02T08:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Optional filters: type (IntegrationType enum; invalid -> 400), search (case-insensitive LIKE on name/description), page/size, sortBy (default createdAt), sortDirection (default desc).
3. configJson secrets are MASKED (keys like password/secret/apiKey/token/authToken/accessKey/privateKey/clientSecret/appSecret replaced with '****'). Returns PageResponse<IntegrationResponse>.

**Database Impact**

Read: integrations. No writes.

---

### `POST /api/integrations` — Create a new integration

**Purpose** — Creates a new integration with the provided details and returns the created integration

**HTTP Method** — `POST`

**URL** — `/api/integrations`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | String | Yes | Integration display name |
| `description` | String | No | Integration description |
| `type` | String | Yes | Integration type Enum: `ERP`, `SAP`, `MES`, `PLC_SCADA`, `IOT_DEVICE`, `EMAIL`, `SMS`, `TEAMS`, `SLACK`, `WEBHOOK`, `REST_API`, `GRAPHQL_API`, `FTP_SFTP`, `CLOUD_STORAGE`, `OTHER`. |
| `configJson` | String | Yes | Configuration JSON (use URL for webhook, credentials for others) |
| `retryCount` | int | No | Number of retry attempts |
| `timeoutSeconds` | int | No | Timeout in seconds |

**Validation rules** — name: @NotBlank max 200 + unique (duplicate -> 400); description: max 1000 optional; type: @NotNull enum (ERP/SAP/MES/PLC_SCADA/IOT_DEVICE/EMAIL/SMS/TEAMS/SLACK/WEBHOOK/REST_API/GRAPHQL_API/FTP_SFTP/CLOUD_STORAGE/OTHER); configJson: @NotBlank (secret keys masked on read); retryCount: @Min(0) default 3; timeoutSeconds: @Min(1) default 30.

**Example Request**

```json
{
  "name": "Production Webhook",
  "description": "Webhook for production line alerts",
  "type": "WEBHOOK",
  "configJson": "{\"url\":\"https://example.com/hook\"}",
  "retryCount": 3,
  "timeoutSeconds": 30
}
```

**Example Response** (201 Integration created successfully)

```json
{
  "success": true,
  "message": "Integration created successfully.",
  "data": {
    "id": 1,
    "name": "SAP ERP",
    "description": "ERP sync",
    "type": "ERP",
    "status": "ACTIVE",
    "configJson": "{\"baseUrl\":\"https://sap.example.com\",\"apiKey\":\"****\"}",
    "retryCount": 3,
    "timeoutSeconds": 30,
    "isActive": true,
    "lastTestedAt": null,
    "lastTestStatus": null,
    "createdBy": "SUPER001",
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `CreateIntegrationRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Duplicate name ->
400.
3. Saves with status=INACTIVE, isActive=true, createdBy = authenticated employeeId (or 'SYSTEM').
4. Returns 201 + Location header; configJson masked in response.

**Database Impact**

Read: integrations (duplicate check). Write: integrations (INSERT).

---

### `GET /api/integrations/{id}` — Get integration details by ID

**Purpose** — Returns the details of a single integration by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/integrations/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Integration ID |

**Example Response** (200 Integration fetched successfully)

```json
{
  "success": true,
  "message": "Integration fetched successfully.",
  "data": {
    "id": 1,
    "name": "SAP ERP",
    "description": "ERP sync",
    "type": "ERP",
    "status": "ACTIVE",
    "configJson": "{\"baseUrl\":\"https://sap.example.com\",\"apiKey\":\"****\"}",
    "retryCount": 3,
    "timeoutSeconds": 30,
    "isActive": true,
    "lastTestedAt": null,
    "lastTestStatus": null,
    "createdBy": "SUPER001",
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Integration not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById -> 404 if missing.
3. configJson masked. Returns IntegrationResponse.

**Database Impact**

Read: integrations (by PK). No writes.

---

### `PUT /api/integrations/{id}` — Update an existing integration

**Purpose** — Updates the details of an existing integration by its unique identifier

**HTTP Method** — `PUT`

**URL** — `/api/integrations/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Integration ID |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | String | Yes | Integration display name |
| `description` | String | No | Integration description |
| `configJson` | String | Yes | Configuration JSON (use URL for webhook, credentials for others) |
| `retryCount` | int | No | Number of retry attempts |
| `timeoutSeconds` | int | No | Timeout in seconds |

**Validation rules** — name: @NotBlank max 200; description: max 1000; configJson: @NotBlank; retryCount: @Min(0); timeoutSeconds: @Min(1). type is NOT updatable. Fields applied when non-null (name/configJson effectively required).

**Example Request**

```json
{
  "name": "Production Webhook Updated",
  "description": "Updated description",
  "configJson": "{\"url\":\"https://example.com/hook\"}",
  "retryCount": 5,
  "timeoutSeconds": 60
}
```

**Example Response** (200 Integration updated successfully)

```json
{
  "success": true,
  "message": "Integration updated successfully.",
  "data": {
    "id": 1,
    "name": "SAP ERP",
    "description": "ERP sync",
    "type": "ERP",
    "status": "ACTIVE",
    "configJson": "{\"baseUrl\":\"https://sap.example.com\",\"apiKey\":\"****\"}",
    "retryCount": 3,
    "timeoutSeconds": 30,
    "isActive": true,
    "lastTestedAt": null,
    "lastTestStatus": null,
    "createdBy": "SUPER001",
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Integration not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateIntegrationRequest`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. Applies non-null fields (name, description, configJson, retryCount, timeoutSeconds); type immutable.
4. Saves; returns masked IntegrationResponse.

**Database Impact**

Read: integrations. Write: integrations (UPDATE).

---

### `DELETE /api/integrations/{id}` — Delete an integration

**Purpose** — Deletes an integration by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/integrations/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Integration ID |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Integration not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. findById ->
404.
3. PHYSICAL delete of the row.
4. FK from integration_execution_histories.integration_id blocks deletion when history exists -> 409 DataIntegrityViolationException.
5. Returns 204 No Content.

**Database Impact**

Read: integrations. Write: integrations (DELETE - physical). FK conflict -> 409.

---

### `POST /api/integrations/{id}/test` — Test integration connection

**Purpose** — Runs a connection test against the integration and returns the updated integration with the test result

**HTTP Method** — `POST`

**URL** — `/api/integrations/{id}/test`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Integration ID |

**Example Response** (200 Connection test completed)

```json
{
  "success": true,
  "message": "Integration tested successfully.",
  "data": {
    "id": 1,
    "name": "SAP ERP",
    "description": "ERP sync",
    "type": "ERP",
    "status": "CONNECTED",
    "configJson": "{\"baseUrl\":\"https://sap.example.com\",\"apiKey\":\"****\"}",
    "retryCount": 3,
    "timeoutSeconds": 30,
    "isActive": true,
    "lastTestedAt": "2026-08-02T10:30:00",
    "lastTestStatus": "CONNECTED",
    "createdBy": "SUPER001",
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Integration not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Finds the connector for the integration type — only WEBHOOK is implemented; other types -> 400 UnsupportedOperationException.
3. Sets status=TESTING, saves.
4. WEBHOOK connector: parses configJson, extracts url (empty -> ERROR), POSTs a test payload {type, timestamp, source} with a 30s connect / 60s read timeout. 2xx -> CONNECTED; non-2xx -> ERROR; timeout/connect -> DISCONNECTED; other -> ERROR.
5. Persists status, lastTestedAt, lastTestStatus.
6. ALWAYS writes an IntegrationExecutionHistory row (integrationId/Name/Type, startTime, endTime, durationMs, status, errorMessage, retryCount=0, responseCode=null, triggerType=MANUAL). Returns masked IntegrationResponse.

**Database Impact**

Read: integrations. Write: integrations (UPDATE status/lastTestedAt/lastTestStatus) + integration_execution_histories (INSERT).

---

### `POST /api/integrations/{id}/enable` — Enable an integration

**Purpose** — Enables a disabled integration by its unique identifier

**HTTP Method** — `POST`

**URL** — `/api/integrations/{id}/enable`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Integration ID |

**Example Response** (200 Integration enabled successfully)

```json
{
  "success": true,
  "message": "Integration enabled successfully.",
  "data": {
    "id": 1,
    "name": "SAP ERP",
    "description": "ERP sync",
    "type": "ERP",
    "status": "ACTIVE",
    "configJson": "{\"baseUrl\":\"https://sap.example.com\",\"apiKey\":\"****\"}",
    "retryCount": 3,
    "timeoutSeconds": 30,
    "isActive": true,
    "lastTestedAt": null,
    "lastTestStatus": null,
    "createdBy": "SUPER001",
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Integration not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Sets isActive=true; if current status is INACTIVE also sets status=ACTIVE (other statuses untouched).
3. Saves. Returns masked IntegrationResponse.

**Database Impact**

Read: integrations. Write: integrations (UPDATE is_active/status).

---

### `POST /api/integrations/{id}/disable` — Disable an integration

**Purpose** — Disables an active integration by its unique identifier

**HTTP Method** — `POST`

**URL** — `/api/integrations/{id}/disable`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Integration ID |

**Example Response** (200 Integration disabled successfully)

```json
{
  "success": true,
  "message": "Integration disabled successfully.",
  "data": {
    "id": 1,
    "name": "SAP ERP",
    "description": "ERP sync",
    "type": "ERP",
    "status": "INACTIVE",
    "configJson": "{\"baseUrl\":\"https://sap.example.com\",\"apiKey\":\"****\"}",
    "retryCount": 3,
    "timeoutSeconds": 30,
    "isActive": false,
    "lastTestedAt": null,
    "lastTestStatus": null,
    "createdBy": "SUPER001",
    "createdAt": "2026-08-02T08:00:00",
    "updatedAt": "2026-08-02T08:00:00"
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 404 | Integration not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Sets isActive=false and status=INACTIVE.
3. Saves. Returns masked IntegrationResponse.

**Database Impact**

Read: integrations. Write: integrations (UPDATE is_active/status).

---

### `GET /api/integrations/history` — Get integration execution history with pagination

**Purpose** — Returns a paginated list of integration execution history records, optionally filtered by integration ID

**HTTP Method** — `GET`

**URL** — `/api/integrations/history`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `integrationId` | query | long | No | Filter by integration ID |
| `page` | query | int | No | Page number |
| `size` | query | int | No | Page size |

**Example Response** (200 Execution history fetched successfully)

```json
{
  "success": true,
  "message": "Integration history fetched successfully.",
  "data": {
    "content": [
      {
        "id": 1,
        "integrationId": 1,
        "integrationName": "SAP ERP",
        "integrationType": "ERP",
        "startTime": "2026-08-02T10:30:00",
        "endTime": "2026-08-02T10:30:00",
        "durationMs": 1200,
        "status": "CONNECTED",
        "errorMessage": null,
        "retryCount": 0,
        "responseCode": null,
        "triggerType": "MANUAL",
        "createdAt": "2026-08-02T10:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasRole('SUPER_ADMIN')).
2. Optional integrationId filter; else all. Sorted createdAt DESC, paged. Returns PageResponse<IntegrationExecutionHistoryResponse>.

**Database Impact**

Read: integration_execution_histories. No writes.

---

## 23. Attachments

File storage. Any authenticated user. Uploads enforce a 10 MB size cap, MIME-type and extension allow-lists, and SHA-256 content dedup. DELETE is a **soft** delete (`is_active=false`) — the file stays on disk.

**Enum reference**

- `AttachmentCategory` (the `category` field / filter): `REPORT_ATTACHMENT`, `INSPECTION_IMAGE`, `SUPPORTING_DOCUMENT`, `SIGNATURE`, `OTHER`.

### `GET /api/attachments` — Search attachments with pagination

**Purpose** — Returns a paginated list of attachments, optionally filtered by keyword, module and category

**HTTP Method** — `GET`

**URL** — `/api/attachments`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `keyword` | query | String | No | Search keyword in file name |
| `relatedModule` | query | String | No | Filter by module |
| `category` | query | String | No | Filter by category |
| `page` | query | int | No | Page number |
| `size` | query | int | No | Page size |

**Example Response** (200 Attachments fetched successfully)

```json
{
  "success": true,
  "message": "Attachments fetched successfully.",
  "data": {
    "content": [
      {
        "id": 1,
        "originalFileName": "photo.jpg",
        "storedFileName": "b3f0a2c1-....jpg",
        "fileExtension": "jpg",
        "mimeType": "image/jpeg",
        "fileSize": 204800,
        "fileHash": "e3b0c44298fc1c149afbf4c8996fb924...",
        "uploadedBy": "EMP001",
        "uploadedAt": "2026-08-02T10:30:00",
        "relatedModule": "REPORT",
        "relatedEntityId": "12",
        "category": "INSPECTION_IMAGE",
        "description": "Inspection photo",
        "isActive": true
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Optional filters: keyword (case-insensitive contains on original file name), relatedModule, category (accepted but IGNORED by the service), page/size. Sorted uploadedAt DESC, active-only. Returns PageResponse<AttachmentResponse>.

**Database Impact**

Read: attachments (active only). No writes.

---

### `POST /api/attachments/upload` — Upload a single file attachment

**Purpose** — Uploads a single file as an attachment and returns its metadata. Supports multipart/form-data.

**HTTP Method** — `POST`

**URL** — `/api/attachments/upload`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `relatedModule` | query | String | No | Related module name |
| `relatedEntityId` | query | String | No | Related entity ID |
| `category` | query | String | No | Attachment category |
| `description` | query | String | No | Description |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `file` | binary | Yes | The file to upload |

**Example Response** (201 File uploaded successfully)

```json
{
  "success": true,
  "message": "Attachment uploaded successfully.",
  "data": {
    "id": 1,
    "originalFileName": "photo.jpg",
    "storedFileName": "b3f0a2c1-....jpg",
    "fileExtension": "jpg",
    "mimeType": "image/jpeg",
    "fileSize": 204800,
    "fileHash": "e3b0c44298fc1c149afbf4c8996fb924...",
    "uploadedBy": "EMP001",
    "uploadedAt": "2026-08-02T10:30:00",
    "relatedModule": "REPORT",
    "relatedEntityId": "12",
    "category": "INSPECTION_IMAGE",
    "description": "Inspection photo",
    "isActive": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - invalid file or missing parameters |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. multipart: file required, plus optional relatedModule, relatedEntityId, category (AttachmentCategory enum), description.
3. validateFile (MIME/extension/size) -> 400; SHA-256 dedup ->
400.
4. Stores file on disk (UUID.ext), builds Attachment (uploadedBy = current user, isActive=true), saves.
5. Returns 201 + Location header with AttachmentResponse.

**Database Impact**

Write: attachments (INSERT) + file to disk under uploads/. Rejected files make no DB change.

---

### `POST /api/attachments/upload-multiple` — Upload multiple file attachments

**Purpose** — Uploads multiple files as attachments in a single request and returns their metadata. Supports multipart/form-data.

**HTTP Method** — `POST`

**URL** — `/api/attachments/upload-multiple`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `relatedModule` | query | String | No | Related module name |
| `relatedEntityId` | query | String | No | Related entity ID |
| `category` | query | String | No | Attachment category |
| `description` | query | String | No | Description |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `files` | List<String> | Yes | The files to upload |

**Example Response** (201 Files uploaded successfully)

```json
{
  "success": true,
  "message": "Attachments uploaded successfully.",
  "data": [
    {
      "id": 1,
      "originalFileName": "photo.jpg",
      "storedFileName": "b3f0a2c1-....jpg",
      "fileExtension": "jpg",
      "mimeType": "image/jpeg",
      "fileSize": 204800,
      "fileHash": "e3b0c44298fc1c149afbf4c8996fb924...",
      "uploadedBy": "EMP001",
      "uploadedAt": "2026-08-02T10:30:00",
      "relatedModule": "REPORT",
      "relatedEntityId": "12",
      "category": "INSPECTION_IMAGE",
      "description": "Inspection photo",
      "isActive": true
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - invalid files or missing parameters |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. multipart: files (list) required + same optional metadata.
3. Loops single upload inside one @Transactional — a validation failure on any file rolls back the whole batch (rows AND already-written files).
4. Returns 201 with List<AttachmentResponse>.

**Database Impact**

Write: attachments (INSERT N rows) + N files to disk. All-or-nothing.

---

### `GET /api/attachments/download/{id}` — Download an attachment file

**Purpose** — Downloads the stored file of an attachment as a binary stream with a Content-Disposition attachment header

**HTTP Method** — `GET`

**URL** — `/api/attachments/download/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Attachment ID |

**Example Response** (200 File downloaded successfully)

```
<binary file stream>
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Attachment not found |
| 500 | Internal server error |

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Loads active attachment (404 if missing/inactive).
3. Streams file from disk with stored mimeType (default application/octet-stream) and Content-Disposition: attachment; filename=<originalFileName>.

**Database Impact**

Read: attachments (active) + file from disk. No writes.

---

### `GET /api/attachments/preview/{id}` — Preview an attachment inline for images and PDFs

**Purpose** — Streams the stored file of an attachment inline for images and PDFs with a Content-Disposition inline header

**HTTP Method** — `GET`

**URL** — `/api/attachments/preview/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Attachment ID |

**Example Response** (200 File preview generated)

```
<binary file stream>
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Attachment not found |
| 500 | Internal server error |

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Identical to download but Content-Disposition: inline (for images/PDFs rendered in the browser).

**Database Impact**

Read: attachments (active) + file from disk. No writes.

---

### `GET /api/attachments/entity/{module}/{entityId}` — Get all attachments for a specific entity

**Purpose** — Returns all attachments linked to the given module and entity ID

**HTTP Method** — `GET`

**URL** — `/api/attachments/entity/{module}/{entityId}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `module` | path | String | Yes | Module name |
| `entityId` | path | String | Yes | Entity ID |

**Example Response** (200 Attachments retrieved for the entity)

```json
{
  "success": true,
  "message": "Attachments fetched successfully.",
  "data": [
    {
      "id": 1,
      "originalFileName": "photo.jpg",
      "storedFileName": "b3f0a2c1-....jpg",
      "fileExtension": "jpg",
      "mimeType": "image/jpeg",
      "fileSize": 204800,
      "fileHash": "e3b0c44298fc1c149afbf4c8996fb924...",
      "uploadedBy": "EMP001",
      "uploadedAt": "2026-08-02T10:30:00",
      "relatedModule": "REPORT",
      "relatedEntityId": "12",
      "category": "INSPECTION_IMAGE",
      "description": "Inspection photo",
      "isActive": true
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByRelatedModuleAndRelatedEntityIdAndIsActiveTrue(module, entityId) -> attachments linked to an entity. Returns List<AttachmentResponse>.

**Database Impact**

Read: attachments (active, by module + entity). No writes.

---

### `GET /api/attachments/{id}` — Get attachment metadata by ID

**Purpose** — Returns the metadata of a single attachment by its unique identifier

**HTTP Method** — `GET`

**URL** — `/api/attachments/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Attachment ID |

**Example Response** (200 Attachment metadata retrieved)

```json
{
  "success": true,
  "message": "Attachment fetched successfully.",
  "data": {
    "id": 1,
    "originalFileName": "photo.jpg",
    "storedFileName": "b3f0a2c1-....jpg",
    "fileExtension": "jpg",
    "mimeType": "image/jpeg",
    "fileSize": 204800,
    "fileHash": "e3b0c44298fc1c149afbf4c8996fb924...",
    "uploadedBy": "EMP001",
    "uploadedAt": "2026-08-02T10:30:00",
    "relatedModule": "REPORT",
    "relatedEntityId": "12",
    "category": "INSPECTION_IMAGE",
    "description": "Inspection photo",
    "isActive": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Attachment not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findActiveById -> 404 if missing or inactive. Returns AttachmentResponse.

**Database Impact**

Read: attachments. No writes.

---

### `PUT /api/attachments/{id}` — Update attachment metadata

**Purpose** — Updates the metadata of an existing attachment by its unique identifier

**HTTP Method** — `PUT`

**URL** — `/api/attachments/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Attachment ID |

**Request Body** (application/json)

| Field | Type | Required | Description |
|---|---|---|---|
| `category` | String | No | Attachment category |
| `description` | String | No | Description of the attachment |
| `relatedModule` | String | No | Related module name |
| `relatedEntityId` | String | No | Related entity ID |

**Validation rules** — category: max 100 optional (enum AttachmentCategory; invalid -> ignored); description: max 500 optional; relatedModule: max 100 optional; relatedEntityId: max 100 optional.

**Example Request**

```json
{
  "category": "INSPECTION_IMAGE",
  "description": "Photo of the final inspection result",
  "relatedModule": "quality-inspection",
  "relatedEntityId": "REP-001234"
}
```

**Example Response** (200 Attachment updated successfully)

```json
{
  "success": true,
  "message": "Attachment updated successfully.",
  "data": {
    "id": 1,
    "originalFileName": "photo.jpg",
    "storedFileName": "b3f0a2c1-....jpg",
    "fileExtension": "jpg",
    "mimeType": "image/jpeg",
    "fileSize": 204800,
    "fileHash": "e3b0c44298fc1c149afbf4c8996fb924...",
    "uploadedBy": "EMP001",
    "uploadedAt": "2026-08-02T10:30:00",
    "relatedModule": "REPORT",
    "relatedEntityId": "12",
    "category": "INSPECTION_IMAGE",
    "description": "Inspection photo",
    "isActive": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 404 | Attachment not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`, `UpdateAttachmentRequest`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Loads active attachment (404).
3. Applies non-null category/description/relatedModule/relatedEntityId (category parsed via enum, invalid -> null).
4. Saves; returns AttachmentResponse.

**Database Impact**

Read: attachments. Write: attachments (UPDATE metadata).

---

### `DELETE /api/attachments/{id}` — Soft delete an attachment

**Purpose** — Soft deletes an attachment by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/attachments/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Attachment ID |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Attachment not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findActiveById (404).
3. SOFT delete: is_active=false; the physical file is NOT removed from disk.
4. Returns 204 No Content.

**Database Impact**

Read: attachments. Write: attachments (UPDATE is_active=false). File remains on disk.

---

## 24. Notifications

Per-user notifications. Any authenticated user; all data is scoped to the current user (ownership violations surface as 404 to hide existence). Read/delete are physical; mark-as-read updates the row.

**Workflow-triggered**: notifications are emitted by backend business flows and persisted in-app. Report flows (`create` -> `REPORT_CREATED`, `submit` -> `REPORT_SUBMITTED`, approve/reject -> `REPORT_APPROVED`/`REPORT_REJECTED` to the creator) and user/auth flows (user creation -> `USER_CREATED` + `WELCOME`, password change -> `PASSWORD_CHANGED`) write notifications via `NotificationChannel`. There are no external channels (email/SMS/push) yet.

**Enum reference**

- `NotificationType` (the `type` filter / field): `WELCOME`, `PASSWORD_CHANGED`, `REPORT_CREATED`, `REPORT_SUBMITTED`, `REPORT_APPROVED`, `REPORT_REJECTED`, `REPORT_RETURNED`, `PENDING_APPROVAL`, `APPROVAL_REMINDER`, `USER_CREATED`, `USER_ACTIVATED`, `USER_DEACTIVATED`, `ROLE_CHANGED`, `ATTACHMENT_UPLOADED`, `MAINTENANCE_NOTICE`.
- `NotificationPriority` (the `priority` field): `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

### `GET /api/notifications` — Get my notifications with pagination, type and read-status filtering

**Purpose** — Returns a paginated list of notifications for the current user, optionally filtered by type and read status

**HTTP Method** — `GET`

**URL** — `/api/notifications`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `type` | query | String | No | Filter by type |
| `isRead` | query | boolean | No | Filter by read status |
| `page` | query | int | No | Page number |
| `size` | query | int | No | Page size |

**Example Response** (200 Notifications fetched successfully)

```json
{
  "success": true,
  "message": "Notifications fetched successfully.",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Report Approved",
        "message": "PMR-20260802-00001 was approved",
        "type": "REPORT_APPROVED",
        "relatedModule": "PROCESS_MONITORING",
        "relatedEntityId": "1",
        "priority": "MEDIUM",
        "isRead": false,
        "readAt": null,
        "createdAt": "2026-08-02T10:30:00",
        "metadata": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request |
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Optional filters: type (NotificationType; invalid value silently ignored -> no type filter), isRead (Boolean), page/size. Sorted createdAt DESC, scoped to the current user. Returns PageResponse<NotificationResponse>.

**Database Impact**

Read: notifications (by recipient). No writes.

---

### `GET /api/notifications/count` — Get unread notification count

**Purpose** — Returns the number of unread notifications for the current user

**HTTP Method** — `GET`

**URL** — `/api/notifications/count`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Unread count fetched successfully)

```json
{
  "success": true,
  "message": "Unread count fetched successfully.",
  "data": {
    "count": 3
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. countByRecipientUserIdAndIsReadFalse(currentUser) -> number of unread notifications. Returns UnreadCountResponse {count}.

**Database Impact**

Read: notifications (COUNT unread). No writes.

---

### `GET /api/notifications/unread` — Get all unread notifications for the current user

**Purpose** — Returns a list of all unread notifications for the current user

**HTTP Method** — `GET`

**URL** — `/api/notifications/unread`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Unread notifications fetched successfully)

```json
{
  "success": true,
  "message": "Unread notifications fetched successfully.",
  "data": [
    {
      "id": 1,
      "title": "Report Approved",
      "message": "PMR-20260802-00001 was approved",
      "type": "REPORT_APPROVED",
      "relatedModule": "PROCESS_MONITORING",
      "relatedEntityId": "1",
      "priority": "MEDIUM",
      "isRead": false,
      "readAt": null,
      "createdAt": "2026-08-02T10:30:00",
      "metadata": null
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser) -> full UNPAGED list of unread notifications. Returns List<NotificationResponse>.

**Database Impact**

Read: notifications (unread, by recipient). No writes.

---

### `PATCH /api/notifications/{id}/read` — Mark a single notification as read

**Purpose** — Marks a single notification as read by its unique identifier

**HTTP Method** — `PATCH`

**URL** — `/api/notifications/{id}/read`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Notification ID |

**Example Response** (200 Notification marked as read)

```json
{
  "success": true,
  "message": "Notification marked as read.",
  "data": {
    "id": 1,
    "title": "Report Approved",
    "message": "PMR-20260802-00001 was approved",
    "type": "REPORT_APPROVED",
    "relatedModule": "PROCESS_MONITORING",
    "relatedEntityId": "1",
    "priority": "MEDIUM",
    "isRead": true,
    "readAt": "2026-08-02T10:30:00",
    "createdAt": "2026-08-02T10:30:00",
    "metadata": null
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Notification not found |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findById (404).
3. Ownership check: if the notification's recipient != current user -> 404 (existence hidden deliberately, not 403).
4. Sets isRead=true, readAt=now; saves. Returns NotificationResponse.

**Database Impact**

Read: notifications. Write: notifications (UPDATE is_read/read_at).

---

### `PATCH /api/notifications/read-all` — Mark all notifications as read for the current user

**Purpose** — Marks all notifications of the current user as read

**HTTP Method** — `PATCH`

**URL** — `/api/notifications/read-all`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 All notifications marked as read)

```json
{
  "success": true,
  "message": "All notifications marked as read.",
  "data": null
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. Bulk JPQL update: SET isRead=true, readAt=now WHERE recipient=currentUser AND isRead=false.
3. Returns ApiResponse with data=null.

**Database Impact**

Write: notifications (bulk UPDATE is_read/read_at for the current user).

---

### `DELETE /api/notifications/{id}` — Delete a single notification

**Purpose** — Deletes a single notification by its unique identifier

**HTTP Method** — `DELETE`

**URL** — `/api/notifications/{id}`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters**

| Param | In | Type | Required | Description |
|---|---|---|---|---|
| `id` | path | long | Yes | Notification ID |

**Example Response** — HTTP 204 No Content (empty body).

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 404 | Notification not found |
| 500 | Internal server error |

**DTOs** — `ApiError`

**Business Flow**

1. @PreAuthorize('isAuthenticated()').
2. findById (404) + ownership check (404 if not owner).
3. PHYSICAL delete of the row.
4. Returns 204 No Content.

**Database Impact**

Read: notifications. Write: notifications (DELETE - physical).

---

## 25. Audit Logs

Read-only audit service. Restricted to `SUPER_ADMIN`/`ADMIN`. IMPORTANT: no code path writes to `audit_logs` — this table stays empty unless populated externally, so these endpoints currently return empty/zero data.

**Enum reference**

- `AuditModule` (the `module` filter / field): `AUTHENTICATION`, `USER_MANAGEMENT`, `SHIFT_MASTER`, `LINE_MASTER`, `PROCESS_MASTER`, `PARAMETER_MASTER`, `PROCESS_MONITORING`, `CHEMICAL_CONSUMPTION`, `DAILY_STARTUP`, `FIRST_PIECE_INSPECTION`, `DAILY_INSPECTION`, `PRE_DELIVERY_INSPECTION`, `SYSTEM`, `DASHBOARD`, `APPROVAL_CENTER`.
- `AuditAction` (the `action` filter / field): `LOGIN`, `LOGOUT`, `FAILED_LOGIN`, `PASSWORD_CHANGE`, `TOKEN_REFRESH`, `CREATE`, `UPDATE`, `DELETE`, `ACTIVATE`, `DEACTIVATE`, `ROLE_CHANGE`, `DRAFT_SAVED`, `SUBMIT`, `APPROVE`, `REJECT`, `CANCEL`, `ATTACHMENT_UPLOAD`, `ATTACHMENT_DELETE`.

### `GET /api/audit-logs` — Get paginated audit logs with filtering and sorting

**Purpose** — Retrieves audit log entries with optional filters (user, module, action, date range), sorting and pagination. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/audit-logs`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Request Parameters** (all optional; bound to `AuditFilterRequest`)

| Param | In | Type | Description |
|---|---|---|---|
| `userId` | query | long | User ID to filter by |
| `module` | query | string | Module to filter by (e.g. `PROCESS_MONITORING`) |
| `action` | query | string | Action to filter by (e.g. `CREATE`) |
| `dateFrom` | query | date-time | Start date-time (ISO `yyyy-MM-ddTHH:mm:ss`) |
| `dateTo` | query | date-time | End date-time (ISO `yyyy-MM-ddTHH:mm:ss`) |
| `sortBy` | query | string | Sort field: `timestamp`, `module`, `action`, `employeeId` (default `timestamp`) |
| `sortDirection` | query | string | `ASC` or `DESC` (default `DESC`) |
| `page` | query | int | Page number (default 0) |
| `size` | query | int | Page size (default 20) |

**Example Response** (200 Audit logs fetched successfully)

```json
{
  "success": true,
  "message": "Audit logs fetched successfully.",
  "data": {
    "content": [
      {
        "id": 1,
        "timestamp": "2026-08-02T10:30:00",
        "userId": 1,
        "employeeId": "EMP001",
        "username": "John Doe",
        "userRole": "OPERATOR",
        "module": "PROCESS_MONITORING",
        "entityType": "REPORT",
        "entityId": "12",
        "action": "CREATE",
        "previousValue": null,
        "newValue": "{}",
        "ipAddress": "192.168.1.10",
        "userAgent": "Mozilla/5.0",
        "metadata": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 400 | Bad Request - validation error or malformed request body |
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Optional query filters (userId, module, action, dateFrom, dateTo) build an AuditLogSpecification; sortBy is whitelisted (timestamp/module/action/employeeId) else timestamp DESC.
3. Paged result mapped to PageResponse<AuditLogResponse>.
4. IMPORTANT: the audit_logs table is NEVER written by any code path — this endpoint serves whatever rows exist externally (typically empty).

**Database Impact**

Read: audit_logs (paged select with filters). No writes anywhere in the app.

---

### `GET /api/audit-logs/recent` — Get the 10 most recent audit log entries

**Purpose** — Retrieves the 10 most recent audit log entries. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/audit-logs/recent`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Recent activities fetched successfully)

```json
{
  "success": true,
  "message": "Recent activities fetched successfully.",
  "data": [
    {
      "id": 1,
      "timestamp": "2026-08-02T10:30:00",
      "userId": 1,
      "employeeId": "EMP001",
      "username": "John Doe",
      "userRole": "OPERATOR",
      "module": "PROCESS_MONITORING",
      "entityType": "REPORT",
      "entityId": "12",
      "action": "CREATE",
      "previousValue": null,
      "newValue": "{}",
      "ipAddress": "192.168.1.10",
      "userAgent": "Mozilla/5.0",
      "metadata": null
    }
  ]
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. findTop10ByOrderByTimestampDesc() -> latest 10 rows. Message 'Recent activities fetched successfully.'

**Database Impact**

Read: audit_logs (LIMIT 10). No writes.

---

### `GET /api/audit-logs/statistics` — Get audit statistics (totals, counts by module and action)

**Purpose** — Retrieves aggregated audit statistics including total log count, today's count, and counts grouped by module and action. Accessible to users with the SUPER_ADMIN or ADMIN role.

**HTTP Method** — `GET`

**URL** — `/api/audit-logs/statistics`

**Authorization** — `Authorization: Bearer <accessToken>` (authenticated user; role checks listed below).

**Request Headers**

| Header | Value | Required |
|---|---|---|
| `Authorization` | `Bearer <accessToken>` | Yes |
| `Content-Type` | `application/json` | Yes (when a body is sent) |

**Example Response** (200 Audit statistics fetched successfully)

```json
{
  "success": true,
  "message": "Audit statistics fetched successfully.",
  "data": {
    "totalLogs": 10,
    "todayCount": 2,
    "logsByModule": [
      {
        "module": "PROCESS_MONITORING",
        "count": 4
      }
    ],
    "logsByAction": [
      {
        "action": "CREATE",
        "count": 6
      }
    ]
  }
}
```

**Error Responses** — error bodies use the `ApiError` envelope (section 3).

| Code | Meaning |
|---|---|
| 401 | Unauthorized - authentication required |
| 403 | Forbidden - requires ADMIN or SUPER_ADMIN role |
| 500 | Internal server error |

**DTOs** — `ApiError`, `ApiResponse`

**Business Flow**

1. @PreAuthorize(hasAnyRole('SUPER_ADMIN','ADMIN')).
2. Aggregates: total count, count since midnight today, counts grouped by module, counts grouped by action. Returns AuditStatisticsResponse.

**Database Impact**

Read: audit_logs (COUNT queries, GROUP BY). No writes.

---

## 26. Appendix A — DTO Reference

The request DTOs below are the component schemas exposed by the OpenAPI spec. Response payloads are documented inline per endpoint; the spec models every response through a concrete typed `ApiResponse<T>` variant (e.g. `ApiResponseUserResponse`, `ApiResponsePageResponseAuditLogResponse`) whose `data` resolves to the endpoint's actual DTO, list, or page schema.

| Schema (DTO) | Kind | Purpose |
|---|---|---|
| `ApiError` | Response | Standard API error response envelope returned on all failed requests |
| `ApiResponse` | Model | Generic response wrapper; materialized in the spec as per-endpoint typed variants (e.g. `ApiResponseUserResponse`) |
| `ApproveChemicalConsumptionRequest` | Model | Request payload for approving or rejecting a Chemical Consumption report |
| `ApproveDailyInspectionRequest` | Model | Request payload for approving or rejecting a Daily Inspection report |
| `ApproveDailyStartupRequest` | Model | Request payload for approving or rejecting a Daily Startup report |
| `ApproveFirstPieceInspectionRequest` | Model | Request payload for approving or rejecting a First Piece Inspection report |
| `ApprovePreDeliveryInspectionRequest` | Model | Request payload for approving or rejecting a Pre-Delivery Inspection report |
| `ApproveReportRequest` | Model | Request payload for approving or rejecting a Process Monitoring report |
| `AuditFilterRequest` | Model | Request to filter audit log entries |
| `BulkUpdateItem` | Request | A single setting key-value pair for bulk update |
| `BulkUpdateSettingsRequest` | Request | Request to bulk update system settings |
| `ChangePasswordRequest` | Request | Request payload for changing the current user's password |
| `ChemicalConsumptionEntryRequest` | Request | An individual entry within a Chemical Consumption report |
| `CreateChemicalConsumptionRequest` | Request | Request payload for creating a Chemical Consumption report |
| `CreateDailyInspectionRequest` | Request | Request payload for creating a Daily Inspection report |
| `CreateDailyStartupRequest` | Request | Request payload for creating a Daily Startup report |
| `CreateFirstPieceInspectionRequest` | Request | Request payload for creating a First Piece Inspection report |
| `CreateIntegrationRequest` | Request | Request to create a new integration |
| `CreateLineRequest` | Request | Request body for creating a new production line |
| `CreateParameterRequest` | Request | Request body for creating a new inspection parameter |
| `CreatePreDeliveryInspectionRequest` | Request | Request payload for creating a Pre-Delivery Inspection report |
| `CreateProcessMonitoringRequest` | Request | Request payload for creating a Process Monitoring report |
| `CreateSettingRequest` | Request | Request to create a new system setting |
| `CreateShiftRequest` | Request | Request body for creating a new shift |
| `CreateUserRequest` | Request | Request payload for creating a new user |
| `DailyInspectionEntryRequest` | Request | An individual entry within a Daily Inspection report |
| `DailyStartupEntryRequest` | Request | An individual entry within a Daily Startup report |
| `FirstPieceInspectionEntryRequest` | Request | An individual entry within a First Piece Inspection report |
| `LoginRequest` | Request | Login request payload |
| `PreDeliveryInspectionEntryRequest` | Request | An individual entry within a Pre-Delivery Inspection report |
| `ProcessMonitoringEntryRequest` | Request | An individual entry within a Process Monitoring report |
| `RefreshTokenRequest` | Request | Refresh token request payload |
| `SubmitChemicalConsumptionRequest` | Model | Request payload for submitting a Chemical Consumption report |
| `SubmitDailyInspectionRequest` | Model | Request payload for submitting a Daily Inspection report |
| `SubmitDailyStartupRequest` | Model | Request payload for submitting a Daily Startup report |
| `SubmitFirstPieceInspectionRequest` | Model | Request payload for submitting a First Piece Inspection report |
| `SubmitPreDeliveryInspectionRequest` | Model | Request payload for submitting a Pre-Delivery Inspection report |
| `SubmitReportRequest` | Model | Request payload for submitting a Process Monitoring report |
| `UpdateAttachmentRequest` | Model | Request to update an existing attachment's metadata |
| `UpdateIntegrationRequest` | Request | Request to update an existing integration |
| `UpdateLineRequest` | Request | Request body for updating an existing production line |
| `UpdateParameterRequest` | Request | Request body for updating an existing inspection parameter |
| `UpdateSettingRequest` | Request | Request to update an existing system setting |
| `UpdateShiftRequest` | Request | Request body for updating an existing shift |
| `UpdateStatusRequest` | Request | Request payload for updating a user's active status |
| `UpdateUserRequest` | Request | Request payload for updating an existing user |

## 27. Appendix B — Status Codes

| Code | Meaning | Usage |
|---|---|---|
| 200 | OK | Read/update/action success |
| 201 | Created | Create endpoints (with `Location` header) |
| 204 | No Content | Delete endpoints (empty body) |
| 400 | Bad Request | Bean-validation failure, invalid enum/type, business-rule conflict (e.g. wrong status transition) |
| 401 | Unauthorized | Missing/invalid token or anonymous access to a guarded endpoint |
| 403 | Forbidden | Authenticated but role not sufficient; path-traversal guard in file downloads |
| 404 | Not Found | Unknown id/key, missing entity, non-completed attachment file |
| 409 | Conflict | Data-integrity violation (e.g. FK conflict on physical delete) |
| 500 | Internal Server Error | Unhandled exception (incl. unhandled DisabledException) |
