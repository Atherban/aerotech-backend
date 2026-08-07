# CED Operations — REST API Documentation

> Complete reference for every HTTP endpoint implemented in the `ced-ops-backend` Spring Boot service (Java 21, Spring Boot 3). Sources of truth: the controller/service/entity source code (verified: **118 HTTP operations across 87 path templates** across 18 controllers) and the committed OpenAPI 3 snapshot (`api-docs.json`, regenerated from source and kept in sync). Every successful JSON response is wrapped in the `ApiResponse<T>` envelope; every error uses the `ApiError` envelope.

## Table of Contents

1. [Overview](#1-overview)
2. [Authentication & Authorization](#2-authentication--authorization)
3. [Common Response Envelope](#3-common-response-envelope)
4. [Pagination](#4-pagination)
5. [Report Engine Workflow](#5-report-engine-workflow)
6. [Authentication](#6-authentication)
7. [Users](#7-users)
8. [Master Data — Lines](#8-master-data--lines)
9. [Master Data — Shifts](#9-master-data--shifts)
10. [Settings](#10-settings)
11. [Dashboard](#11-dashboard)
12. [Global Search](#12-global-search)
13. [Analytics](#13-analytics)
14. [Integrations](#14-integrations)
15. [Attachments](#15-attachments)
16. [Notifications](#16-notifications)
17. [Audit Logs](#17-audit-logs)
18. [Module-Driven Master Data APIs](#18-module-driven-master-data-apis)
19. [Configuration-Driven Report Engine APIs](#19-configuration-driven-report-engine-apis)
20. [Appendix A — DTO Reference](#20-appendix-a--dto-reference)
21. [Appendix B — Status Codes](#21-appendix-b--status-codes)

## 1. Overview

The backend exposes a production-operations domain: user & role management, master data (lines, shifts, and the module-driven hierarchy — module types, modules, template versions, processes, global parameters), a **configuration-driven report engine** (report sessions and completed reports), a dashboard, unified search over reports/users/parameters, analytics, integrations, attachments, notifications, settings, and a (currently read-only) audit-log service. The backend provides structured JSON APIs only; PDF/Excel/CSV/print export is implemented by the frontend.

Key conventions:
- **Base URL:** `http://<host>:3000` (`application.properties`; PostgreSQL `ced_ops` on port 5432).
- **Content type:** `application/json` for JSON bodies/responses; `multipart/form-data` for uploads.
- **Response envelope:** every success is `ApiResponse<T>` (`{success, message, data}`); every failure is `ApiError` (section 3).
- **Pagination:** list endpoints return `PageResponse<T>` and accept `page`/`size` (section 4).
- **Report lifecycle:** `ReportSession IN_PROGRESS → COMPLETED`; a completed `report` starts `SUBMITTED` (approval columns are forward-compatible). See section 5 and 19.

> **Migration status (Phase 5 complete):** the legacy hardcoded ReportType
> architecture (six report modules, the report-type parameter catalog, the
> report-type catalog endpoint, and the dead legacy search stack) has been
> **removed**. The Generic Report Engine is the only report architecture; its
> schema is `report_session` / `recorded_process` / `recorded_value` / `report`.

Report-type catalog:

The engine schema: `report_session`, `recorded_process`, `recorded_value`, and
`report` (completed report).

Other core tables: `users`, `roles`, `refresh_token`, `line_master`, `shifts`,
`module_type`, `module`, `module_template_version`, `module_process`,
`parameter`, `process_parameter`, `system_settings`, `integrations`,
`integration_execution_histories`, `attachments`, `notifications`,
`audit_logs`.

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
| Lines, Shifts master data create/update | `SUPER_ADMIN`, `ADMIN` |
| Lines, Shifts master data delete | `SUPER_ADMIN` only |
| Lines, Shifts reads | any authenticated user |
| Module-driven master data write (module types, modules, processes, parameters) | `SUPER_ADMIN`, `ADMIN` |
| Module-driven master data read | any authenticated user |
| Settings create/update/delete | `SUPER_ADMIN` only |
| Settings reads | `SUPER_ADMIN`, `ADMIN` |
| Report engine (start session, save-next, save-submit, list my reports/sessions) | any authenticated user |
| Dashboard, Unified Search, Attachments, Notifications | any authenticated user |
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
  DTOs extend it (e.g. `UserFilterRequest`, `ParameterFilterRequest`).
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
| `GET /api/module-parameters` | `ParameterFilterRequest` | `inputType`, `active` |
| `GET /api/processes/{id}/parameters` | (paged via engine) | processId |

**Sort whitelists** — Users: `id`, `employeeId`, `firstName`, `lastName`,
`role`, `active`, `createdAt`. Module-driven master data: per-module whitelists
(see sections 18 and 19). Engine report/session lists: `id`, `reportNumber`,
`status`, `createdAt`, `startedAt`, `submittedAt`.

## 5. Report Engine Workflow

The report lifecycle is executed entirely by the **configuration-driven report
engine** (`/api/report-engine`, see section 19). The state machine is
backend-authoritative; the frontend only renders each step and never computes
navigation.

**Session state machine:**

```
Start → IN_PROGRESS ──(save-next per process, advance by displayOrder)──► … ──►
            └── save-submit (final process) ──► COMPLETED  →  report (SUBMITTED)
```

- **Start:** freezes the module's latest ACTIVE template version on a new
  `ReportSession`; captures the shift/line passed at start (shift auto-detected
  from the current time when omitted) and returns the first process step.
- **Save-next:** records the current process (grouped under a `RecordedProcess`
  with a process-order snapshot) and advances to the next process by
  `displayOrder`. Mandatory visible fields must be provided, else 400.
- **Save-submit:** records the final process, completes the session and creates
  + submits the completed `report` with immutable snapshots of the module,
  version, shift/line, and each parameter's spec (name/unit/inputType/min/max).
- **No update endpoint:** a completed report cannot be edited or resumed, and the
  approval workflow (approve/reject) is not yet implemented (forward-compatible
  `approvedAt`/`approvedBy` columns exist).

> See `API_DOCUMENTATION.md` section 19 for the full endpoint contract.

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

## 10. Settings

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

## 11. Dashboard

Dashboard KPIs are read-only and any authenticated user may call them. Since
Phase 4 the metrics are computed **exclusively over the Generic Report Engine's
`report` (`completed_report`) table** (no longer the six legacy per-type report
tables). The response contracts below are unchanged. No query parameters are used
except `GET /api/reports/dashboard/recent-activity?limit=N`.

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

SELECT status, COUNT(*) FROM report GROUP BY status -> buckets for SUBMITTED (+ forward-compatible approvals) plus total. Returns DashboardSummaryResponse {totalReports, submittedReports, approvedReports, rejectedReports}.

**Database Impact**

Read: 1 native aggregate SELECT over the engine `report` table. No writes.

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
      "reportType": "Extrusion",
      "count": 40
    },
    {
      "reportType": "Coating",
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

SELECT module_name, COUNT(*) FROM `report` GROUP BY module_name ORDER BY module_name -> counts per module (labels are the Module names). Returns List of {reportType (=module name), count}.

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
      "reportNumber": "RPT-20260802-00012",
      "reportType": "Extrusion",
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

Recent union over `report` (module_name as reportType) LEFT JOIN shifts/line_master/users, ORDER BY created_at DESC LIMIT 10; created_by = COALESCE(CONCAT(first_name,' ',last_name), employee_id). Returns List of {id, reportNumber, reportType (=module name), reportDate, status, shiftName, lineName, createdBy, createdAt}.

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

1. Group `status` over the engine `report` table -> `pendingApprovals` (SUBMITTED), `approvedReports`, `rejectedReports`.
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

1. Builds lifecycle events over the engine `report` table: a `CREATED` event from each row (`created_by`/`created_at`) plus `APPROVED`/`REJECTED` events (derived from `approved_at`).
2. Left-joins `users` for the actor's display name, orders by event time descending, applies `LIMIT :limit`.
3. Returns `RecentActivityResponse` items (lightweight for mobile feeds).

**Database Impact**

Read: 1 native SELECT over the engine `report` table + `users` join. No writes.

---

## 12. Global Search

Read-only search across report data, users and parameters. The unified search
runs against the **engine's `report` (`completed_report`) table**, the `users`
table, and the module architecture's global `parameter` table.

- `GET /api/search` — **unified enterprise search** across reports, users, and
  parameters, built on the shared pagination framework (`PageRequest` /
  `PageResponse`). The legacy report-only search path (`/api/reports/search`)
  and the legacy per-type search stack were removed in Phase 5.

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
| `keyword` | query | string | Free-text match on title / subtitle / module name / actor |
| `reportNumber` | query | string | Partial report number / parameter name / employee ID |
| `reportType` | query | string | Module-name filter (matches the report's module, engine reports only) |
| `status` | query | string | Report status filter |
| `employeeName` | query | string | Employee name filter (report creator / user full name) |
| `role` | query | string | User role filter (users only) |
| `shiftId` | query | long | Shift ID filter (reports only) |
| `lineId` | query | long | Line ID filter (reports only) |
| `moduleId` | query | long | Module filter (engine reports only) |
| `moduleTypeId` | query | long | Module-type filter (engine reports only) |
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
        "title": "RPT-20260802-00031",
        "subtitle": "Extrusion",
        "reportType": "Extrusion",
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
2. `UnifiedSearchQueryBuilder` builds a union of the **engine `report` table**, the `users` table and the module architecture's global `parameter` table, each normalized to a common shape (`type, id, title, subtitle, report_type, status, shift_name, line_name, actor, report_date, created_at, role_name, shift_id, line_id`).
3. Filters (`keyword`, `reportNumber`, `reportType`, `status`, `employeeName`, `role`, `shiftId`, `lineId`, `moduleId`, `moduleTypeId`, `dateFrom`, `dateTo`) are applied as optional parameterized conditions; `type` restricts which branches are unioned.
4. Count query for `totalElements`; data query with `LIMIT :size OFFSET :offset` and a whitelisted `sortBy`.
5. Returns `PageResponse<UnifiedSearchResultItem>` (lightweight, mobile-friendly).

**Database Impact**

Read: 1-2 native SELECTs (count + data) over the union. No writes.

---

## 13. Analytics

Analytics aggregates are read-only. Since Phase 4 they are computed **over the
engine's `report` (`completed_report`) + `recorded_value` tables** (no longer the
six legacy report + entry tables). Restricted to `SUPER_ADMIN`/`ADMIN`. Optional
filters: `dateFrom`, `dateTo`, `shiftId`, `lineId` (per endpoint). Entry-level
PASS/FAIL is **config-driven**: a numeric `recorded_value.observedValue` is
compared against the frozen `minimum_value`/`maximum_value` snapshot (FAIL below
min or above max). No validation that `dateFrom <= dateTo`; a malformed date -> 400.

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

Read: aggregate SELECTs over the engine `report` + `recorded_value` tables. No writes.

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

Restricted to engine reports. KPIs: total (filtered) + today's reports (note: dailyCount ignores date filters in code). Trends daily/weekly (DATE_TRUNC week)/monthly; `consumption` sums numeric bounded recorded values, per line (JOIN line_master). Returns ChemicalConsumptionKPIResponse.

**Database Impact**

Read: aggregate SELECTs over the engine `report`/`recorded_value` + `line_master`. No writes.

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

Aggregates `recorded_value` (JOIN `report`/`recorded_process`) grouped by pass/fail + parameter_name. Entry status is config-driven (numeric observed value within the frozen min/max snapshot). Computes pass/fail counts, process stability (PASS %), failureFrequency (FAIL per parameter), outOfSpecParameters (FAIL per parameter), totalReports. Returns ProcessMonitoringKPIResponse.

**Database Impact**

Read: aggregate SELECTs over the engine `report`/`recorded_process`/`recorded_value`. No writes.

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

## 14. Integrations

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

## 15. Attachments

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

## 16. Notifications

Per-user notifications. Any authenticated user; all data is scoped to the current user (ownership violations surface as 404 to hide existence). Read/delete are physical; mark-as-read updates the row.

**Source of notifications**: notifications are written in-app by the `user`
module workflows only — user creation (`USER_CREATED` + `WELCOME`) and password
change (`PASSWORD_CHANGED`). The report engine does **not** emit report-flow
notifications, and the notification backend does not currently write
`REPORT_*` notifications. There are no external channels (email/SMS/push) yet.

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

## 17. Audit Logs

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

## 18. Module-Driven Master Data APIs

> Module-driven migration (see `MIGRATION_PLAN.md`). **config-oriented**
> master-data endpoints for the hierarchy
> `Module Type → Module → Template Version → Process → Process Parameter →
> (global) Parameter`. The legacy report-type/parameter endpoints have been
> **removed** (Phase 5). The report engine APIs are in section 19 and the
> Dashboard / Global Search / Analytics read the engine. Conventions match the
> rest of the system: `ApiResponse<T>` wrapper,
> `PageResponse<T>` for filtered lists, `@PreAuthorize` RBAC (write = Super
> Admin / Admin, read = any authenticated user), and shared pagination
> (`page`/`size`/`sortBy`/`sortDirection`/`keyword`) where noted.

### `POST /api/module-types` — Create a module type

Request `CreateModuleTypeRequest`: `name` (required, ≤100), `description` (≤300).
Returns `ModuleTypeResponse` (`id`, `name`, `description`, `active`).

### `GET /api/module-types` — List module types

Without paging/filter params returns the full `ModuleTypeResponse[]`; with any
param returns `PageResponse<ModuleTypeResponse>`. Filters: `keyword` (name,
description), `active`.

### `GET /api/module-types/{id}` — Get a module type
### `PUT /api/module-types/{id}` — Update a module type
### `DELETE /api/module-types/{id}` — Deactivate a module type (Super Admin only)

### `POST /api/modules` — Create a module (and its initial template version)

Data: `CreateModuleRequest` — `moduleTypeId` (required), `name` (required, ≤150,
unique), `prefix` (required, ≤10, unique, uppercased), `description` (≤500),
`changeNote` (required, used for the initial version). Creates the module in
`DRAFT` status together with template version **1** (`DRAFT`). Returns:
`ModuleResponse`.

### `GET /api/modules` — List modules

Full list, or `PageResponse<ModuleResponse>` when paged. Filters: `moduleTypeId`,
`status` (`DRAFT`/`ACTIVE`/`ARCHIVED`), keyword (name, prefix, description).

`ModuleResponse` carries `moduleType` (summary), `name`, `prefix`, `description`,
`status`, and `latestActiveVersion` (the newest `ACTIVE` template version — the
one new reports must use).

### `GET /api/modules/{id}` — Get a module
### `PUT /api/modules/{id}` — Update a module (name/prefix/description; status unchanged)
### `PATCH /api/modules/{id}/archive` — Archive a module (Super Admin only)

Sets status `ACTIVE → ARCHIVED` so no new reports can be created against it.

### `GET /api/modules/{id}/versions` — List a module's template versions

Returns `List<TemplateVersionResponse>` newest first. Each: `id`, `moduleId`,
`versionNumber`, `status` (`DRAFT`/`ACTIVE`/`SUPERSEDED`), `changeNote`.

### `POST /api/modules/{id}/versions` — Create a new template version

Data: `CreateTemplateVersionRequest` (`changeNote`, required). **Snapshots** the
current latest `ACTIVE` version's processes and process parameters into a new
`DRAFT` version with `versionNumber = latest + 1` (falls back to 1 when the
module has no versions). The source ACTIVE version is never modified. Returns the
new `TemplateVersionResponse`.

### `POST /api/modules/{id}/versions/{versionId}/publish` — Publish a template version

Only a `DRAFT` version can be published. Flips the version to `ACTIVE`,
marks every other `ACTIVE` version of the module `SUPERSEDED`, and if the module
is still `DRAFT` marks it `ACTIVE`. Returns the published
`TemplateVersionResponse`.

### `GET /api/modules/{id}/versions/{versionId}/processes` — List a version's processes

Returns `List<ProcessResponse>` ordered by `displayOrder`. `ProcessResponse`:
`id`, `templateVersionId`, `name`, `description`, `displayOrder`, `status`.

### `GET /api/modules/{id}/versions/{versionId}/process-parameters` — List a version's process parameters

Returns `List<ProcessParameterResponse>` ordered by `displayOrder` (grouped by
process). `ProcessParameterResponse`: `id`, `processId`, `parameter` (summary),
`displayOrder`, `mandatory`, `visible`, `defaultValue`, `unit`, `minimumValue`,
`maximumValue`, `active`, `inputType` (derived from the bound global parameter).

### `POST /api/processes` — Create a process

Data: `CreateProcessRequest` — `templateVersionId` (required), `name` (required,
≤150, unique per version), `description` (≤500), `displayOrder` (required ≥1).
Created `ACTIVE`. Superseded template versions are rejected.

### `GET /api/processes` — List processes

Returns: full list or `PageResponse<ProcessResponse>` when filters present.
Filters: `templateVersionId`, `status` (`DRAFT`/`ACTIVE`/`ARCHIVED`).

### `GET /api/processes/{id}` — Get a process
### `PUT /api/processes/{id}` — Update a process (name/description/displayOrder)
### `PATCH /api/processes/{id}/archive` — Archive a process

Soft lifecycle — marks the process `ARCHIVED`.

### `GET /api/processes/{processId}/parameters` — List a process's parameter bindings

Returns `List<ProcessParameterResponse>` ordered by `displayOrder`.

### `POST /api/processes/{processId}/parameters` — Bind a global parameter to a process

Data: `CreateProcessParameterRequest` — `parameterId` (required), `displayOrder`
(required ≥1), `mandatory` (default `true`), `visible` (default `true`),
`defaultValue`, `unit`, `minimumValue`, `maximumValue`. Rejects inactive
parameters and duplicate bindings.

### `GET /api/processes/{processId}/parameters/{id}` — Get a binding
### `PUT /api/processes/{processId}/parameters/{id}` — Update a binding
### `DELETE /api/processes/{processId}/parameters/{id}` — Deactivate a binding (Super Admin only)

### `POST /api/module-parameters` — Create a global parameter

Data: `CreateParameterRequest` — `name` (required, ≤150, unique), `inputType`
(`NUMBER`/`TEXT`/`BOOLEAN`/`DROPDOWN`, required), `description` (≤500). Created
`active=true`.

### `GET /api/module-parameters` — List global parameters

Returns: full list or `PageResponse<ParameterResponse>` when filters present.
Filters: `inputType`, `active`. `ParameterResponse`: `id`, `name`, `inputType`,
`description`, `active`.

### `GET /api/module-parameters/{id}` — Get a global parameter
### `PUT /api/module-parameters/{id}` — Update a global parameter
### `DELETE /api/module-parameters/{id}` — Deactivate a global parameter (Super Admin only)

---

## 19. Configuration-Driven Report Engine APIs

> A generic, **configuration-driven report engine** (`/api/report-engine`) that
> works exclusively against the module hierarchy. There is **no report-specific
> Java code** — every process and field is derived from the module config. The
> legacy ReportType architecture has been **removed** (Phase 5); this engine is
> the only report API. Workflow (backend-authoritative):
>
> `Start → Save processes one-by-one → Save & Submit → Completed Report`.
>
> The engine captures **immutable snapshots** on the completed report and
> recorded values (module name/prefix, template version number, module type,
> shift/line id + name), and `start` accepts an optional `shiftId`/`lineId`.
> The Dashboard / Global Search / Analytics read these tables (sections 11–13).

### `POST /api/report-engine/start` — Start a report session

Data: `StartReportRequest` — `moduleId` (required, must be ACTIVE), `shiftId` /
`lineId` (optional). When supplied, the shift/line **names are resolved once and
frozen**; otherwise the shift is auto-detected for the current time and no line is
set.

Behavior: resolves the module's **latest ACTIVE template version** and **freezes
it** on the new session; sets `currentProcess` to the first ACTIVE process (by
`displayOrder`). Returns `ReportSessionResponse`: `id`, `moduleId`, `moduleName`,
`templateVersionId`, `versionNumber`, `currentProcessId`, `startedAt`,
`completedProcessCount`, `status` (`IN_PROGRESS`), `shiftId`/`shiftName`/`lineId`/`lineName`.

### `GET /api/report-engine/sessions/{sessionId}` — Get a session snapshot

Returns the current `ReportSessionResponse` (or 404).

### `GET /api/report-engine/sessions/{sessionId}/current` — Load the current process step

Returns the `ReportProcessStep` the frontend must render: `processId`, `name`,
`description`, `displayOrder`, `lastProcess` (bool), and `fields`
(`ProcessParameterField` list — ``processParameterId`, `parameterId`,
`parameterName`, `inputType`, `mandatory`, `unit`, `minimumValue`,
`maximumValue`, `defaultValue`), all backend-derived from the frozen template.
The frontend only renders this; it never computes navigation.

### `POST /api/report-engine/sessions/{sessionId}/save-next` — Save current process, get next

Data: `RecordProcessRequest` — `values`: `[ { processParameterId, observedValue } ]`.

Values must cover every **mandatory** visible field, else 400. Records the current
process (with a `processOrderSnapshot`), advances `currentProcess` to the next
process **by `displayOrder`**, and returns `RecordProcessResponse`. When the saved
process **was the last**, the session is completed and the completed report is
created and submitted (`reportCompleted=true`, `report` populated,
`nextProcess=null`).

### `POST /api/report-engine/sessions/{sessionId}/save-submit` — Save final process & submit

Same request shape as save-next. Records the current process and unconditionally
completes + submits the report. Returns `RecordProcessResponse` with
`reportCompleted=true` and the `report` (`CompletedReportResponse`:
`id`, `reportNumber`, `moduleName`, `versionNumber`, `prefix`, `moduleType`,
`shiftId`/`shiftName`/`lineId`/`lineName`, `startedAt`, `submittedAt`, `status`
(`SUBMITTED`), `sessionId`).

### `GET /api/report-engine/sessions/{sessionId}/recorded`

Returns the session's `RecordedProcessItem` list — each with `id`, `processId`,
`processName`, `processOrderSnapshot`, and its **grouped** `values`
(`RecordedValueItem`: `id`, `processParameterId`, `parameterId`, `parameterName`,
`inputType`, `unit`, `minimumValue`, `maximumValue`, `observedValue`). Values are
grouped under their process; never flattened.

### `GET /api/report-engine/reports/{reportId}` — Get a submitted report
### `GET /api/report-engine/reports/my` — List my submitted reports
### `GET /api/report-engine/sessions/my` — List my in-progress sessions

Read helpers for the current user (RBAC `isAuthenticated`).

---

## 20. Appendix A — DTO Reference

The request DTOs below are the component schemas exposed by the OpenAPI spec. Response payloads are documented inline per endpoint; the spec models every response through a concrete typed `ApiResponse<T>` variant (e.g. `ApiResponseUserResponse`, `ApiResponsePageResponseAuditLogResponse`) whose `data` resolves to the endpoint's actual DTO, list, or page schema.

| Schema (DTO) | Kind | Purpose |
|---|---|---|
| `ApiError` | Response | Standard API error response envelope returned on all failed requests |
| `ApiResponse` | Model | Generic response wrapper; materialized in the spec as per-endpoint typed variants (e.g. `ApiResponseUserResponse`) |
| `AuditFilterRequest` | Model | Request to filter audit log entries |
| `BulkUpdateItem` | Request | A single setting key-value pair for bulk update |
| `BulkUpdateSettingsRequest` | Request | Request to bulk update system settings |
| `ChangePasswordRequest` | Request | Request payload for changing the current user's password |
| `CompletedReportResponse` | Response | A completed engine report with immutable snapshots (module/template/values) |
| `CreateIntegrationRequest` | Request | Request to create a new integration |
| `CreateLineRequest` | Request | Request body for creating a new production line |
| `CreateModuleRequest` | Request | Request body for creating a module |
| `CreateModuleTypeRequest` | Request | Request body for creating a module type |
| `CreateParameterRequest` | Request | Request body for creating a module parameter |
| `CreateProcessParameterRequest` | Request | Request body for creating a process parameter |
| `CreateProcessRequest` | Request | Request body for creating a process |
| `CreateSettingRequest` | Request | Request to create a new system setting |
| `CreateShiftRequest` | Request | Request body for creating a new shift |
| `CreateTemplateVersionRequest` | Request | Request body for freezing a new template version |
| `CreateUserRequest` | Request | Request payload for creating a new user |
| `LoginRequest` | Request | Login request payload |
| `ModuleFilterRequest` | Request | Request to filter modules |
| `ModuleResponse` | Response | Module response payload |
| `ModuleTypeFilterRequest` | Request | Request to filter module types |
| `ModuleTypeResponse` | Response | Module type response payload |
| `ModuleTypeSummaryResponse` | Response | Lightweight module type summary |
| `ParameterFilterRequest` | Request | Request to filter parameters |
| `ParameterResponse` | Response | Module parameter response payload |
| `ParameterSummaryResponse` | Response | Lightweight module parameter summary |
| `ProcessFilterRequest` | Request | Request to filter processes |
| `ProcessParameterField` | Response | A frozen field definition resolved from a process parameter |
| `ProcessParameterResponse` | Response | Process parameter response payload |
| `ProcessResponse` | Response | Process response payload |
| `RecordProcessRequest` | Request | Request to save recorded values for one process step |
| `RecordProcessResponse` | Response | Saved process-step snapshot with frozen values |
| `RecordedProcessItem` | Response | A recorded process step within a report session |
| `RecordedValueItem` | Response | A single frozen recorded value within a process step |
| `RecordedValueRequest` | Request | Single observed value for a process parameter |
| `RefreshTokenRequest` | Request | Refresh token request payload |
| `ReportProcessStep` | Response | A process step definition in a report session |
| `ReportSessionResponse` | Response | A report session with its recorded processes |
| `StartReportRequest` | Request | Request to start a report session (moduleId, optional shiftId/lineId) |
| `TemplateVersionResponse` | Response | Template version response payload |
| `UnifiedSearchRequest` | Request | Unified search query with entity type and filters |
| `UpdateAttachmentRequest` | Model | Request to update an existing attachment's metadata |
| `UpdateIntegrationRequest` | Request | Request to update an existing integration |
| `UpdateLineRequest` | Request | Request body for updating an existing production line |
| `UpdateModuleRequest` | Request | Request body for updating a module |
| `UpdateModuleTypeRequest` | Request | Request body for updating a module type |
| `UpdateParameterRequest` | Request | Request body for updating a module parameter |
| `UpdateProcessParameterRequest` | Request | Request body for updating a process parameter |
| `UpdateProcessRequest` | Request | Request body for updating a process |
| `UpdateSettingRequest` | Request | Request to update an existing system setting |
| `UpdateShiftRequest` | Request | Request body for updating an existing shift |
| `UpdateStatusRequest` | Request | Request payload for updating a user's active status |
| `UpdateUserRequest` | Request | Request payload for updating an existing user |

## 21. Appendix B — Status Codes

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
