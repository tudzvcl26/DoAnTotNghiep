# Recruitment Service Bug Fix Report

**Scope:** All BUG-01 through BUG-12 items in `REVIEW_RECRUITMENT_SERVICE.md`.

Every code/database change below was followed by `mvn test -q` with **BUILD SUCCESS**. The final test execution used the isolated `test` profile and H2; it did not connect to the previous localhost PostgreSQL database.

## BUG-01

**Root Cause:** Updating a category only rejected a category as its own parent. It did not inspect the proposed parent's ancestors.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/service/impl/JobCategoryServiceImpl.java`

**Reason:** A descendant could be assigned as a parent and create a circular category hierarchy.

**Implementation:** Added `validateParentCategory`, which walks from the proposed parent to the root and throws `INVALID_PARENT_CATEGORY` if the category being updated is encountered.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-02

**Root Cause:** The update DTO intentionally had no slug field, but that immutability was undocumented.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/dto/category/UpdateJobCategoryRequest.java`

**Reason:** Changing the API/DTO to make slugs mutable would be an unnecessary compatibility change. Existing mapper behaviour already keeps slug unchanged because it is not mapped from the update request.

**Implementation:** Documented `slug` as immutable after creation in the update DTO. No repository or service uniqueness check is needed because the value cannot be changed by this API.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-03

**Root Cause:** The business-exception handler hard-coded HTTP 400 and returned `ErrorResponse`, discarding `ErrorCode.status` and `ErrorCode.code`.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/exception/GlobalExceptionHandler.java`

**Reason:** Clients need the correct HTTP status and stable application error code in the existing API response contract.

**Implementation:** `BusinessException` now returns `ResponseEntity<ApiResponse<Void>>` with `errorCode.status`, `errorCode.code`, and the controlled `errorCode.message`.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-04

**Root Cause:** Category deletion only checked child categories, not jobs holding the `category_id` foreign key.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/repository/JobRepository.java`
- `src/main/java/com/recruitment/recruitmentservice/service/impl/JobCategoryServiceImpl.java`
- `src/main/java/com/recruitment/recruitmentservice/exception/ErrorCode.java`

**Reason:** Deleting a referenced category otherwise reaches PostgreSQL and fails as an unhandled integrity exception.

**Implementation:** Added `existsByCategory_Id`, checked it before deletion, and added `JOB_CATEGORY_IN_USE` with HTTP 409.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-05

**Root Cause:** Controller paging parameters had no bounds and the supplied sort property/direction were used without validation.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/controller/JobCategoryController.java`
- `src/main/java/com/recruitment/recruitmentservice/exception/ErrorCode.java`

**Reason:** Invalid page values, oversized requests, or unknown sort fields could produce framework exceptions or expensive queries.

**Implementation:** Centralized pageable creation, enforces `page >= 0`, `1 <= size <= 100`, permits only `name`, `displayOrder`, `createdAt`, and `updatedAt`, and accepts only `asc`/`desc`. Invalid input returns `COMMON_002` through the business-exception handler.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-06

**Root Cause:** `JwtAuthenticationToken` was built with no authorities, so role claims could not satisfy Spring method-security checks.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/security/JwtAuthenticationToken.java`
- `src/main/java/com/recruitment/recruitmentservice/security/JwtAuthenticationFilter.java`

**Reason:** `@EnableMethodSecurity` only works when the authentication holds `GrantedAuthority` instances.

**Implementation:** Role claims are converted to `SimpleGrantedAuthority`, normalizing unprefixed roles to `ROLE_*`, and supplied to the authentication token.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-07

**Root Cause:** Failed token validation cleared the context and continued the filter chain, treating a malformed bearer token as an anonymous request.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/security/JwtAuthenticationFilter.java`

**Reason:** A supplied invalid credential must receive the service's standard authentication response.

**Implementation:** The filter now clears context, calls `JwtAuthenticationEntryPoint`, and returns for invalid-token results and parsing exceptions.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-08

