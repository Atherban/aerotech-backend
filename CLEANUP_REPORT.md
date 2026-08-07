# CED Operations — Codebase Cleanup Report (Analysis Phase)

> Date: 2026-08-07 · Scope: production cleanup of the module-driven report
> engine codebase. No new features, no architecture changes, no refactor of
> working code.
>
> Method: static cross-reference analysis over `src/main/java` (221 files) and
> `src/test/java` (5 files) using token/reference scans, plus inspection of
> `pom.xml`, resources, and Flyway migrations. Baseline: `mvn clean compile`
> passes; 24 unit tests pass; `CedOpsBackendApplicationTests.contextLoads`
> fails only because Postgres is unavailable in this environment.

## Categorized findings

| Category | Finding | Disposition |
|---|---|---|
| Unused Dependencies | `org.mapstruct:mapstruct` + `mapstruct-processor` (no `@Mapper` anywhere) | Remove |
| Unused Dependencies | `spring-boot-configuration-processor` (no `@ConfigurationProperties`) | Remove |
| Unused Dependencies | `spring-security-test` (no usage in tests) | Remove |
| Unused Enums (constants) | `IntegrationType.ERP` — never referenced | Remove constant |
| Unused Enums (constants) | `ReportSessionStatus.CANCELLED` — never referenced | Remove constant |
| Unused Imports | `JwtAuthenticationFilter`: `org.jspecify.annotations.NonNull` | Remove |
| Unused Imports | `IntegrationMapper`: `java.util.Collections`, `java.util.HashMap` | Remove |
| Unused Imports | `IntegrationConnector`: `org.springframework.http.HttpStatus` | Remove |
| Unused Imports | `GlobalExceptionHandler`: `java.util.stream.Collectors` | Remove |
| Unused Imports | `TemplateVersionService`: `master.module.enums.ProcessStatus` | Remove |
| Unused Imports | `GenericReportEngineServiceTest`: `ReportProcessStep` | Remove |
| Unused Resources | 8 tracked `.DS_Store` files (macOS artifacts) | Delete + gitignore |

## Categories audited and found clean (verified, nothing to remove)

- **Unused Classes / Interfaces / Services / Repositories / Controllers /
  DTOs / Entities / Beans / Configs / Validators / Mappers / Utilities /
  Exceptions / Constants**: none. Every non-Spring-scanned class is referenced
  by name at least once; all classes discovered only via annotation scanning
  (controllers, `@Configuration`, `@Component`, `@Service`, `@Repository`,
  `@RestControllerAdvice`, exception handlers, `@Bean`/`@PostConstruct`/
  `@EventListener`/JPA lifecycle hooks) are wired by Spring. All 21 JPA
  repositories are injected. All 18 controllers map 118 live operations.
- **Duplicate Implementations**: `ParameterService` (internal resolver) and
  `ParameterCrudService` (API CRUD) are both referenced and serve distinct
  roles — retained.
- **Dead Endpoints / Dead API Routes**: all 118 operations are reachable and
  documented; none shadowed, commented, or belonging to removed architecture.
- **Unused Methods**: all service helpers are called (verified same-file call
  sites); no orphaned public/private methods.
- **Unused DTO fields / entity columns**: all Lombok-backed and serialized;
  none orphaned.
- **Unused Packages / empty folders / backup or temp files**: none.
- **Unused Flyway migrations**: all 12 (`V1`–`V12`) are a frozen sequence.
  `V4`/`V6` created the legacy schema later dropped by `V12`; removing any
  migration would break Flyway checksumming for existing databases. Retained
  by design (historical chain, still required by migrations).
- **TODO / FIXME comments**: none.
- **Commented-out code**: none.
- **Deprecated API use (working, not dead)**: `ApplicationConfig`
  (`AuthenticationConfiguration.getAuthenticationManager`) and
  `SpecificationBuilder` (`Specification.where`) trigger deprecation warnings —
  functional, left untouched per scope; logged as technical debt.

## Verification notes per removal

- MapStruct: `rg "@Mapper|org.mapstruct"` → 0 hits in source or tests.
- Configuration processor: `rg "@ConfigurationProperties"` → 0 hits.
- Security test: `rg "spring-security-test|WithMockUser"` → 0 hits in tests.
- `ERP` / `CANCELLED`: 0 references outside their declaring enum (incl. seeds,
  tests, resources).
- All 7 unused imports verified by body scan of the affected files.

## No changes made in this phase

This report is the pre-deletion inventory. Deletions and dependency removals
are executed next, followed by compile/test/swagger verification.
