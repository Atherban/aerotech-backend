# CED Operations — Production Cleanup Report

> Date: 2026-08-07 · Scope: remove dead code, unused dependencies, and obsolete
> artifacts from the module-driven report engine. No new features, no
> architecture change, no refactor of working code, no endpoint removed.
>
> Pre-work: `CLEANUP_REPORT.md` (analysis inventory). All removals verified
> against Spring DI, JPA/Hibernate, Jackson, validation, Swagger, tests, Flyway,
> and startup wiring before being deleted.

## Summary

The repository is already production-tight following the Phase 5 legacy-ReportType
removal. Cross-reference analysis (221 main + 5 test files) found **no** orphaned
classes, interfaces, services, repositories, controllers, DTOs, entities, configs,
beans, mappers, validators, exceptions, constants, endpoints, or unused Flyway
migrations. This cleanup removes the remaining dead dependencies, two unused enum
values, seven unused imports, and macOS artifact files, then re-registers the
OpenAPI snapshot.

## Files Deleted

- 8 tracked macOS `.DS_Store` files across repo root and `src/` tree.

## Classes Deleted

- None. Every compiled class is reachable or Spring-wired.

## Endpoints Removed

- **None.** All 118 operations (87 path templates, 18 controllers) remain
  documented and reachable; no dead/duplicate/undocumented routes existed.

## Dependencies Removed (`pom.xml`)

| Dependency | Why |
|---|---|
| `org.mapstruct:mapstruct` + `mapstruct-processor` annotation processor + `mapstruct.version` property | No `@Mapper` / `org.mapstruct` usage anywhere |
| `spring-boot-configuration-processor` | No `@ConfigurationProperties` present |
| `spring-security-test` | No usage in tests |

## Unused Imports Removed

- `JwtAuthenticationFilter`: `org.jspecify.annotations.NonNull`
- `IntegrationMapper`: `java.util.Collections`, `java.util.HashMap`
- `IntegrationConnector`: `org.springframework.http.HttpStatus`
- `GlobalExceptionHandler`: `java.util.stream.Collectors`
- `TemplateVersionService`: `master.module.enums.ProcessStatus`
- `GenericReportEngineServiceTest`: `report.engine.dto.ReportProcessStep`

## Dead Code Removed

- Unused enum constant `IntegrationType.ERP` (0 references incl. seeds/tests).
- Unused enum constant `ReportSessionStatus.CANCELLED` (0 references).
- `@Schema` descriptions updated accordingly.

## Configuration Simplified

- `pom.xml`: removed dead dependencies/property/annotation processor.
- `.gitignore`: added `.DS_Store`.
- No bean/config class removed — all `@Configuration`/`@Bean`/`@EnableCaching`/
  `@EnableJpaAuditing` and exception handlers remain wired.

## Packages Removed

- None. No empty or obsolete packages identified.

## Remaining Technical Debt (not in scope — working code)

- Deprecation warnings: `ApplicationConfig`
  (`AuthenticationConfiguration.getAuthenticationManager`) and `SpecificationBuilder`
  (`Specification.where`) — functional, left untouched.
- `CedOpsBackendApplicationTests.contextLoads` requires Postgres and fails in this
  environment (no local DB); pre-existing, not a cleanup regression.
- `spring-boot-devtools` retained (active runtime dev facility).

## Build Result

- `mvn clean test` — compiles cleanly, 221 main sources under Java 21 target.
- No unresolved symbols; no reference to removed dependencies/enums.

## Test Result

- **24 unit tests pass** (Engine 10, TemplateVersion 5, UnifiedSearchQueryBuilder 5,
  ModuleDomain 4).
- `contextLoads` — 1 error, solely `Connection to localhost:5432 refused` (no DB in
  environment). No missing beans / broken injections.

## Swagger Result

- `api-docs.json` regenerated: **87 paths / 118 operations / 89 schemas**, all
  `$ref`s resolve. Enum values `ERP` and `CANCELLED` no longer appear in the spec.

## Considered but retained (by design, not dead)

- All 12 Flyway migrations (`V1`–`V12`): a frozen sequence; `V4`/`V6` feed `V12`
  and removing any risks checksum/apply breakage for existing databases.
- `ParameterService` (internal resolver) vs `ParameterCrudService` (API CRUD): both
  referenced with distinct roles.
- Analytics `chemical-consumption`/`process-monitoring` endpoints: reachable, engine-
  backed, documented — retained.

## Conclusion

Repository contains only production-ready code. No obsolete or unreachable
implementation, dependency, endpoint, migration, or artifact remains. Stopped
after cleanup; Production Hardening and new-feature development intentionally NOT
started.