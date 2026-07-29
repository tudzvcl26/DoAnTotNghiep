# Recruitment Service Production-Readiness Audit

Scope: current `backend/recruitment-service` source. No application code was changed. `mvn test -q` passes, but it runs only `contextLoads` against H2 with Flyway disabled; it does not verify PostgreSQL migrations, CRUD, JWT, or authorization behaviour.

# Overall Score

**42/100 — not production-ready.** Core layering and persistence foundations exist, but credential exposure, missing authorization, unsafe JWT logging, weak domain validation, and insufficient testing are release blockers.

# Critical Issues

## CRIT-01 — Production credentials are committed

- **Severity:** Critical
- **File:** `src/main/resources/application.yml`
- **Class:** Configuration
- **Line number:** 9, 39
- **Explanation:** The database password and JWT signing secret are source literals. Repository/build-artifact access enables database access or issuance of accepted tokens.
- **Root cause:** Secrets are development defaults instead of required injected configuration.
- **Recommended fix:** Remove and rotate both secrets. Inject them through an approved secret store/environment and validate them at startup.

## CRIT-02 — Authentication is not authorization

- **Severity:** Critical
- **File:** `src/main/java/com/recruitment/recruitmentservice/config/SecurityConfig.java`
- **Class:** `SecurityConfig`
- **Line number:** 34-49
- **Explanation:** All non-public endpoints require a valid token, but no role/authority rules or tenant ownership checks are enforced. Any authenticated identity can mutate/read any resource.
- **Root cause:** `@EnableMethodSecurity` and JWT authorities are configured but unused by controllers/services.
- **Recommended fix:** Enforce role-to-operation rules and company/job ownership at the controller/service boundary.

## CRIT-03 — JWT parser errors leak through console debug output

- **Severity:** Critical
- **File:** `src/main/java/com/recruitment/recruitmentservice/security/JwtAuthenticationFilter.java`
- **Class:** `JwtAuthenticationFilter`
- **Line number:** 88-95
- **Explanation:** Stack traces and raw exception messages are written to standard output. This bypasses structured logging and can expose security/parser details.
- **Root cause:** Debug code remains in the authentication failure path.
- **Recommended fix:** Remove `printStackTrace`/`System.out`; log a sanitized warning through SLF4J without token or raw untrusted-message content.

# High Priority Issues

## HIGH-01 — Client can reassign a job to another company

- **Severity:** High
- **File:** `src/main/java/com/recruitment/recruitmentservice/dto/job/UpdateJobRequest.java`; `src/main/java/com/recruitment/recruitmentservice/mapper/JobMapper.java`; `src/main/java/com/recruitment/recruitmentservice/service/impl/JobServiceImpl.java`
- **Class:** `UpdateJobRequest`, `JobMapper`, `JobServiceImpl`
- **Line number:** `UpdateJobRequest.java:59-63`; `JobMapper.java:35-48`; `JobServiceImpl.java:81-93`
- **Explanation:** `companyId` is accepted and mapped during update without ownership validation. Combined with CRIT-02, this permits cross-tenant job hijacking.
- **Root cause:** Tenant ownership is modeled as a mutable client field.
- **Recommended fix:** Resolve ownership from a trusted identity/integration or make it immutable after creation.

## HIGH-02 — Soft-deleted jobs remain publicly retrievable

- **Severity:** High
- **File:** `src/main/java/com/recruitment/recruitmentservice/service/impl/JobServiceImpl.java`
- **Class:** `JobServiceImpl`
- **Line number:** 98-112, 115-160
- **Explanation:** Delete only sets `active=false`; get/list/search use unfiltered repository queries. Deactivated jobs remain visible and keep categories in use.
- **Root cause:** Soft-delete policy lacks matching read-query predicates.
- **Recommended fix:** Add explicit active predicates to public reads and authorization-aware administrative reads.

## HIGH-03 — No optimistic locking

- **Severity:** High
- **File:** `src/main/java/com/recruitment/recruitmentservice/entity/BaseEntity.java`
- **Class:** `BaseEntity`
- **Line number:** 22-35
- **Explanation:** Concurrent changes to jobs, categories, skills, and benefits silently overwrite one another.
- **Root cause:** Timestamps are present but no JPA `@Version`/conditional update exists.
- **Recommended fix:** Add a version column in a forward migration and translate optimistic-lock failures to HTTP 409.