**Root Cause:** The security configuration publicly permitted `/actuator/**`, while health details were always exposed.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/config/SecurityConfig.java`
- `src/main/resources/application.yml`

**Reason:** Future actuator endpoint exposure must not automatically become public.

**Implementation:** Only `/actuator/health/**` is public; every other actuator request remains authenticated. Endpoint exposure is limited to `health` and health details are hidden.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-09

**Root Cause:** Category list/search mapping reads lazy `parent` fields for each result.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/repository/JobCategoryRepository.java`

**Reason:** This can issue extra parent queries while mapping a page of category DTOs.

**Implementation:** Added `@EntityGraph(attributePaths = "parent")` to both paged category queries used by the service.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-10

**Root Cause:** The initial schema did not enforce uniqueness of job-skill/job-benefit pairs or one primary location per job.

**Files modified:**

- `src/main/resources/db/migration/V2__add_job_association_constraints.sql`

**Reason:** These are data-integrity rules that must be guaranteed at the database layer.

**Implementation:** Added unique constraints on `(job_id, skill_id)` and `(job_id, benefit_id)`, plus a PostgreSQL partial unique index for `job_locations(job_id)` where `primary_location = TRUE`.

**Verification:** `mvn test -q` — BUILD SUCCESS; Flyway validated and applied V2 to the existing development schema before test isolation was introduced.

## BUG-11

**Root Cause:** `DataIntegrityViolationException` was handled by the generic error handler, returning an HTTP 500 and the raw exception message.

**Files modified:**

- `src/main/java/com/recruitment/recruitmentservice/exception/ErrorCode.java`
- `src/main/java/com/recruitment/recruitmentservice/exception/GlobalExceptionHandler.java`

**Reason:** Unique/foreign-key constraint races are client-visible conflicts, not server errors; SQL details must remain private.

**Implementation:** Added `DATA_INTEGRITY_VIOLATION` (`COMMON_003`, HTTP 409) and a dedicated `DataIntegrityViolationException` handler using `ApiResponse`. The generic handler now returns the controlled `COMMON_500` message instead of `ex.getMessage()`.

**Verification:** `mvn test -q` — BUILD SUCCESS.

## BUG-12

**Root Cause:** The context test used default configuration, which targeted localhost PostgreSQL and included a checked-in database password.

**Files modified:**

- `pom.xml`
- `src/main/resources/application.yml`
- `src/test/java/com/recruitment/recruitmentservice/RecruitmentServiceApplicationTests.java`
- `src/test/resources/application-test.yml`

**Reason:** Tests must not touch an operational database, and database credentials must not be committed in defaults.

**Implementation:** Added test-scoped H2, activated the `test` profile, configured an isolated H2 in-memory datasource with `create-drop`, disabled Flyway for that H2 context, supplied a test JWT secret, and replaced the main datasource password with required `${DB_PASSWORD}` injection.

**Verification:** `mvn test -q` — BUILD SUCCESS. Logs identify the active `test` profile and H2 JDBC URL.

## Fixed Bugs

All twelve reported bugs are fixed in source/configuration/migrations. BUG-04 depends on BUG-03 because its new `BusinessException` only becomes a correct HTTP 409 after the global handler honors `ErrorCode.status`. BUG-10 supports BUG-11 by ensuring concurrent or bypassed writes still receive a safe conflict response.

## Remaining Issues

- Only the JobCategory API/service is implemented; Jobs, Skills, Benefits, JobSkills, JobBenefits, and JobLocations still lack controllers and service implementations.
- There are no focused unit, repository, MVC, JWT, or migration integration tests for the newly fixed behaviour. The existing test verifies context boot only.
- Cross-service company ownership validation, messaging/outbox, monitoring/tracing, and environment-specific deployment configuration remain outside the reviewed bug list.
- The default application configuration still enables SQL/bind logging; this technical-debt item was not changed because it was not a numbered bug in the requested scope.

## Architecture Impact

No packages, API URLs, DTO names, or frameworks changed. The changes add small repository/security/error-handling extensions, one Flyway migration, and an isolated test profile. Category slugs remain immutable to preserve the existing update API.

## Risk

- **Low:** API behaviour is preserved except intentional improved error HTTP codes and rejection of invalid pagination/JWT input.
- **Medium:** Applying V2 to a database containing existing duplicate associations or multiple primary locations will fail until that data is cleaned. This is intentional to avoid silently retaining invalid data.
- **Medium:** H2 tests do not execute PostgreSQL-specific partial-index SQL because Flyway is disabled in the test profile; run PostgreSQL migration tests in CI before production release.

## Production Readiness Score

**5/10.** The reported correctness, security, error-contract, schema-integrity, and test-isolation bugs are resolved. Production readiness remains constrained by the incomplete job-management modules, limited behavioural test coverage, and unresolved operational/integration work.