## HIGH-04 — Job lifecycle and cross-field invariants are absent

- **Severity:** High
- **File:** `src/main/java/com/recruitment/recruitmentservice/dto/job/{CreateJobRequest,UpdateJobRequest}.java`; `src/main/java/com/recruitment/recruitmentservice/service/impl/JobServiceImpl.java`
- **Class:** Job DTOs, `JobServiceImpl`
- **Line number:** `CreateJobRequest.java:35-53`; `UpdateJobRequest.java:35-53`; `JobServiceImpl.java:35-95`
- **Explanation:** No rule enforces `salaryMin <= salaryMax`, future deadline, active category, valid lifecycle dates, or verified company existence/ownership.
- **Root cause:** Only single-field Bean Validation is applied.
- **Recommended fix:** Add cross-field request validation and service/domain checks before persistence.

## HIGH-05 — Production Flyway migrations have no test coverage

- **Severity:** High
- **File:** `src/test/resources/application-test.yml`; `src/test/java/com/recruitment/recruitmentservice/RecruitmentServiceApplicationTests.java`
- **Class:** Test configuration, `RecruitmentServiceApplicationTests`
- **Line number:** `application-test.yml:9-14`; `RecruitmentServiceApplicationTests.java:7-13`
- **Explanation:** Tests disable Flyway and use H2 DDL generation. PostgreSQL-specific V2 partial-index SQL is never executed; the sole test has no assertions.
- **Root cause:** Context boot was substituted for database/API testing.
- **Recommended fix:** Run Flyway and CRUD/security tests against disposable PostgreSQL in CI.

## HIGH-06 — Referenced skills/benefits have no business-safe delete rule

- **Severity:** High
- **File:** `src/main/java/com/recruitment/recruitmentservice/service/impl/{SkillServiceImpl,BenefitServiceImpl}.java`
- **Class:** `SkillServiceImpl`, `BenefitServiceImpl`
- **Line number:** `SkillServiceImpl.java:65-71`; `BenefitServiceImpl.java:65-71`
- **Explanation:** Deletes do not check job association rows. The database conflict handler returns a generic conflict rather than a stable domain decision.
- **Root cause:** Catalogue lifecycle rules are missing.
- **Recommended fix:** Check references with dedicated error codes, or adopt an explicit inactive/retirement policy.

# Medium Priority Issues

## MED-01 — Skill/Benefit sorting accepts arbitrary mapped properties

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/controller/{SkillController,BenefitController}.java`
- **Class:** `SkillController`, `BenefitController`
- **Line number:** `SkillController.java:172-200`; `BenefitController.java:174-202`
- **Explanation:** `sortBy` is passed to `Sort.by` without an allow-list. Unknown properties raise exceptions; invalid direction silently becomes ascending. Page/size are silently coerced, obscuring bad requests.
- **Root cause:** Pagination logic was duplicated rather than using the validated category policy.
- **Recommended fix:** Apply one allow-listed pagination policy and reject invalid values with a stable error code.

## MED-02 — Error contract is inconsistent and can expose raw messages

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/exception/GlobalExceptionHandler.java`
- **Class:** `GlobalExceptionHandler`
- **Line number:** 21-34, 55-109, 128-141
- **Explanation:** Business/database errors return `ApiResponse`; validation/resource/argument errors return `ErrorResponse`. Constraint and illegal-argument responses expose `ex.getMessage()`.
- **Root cause:** Migration to the shared response envelope is incomplete.
- **Recommended fix:** Use one versioned error response, controlled codes, and safe messages for every handler.

## MED-03 — CORS is enabled without a policy

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/config/SecurityConfig.java`
- **Class:** `SecurityConfig`
- **Line number:** 27-33
- **Explanation:** Spring Security CORS is enabled but no `CorsConfigurationSource`/MVC policy defines origins, methods, headers, or credentials.
- **Root cause:** CORS processing was enabled without configuration.
- **Recommended fix:** Define a least-privilege, environment-configured CORS policy or remove it for gateway-only deployment.

## MED-04 — JWT claims have no issuer/audience/required-claim policy

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/security/{JwtService,JwtAuthenticationFilter}.java`
- **Class:** `JwtService`, `JwtAuthenticationFilter`
- **Line number:** `JwtService.java:33-39,54-85`; `JwtAuthenticationFilter.java:59-82`
- **Explanation:** Signature/expiry parsing is performed, but issuer, audience, required user ID, and allowed role vocabulary are not enforced.
- **Root cause:** Token validation only parses signed claims.
- **Recommended fix:** Validate the auth-service token contract explicitly and externalize expected issuer/audience.

## MED-05 — Nullable update fields can violate non-null persistence columns

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/mapper/{SkillMapper,BenefitMapper,JobCategoryMapper}.java`
- **Class:** `SkillMapper`, `BenefitMapper`, `JobCategoryMapper`
- **Line number:** `SkillMapper.java:21-25`; `BenefitMapper.java:21-25`; `JobCategoryMapper.java:25-32`
- **Explanation:** Default MapStruct update mapping writes nulls. Optional `active`/`displayOrder` fields can become null despite non-null entity/database columns.
- **Root cause:** DTO optionality conflicts with default MapStruct null assignment.
- **Recommended fix:** Require fields for PUT or use an explicit null-value strategy for PATCH semantics.

## MED-06 — Category cascade conflicts with deletion policy

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/entity/JobCategory.java`
- **Class:** `JobCategory`
- **Line number:** 45-50
- **Explanation:** `CascadeType.ALL` includes remove, but service logic prohibits deletion of parents with children. Another remove path could cascade contrary to policy.
- **Root cause:** JPA cascade scope does not match domain deletion rules.
- **Recommended fix:** Remove `REMOVE` cascade unless categories are explicitly an aggregate that must be removed together.

## MED-07 — Core Job constraints are application-only

- **Severity:** Medium
- **File:** `src/main/resources/db/migration/V1__create_recruitment_tables.sql`
- **Class:** Flyway V1
- **Line number:** 64-85
- **Explanation:** Database accepts null category, negative/reversed salary, non-positive quantity, and invalid lifecycle dates if a caller bypasses the API.
- **Root cause:** Structural FKs exist, but key business invariants are absent from schema.
- **Recommended fix:** Add forward migrations for check constraints and make `category_id` non-null if it is mandatory.

# Low Priority Issues

## LOW-01 — Association scaffolding has no owning vertical slice

- **Severity:** Low
- **File:** `src/main/java/com/recruitment/recruitmentservice/{dto,mapper,repository}`
- **Class:** JobSkill, JobBenefit, JobLocation types
- **Line number:** Package-level
- **Explanation:** DTO/mapper/repository types exist without services/controllers, adding unexercised code and schema surface.
- **Root cause:** Partial vertical-slice development.
- **Recommended fix:** Complete each slice with authorization/tests or defer unused code.

## LOW-02 — Public `/api/v1/health` route is not implemented

- **Severity:** Low
- **File:** `src/main/java/com/recruitment/recruitmentservice/config/SecurityConfig.java`
- **Class:** `SecurityConfig`
- **Line number:** 45
- **Explanation:** The route is permitted, but no controller implements it; actual health uses `/actuator/health/**`.
- **Root cause:** Stale allow-list entry.
- **Recommended fix:** Remove it or implement/document the intended endpoint.

# Security Issues

CRIT-01 through CRIT-03 are production blockers. MED-03 and MED-04 also require resolution: CORS behaviour is undefined and the token contract is under-validated. Files: `application.yml:6-10,38-41`, `SecurityConfig.java:27-49`, `JwtAuthenticationFilter.java:45-105`, and `JwtService.java:33-101`.

# Performance Issues

## PERF-01 — Job summaries can trigger N+1 category loading

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/service/impl/JobServiceImpl.java`; `src/main/java/com/recruitment/recruitmentservice/mapper/JobMapper.java`
- **Class:** `JobServiceImpl`, `JobMapper`
- **Line number:** `JobServiceImpl.java:129-159`; `JobMapper.java:55-58`
- **Explanation:** Paged jobs are loaded without fetch/projection while mapping dereferences lazy category fields.
- **Root cause:** Lazy association access in list DTO mapping.
- **Recommended fix:** Use a DTO projection/entity graph and verify PostgreSQL query counts.

## PERF-02 — Case-insensitive contains search lacks a scalable index plan

- **Severity:** Low
- **File:** `src/main/java/com/recruitment/recruitmentservice/repository/JobRepository.java`; `src/main/resources/db/migration/V1__create_recruitment_tables.sql`
- **Class:** `JobRepository`, Flyway V1
- **Line number:** `JobRepository.java:21-24`; `V1__create_recruitment_tables.sql:53-93`
- **Explanation:** `%keyword%` case-insensitive title search cannot use the listed B-tree indexes efficiently at scale.
- **Root cause:** Query and index strategy were not designed together.
- **Recommended fix:** Define search requirements and add an appropriate PostgreSQL search/index strategy.

# Database Issues

HIGH-05 and MED-07 apply. In addition, `job_categories.parent_id` has a foreign key but no index (`V1__create_recruitment_tables.sql:13-20`), which degrades hierarchy checks and FK operations as data grows. Add a forward index migration.

# REST API Issues

## REST-01 — Resource status/body conventions conflict

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/controller/{JobController,SkillController,BenefitController,JobCategoryController}.java`
- **Class:** Resource controllers
- **Line number:** `JobController.java:30-43,62-75`; `SkillController.java:34-48,69-84`; `BenefitController.java:34-48,69-84`; `JobCategoryController.java:44-54,70-83`
- **Explanation:** Job create returns 201 and delete declares 204 while returning JSON; other resources return 200 for create/delete. A 204 response must not have a body.
- **Root cause:** No module-wide REST response convention.
- **Recommended fix:** Standardize create/delete status semantics and their response bodies.

## REST-02 — Invalid UUID/query conversion has no stable handler

- **Severity:** Medium
- **File:** `src/main/java/com/recruitment/recruitmentservice/exception/GlobalExceptionHandler.java`
- **Class:** `GlobalExceptionHandler`
- **Line number:** 79-109
- **Explanation:** `MethodArgumentTypeMismatchException` is not handled, so malformed UUIDs/paging values do not receive a documented consistent error.
- **Root cause:** Web binding/type conversion is omitted from controller advice.
- **Recommended fix:** Add controlled conversion-error responses with application error codes.

# Validation Issues

HIGH-04, MED-01, and MED-05 apply. Additionally, `CreateJobRequest.java:29-57` and `UpdateJobRequest.java:29-57` allow unbounded `TEXT` values and client-controlled `active`; define operation-specific field policies and validation groups.

# Code Quality Issues

No structured service/security logging exists; the sole JWT failure output is raw console debug (`JwtAuthenticationFilter.java:88-95`). Mapper standards are inconsistent: `JobMapper.java:14` bypasses `RecruitmentMapperConfig`, while `RecruitmentMapperConfig.java:6-9` suppresses unmapped-target errors. Use one explicit mapper policy and structured redacted logging.

# Architecture Issues

Layer package names are conventional, but CRIT-02/HIGH-01 show no authorization or tenant boundary, HIGH-03 shows no aggregate concurrency policy, and LOW-01 shows incomplete vertical slices. Keep the current package layout but add narrow authorization/integration components before expanding CRUD.

# Naming Issues

`JobController.delete` (`JobController.java:62-75`) deactivates rather than removes a Job. The API name and declared 204 status imply deletion. Document the soft-delete contract or use explicit lifecycle wording while preserving URL compatibility.

# Swagger Issues

Operations have summaries but no documented response/error schemas, examples, allowable sort values, or per-operation authorization requirements (`controller` operation annotations). `OpenApiConfig.java:27-36` contains mojibake, so rendered API documentation is corrupted. Add response annotations/examples and preserve UTF-8 source encoding.

# Suggested Improvements

1. Rotate hard-coded secrets and externalize configuration.
2. Implement role and company ownership enforcement before exposing mutation APIs.
3. Remove raw JWT debug output and establish redacted structured logging.
4. Add PostgreSQL Flyway, CRUD, JWT, authorization, duplicate, UUID, pagination, and concurrency integration tests.
5. Standardize error envelopes and REST status conventions.
6. Add optimistic locking and core database constraints.

# Production Readiness Score

**42/100.** Do not release until all Critical and High issues are resolved and PostgreSQL-backed behavioural/security testing exists.
