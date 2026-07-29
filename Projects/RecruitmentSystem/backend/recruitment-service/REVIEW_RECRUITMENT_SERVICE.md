# Recruitment Service Architecture Review

**Scope:** `backend/recruitment-service` as present in the working tree on 2026-07-29. This is a source review only; no application code was changed. `mvn test` passed (one Spring-context test, zero behavioural tests). The working tree already contained uncommitted service changes before this review.

## 1. Architecture Review

The module uses a conventional Spring Boot layered layout: `controller`, `service`/`service.impl`, `repository`, `entity`, feature-grouped `dto`, `mapper`, `common`, `config`, `exception`, and `security`. The implemented job-category path follows the intended Controller -> Service -> Repository pattern and uses request/response DTOs plus MapStruct.

The architectural issue is completeness rather than the package names: there are seven entities and seven repositories, but only one controller/service pair. Six feature areas are therefore data-model scaffolding, not usable modules. There is no integration/client package for company-service ownership validation, no event/outbox integration despite the microservice context, no cache/messaging/object-storage usage, and no health controller although the security allow-list advertises `/api/v1/health`.

### Domain model

| Entity | Responsibility | Relationships |
|---|---|---|
| `JobCategory` | Hierarchical job taxonomy | Self `ManyToOne parent`; self `OneToMany children` |
| `Skill` | Reusable skill catalogue | Referenced by `JobSkill` |
| `Benefit` | Reusable benefit catalogue | Referenced by `JobBenefit` |
| `Job` | Job posting owned by `companyId` | Lazy `ManyToOne` category; referenced by the three job-detail entities |
| `JobSkill` | Job-to-skill association, level, required flag | Lazy `ManyToOne` job and skill |
| `JobBenefit` | Job-to-benefit association | Lazy `ManyToOne` job and benefit |
| `JobLocation` | A job's location | Lazy `ManyToOne` job |

No bidirectional job collections exist, so the current model avoids JSON circular-reference risk. Category's self-reference is not exposed directly in `JobCategoryResponse`, which is also safe. Lazy relationship fields mapped into response DTOs will cause N+1 queries once list endpoints for jobs or association entities are added unless those queries fetch/project the required relations.

## 2. Completed Modules

- **Application/data foundation — substantially complete:** Spring Boot 3.5.4, Java 21, JPA, PostgreSQL, Flyway, MapStruct, Swagger, auditing, and a single initial migration are configured.
- **Job category — partially complete:** create, update, delete, get, paginated list, and name search exist; duplicate name/slug checks and parent existence checks exist. It still has correctness, API, security, and test gaps listed below.
- **Cross-cutting — partial:** JWT parsing/filtering, method-security enablement, OpenAPI metadata, response wrappers, and exception advice exist.
- **DTO/mapper/repository scaffolding — complete only as structure:** classes/interfaces exist for jobs, skills, benefits, job skills, job benefits, and locations; no application services or HTTP endpoints use them.

## 3. Remaining Modules

1. Implement vertical slices for `Job`, `Skill`, `Benefit`, `JobSkill`, `JobBenefit`, and `JobLocation`: services, controllers, ownership checks, authorization, validation, error codes, OpenAPI operations, and tests.
2. Define job lifecycle operations (draft, publish, close/expire, deactivate) and enforce legal state transitions, dates, salary range, category validity, and company ownership.
3. Implement catalog/list/search APIs with bounded pagination, sort allow-lists, active filtering, and efficient fetch/projection queries.
4. Add company-service integration or a documented asynchronous ownership model for `companyId`; the database cannot enforce this cross-service relationship.
5. Add migrations for composite uniqueness and data-integrity constraints, indexes, and an outbox/events contract if other services consume job publication changes.
6. Add automated unit, web, repository, migration, security, and integration tests. The sole current test only starts the context.
7. Add operational configuration: environment-specific secrets, CORS policy, production logging, health readiness/liveness, metrics/tracing, and deployment configuration.

## 4. Technical Debt

### TD-01 — Incomplete vertical slices

- **Severity:** High
- **Location:** `src/main/java/com/recruitment/recruitmentservice/{controller,service}`
- **Explanation:** Only `JobCategoryController` and `JobCategoryService`/`Impl` exist, while the other six domains expose only persistence and mapping scaffolding.
- **Recommendation:** Deliver each domain as a complete vertical slice with service-level rules and tests before exposing it as an API.

### TD-02 — Silent MapStruct update semantics are inconsistent

- **Severity:** Medium
- **Location:** `src/main/java/com/recruitment/recruitmentservice/mapper/{SkillMapper,BenefitMapper,JobCategoryMapper,JobSkillMapper,JobBenefitMapper,JobLocationMapper}.java`
- **Explanation:** Only `JobMapper.updateEntity` uses `NullValuePropertyMappingStrategy.IGNORE`; the other update mappers overwrite nullable fields with `null`. Their current DTO validation often masks this, but future partial-update use will be unsafe and inconsistent.
- **Recommendation:** Choose PUT replacement versus PATCH semantics explicitly and apply a shared null strategy consistently.

### TD-03 — Global response/error contracts diverge

- **Severity:** Medium
- **Location:** `src/main/java/com/recruitment/recruitmentservice/common/ApiResponse.java`; `exception/{ErrorResponse,GlobalExceptionHandler}.java`
- **Explanation:** Success and JWT failures use `ApiResponse`, but controller exceptions use a structurally different `ErrorResponse`; `ErrorCode.code` is not returned by the exception handler.
- **Recommendation:** Publish one versioned error schema and have every failure path include the stable application code, request path, timestamp, and field errors.

### TD-04 — Production diagnostics are enabled in the default configuration

- **Severity:** Medium
- **Location:** `src/main/resources/application.yml`
- **Explanation:** `show-sql: true` and Hibernate bind logging at `TRACE` can expose parameter values and create high log volume.
- **Recommendation:** Restrict SQL/bind logs to local development profiles; keep production defaults at INFO/WARN and redact sensitive values.

## 5. Bug List

### BUG-01 — Category ancestry cycles beyond self-parenting are accepted

- **Severity:** High
- **Location:** `src/main/java/com/recruitment/recruitmentservice/service/impl/JobCategoryServiceImpl.java:70-85`
- **Explanation:** Update rejects `id == parentId`, but allows moving a category under any descendant. That persists a cycle in `job_categories.parent_id`, which breaks hierarchy traversal and future tree APIs.
- **Recommendation:** Walk the proposed parent's ancestry (or enforce with a database/recursive-query guard) and reject any parent whose ancestry contains the category.

### BUG-02 — Category slug uniqueness is not checked on update

- **Severity:** High
- **Location:** `src/main/java/com/recruitment/recruitmentservice/service/impl/JobCategoryServiceImpl.java:60-68`; `mapper/JobCategoryMapper.java:25-32`
- **Explanation:** The update DTO omits `slug`, so the current endpoint cannot intentionally change it. More importantly, no `existsBySlugAndIdNot` safeguard exists for the update flow; introducing slug into the request later would defer conflict detection to a database exception.
- **Recommendation:** Decide whether slugs are immutable. If mutable, add the field, validation, `existsBySlugAndIdNot`, and a conflict error; if immutable, document it and ensure mapper policy makes that explicit.

### BUG-03 — Business exception HTTP statuses and codes are discarded

- **Severity:** High
- **Location:** `src/main/java/com/recruitment/recruitmentservice/exception/GlobalExceptionHandler.java:36-49`; `exception/ErrorCode.java`
- **Explanation:** Every `BusinessException` returns HTTP 400 and no application code, even though `ErrorCode` carries `NOT_FOUND`, `UNAUTHORIZED`, `FORBIDDEN`, etc. This makes API behaviour contradict the domain error model.
- **Recommendation:** Respond with `ex.getErrorCode().getStatus()` and include `getCode()` in the published error body.

### BUG-04 — Category deletion can violate the job foreign key

- **Severity:** High
- **Location:** `src/main/java/com/recruitment/recruitmentservice/service/impl/JobCategoryServiceImpl.java:94-104`; `src/main/resources/db/migration/V1__create_recruitment_tables.sql`
- **Explanation:** Delete checks only child categories. A category referenced by `jobs.category_id` is deleted without a pre-check; PostgreSQL rejects it with an unhandled `DataIntegrityViolationException`, resulting in a 500 response.
- **Recommendation:** Add `existsByCategory_Id` to `JobRepository`, return a defined business conflict, and decide whether jobs may be reassigned or categories soft-deactivated.

### BUG-05 — Paging/sorting input can cause 500s or abusive queries

- **Severity:** Medium
- **Location:** `src/main/java/com/recruitment/recruitmentservice/controller/JobCategoryController.java:85-145`
- **Explanation:** `page` and `size` lack bounds and `sortBy` is passed directly to Spring Data. Negative values, a non-existent property, or excessive page sizes cause exceptions/expensive requests; invalid `direction` silently becomes ascending.
- **Recommendation:** Use validated query parameters, cap page size, and allow-list sortable properties/directions in a reusable pagination resolver.

### BUG-06 — JWT roles do not become Spring authorities

- **Severity:** High
- **Location:** `src/main/java/com/recruitment/recruitmentservice/security/JwtAuthenticationToken.java:10-22`; `security/JwtAuthenticationFilter.java:50-70`
- **Explanation:** The token is constructed with `super(null)`, so `getAuthorities()` is empty. `@EnableMethodSecurity` is present, but `hasRole`/`hasAuthority` checks cannot authorize role claims.
- **Recommendation:** Convert validated role claims into `GrantedAuthority` instances with a documented naming convention (for example `ROLE_*`) and pass them to the authentication token.

### BUG-07 — Invalid bearer tokens are silently treated as anonymous

- **Severity:** Medium
- **Location:** `src/main/java/com/recruitment/recruitmentservice/security/JwtAuthenticationFilter.java:41-82`
- **Explanation:** Validation failures and all parsing exceptions clear the context then continue. Protected endpoints eventually return 401, but public endpoints cannot distinguish a malformed/expired supplied credential from an anonymous request, and failures are not observable.
- **Recommendation:** Define the desired policy; normally reject malformed supplied bearer tokens with the configured entry point and log/metric only safe failure metadata.

### BUG-08 — Security configuration permits every actuator endpoint

- **Severity:** High
- **Location:** `src/main/java/com/recruitment/recruitmentservice/config/SecurityConfig.java:35-49`
- **Explanation:** `/actuator/**` is public. Although only health/info are configured today, future exposure becomes public by default and health details are configured as `always`.
- **Recommendation:** Permit only the required liveness/readiness endpoint(s), protect all other actuator routes, and hide details outside authorized operations contexts.

### BUG-09 — Category response mapping can create N+1 lookups

- **Severity:** Medium
- **Location:** `src/main/java/com/recruitment/recruitmentservice/mapper/JobCategoryMapper.java:14-15`; `service/impl/JobCategoryServiceImpl.java:122-145`
- **Explanation:** Every list/search response maps `parent.id` and `parent.name` from a lazy relationship. Distinct parent categories can trigger additional selects per page.
- **Recommendation:** Use an entity graph/fetch join or a DTO projection tailored to the list response and verify query counts in an integration test.

### BUG-10 — Schema permits duplicate association rows and multiple primary locations

- **Severity:** Medium
- **Location:** `src/main/resources/db/migration/V1__create_recruitment_tables.sql`
- **Explanation:** `job_skills` has no unique `(job_id, skill_id)`, `job_benefits` has no unique `(job_id, benefit_id)`, and `job_locations` has no rule limiting `primary_location = true` to one row per job.
- **Recommendation:** Add unique composite constraints and a PostgreSQL partial unique index for primary location, then add matching service validations.

### BUG-11 — Unhandled database constraint conflicts become 500 errors

- **Severity:** Medium
- **Location:** `src/main/java/com/recruitment/recruitmentservice/exception/GlobalExceptionHandler.java:111-126`
- **Explanation:** Concurrent creates can pass the service `exists` check but lose the database unique constraint race. `DataIntegrityViolationException` falls into the generic handler and exposes the exception message.
- **Recommendation:** Map integrity violations to a stable conflict error (409 where appropriate), log the server exception internally, and never return raw exception messages.

### BUG-12 — Credentials and an operational database are used by the default test context

- **Severity:** High
- **Location:** `src/main/resources/application.yml`; `src/test/java/com/recruitment/recruitmentservice/RecruitmentServiceApplicationTests.java`
- **Explanation:** The test starts against `localhost:5432/recruitment_db` and the default configuration contains a concrete password. Tests are not isolated and can use/alter a developer database through Flyway.
- **Recommendation:** Remove the password from source, require environment/secret injection, and use a dedicated test profile with Testcontainers or an isolated database.

## 6. Refactoring Suggestions

1. Organize by feature or retain current layers but require a complete feature checklist; the present broad layers make incomplete vertical slices easy to miss.
2. Extract a reusable pagination/sort policy instead of duplicating `PageRequest` construction in each future controller.
3. Make DTOs immutable records (or at least constructor-based) once API compatibility permits; this reduces accidental request mutation and makes defaults explicit.
4. Replace `ReportingPolicy.IGNORE` with `WARN`/`ERROR` where feasible and explicitly ignore intentional fields. Silent unmapped fields are dangerous as DTOs evolve.
5. Use `@ControllerAdvice` to centralize the response envelope, then remove `ErrorResponse` or make it the single envelope.
6. Prefer service-specific query projections/entity graphs for all list APIs to prevent lazy-load surprises with `open-in-view: false`.
7. Add a dedicated authorization component that evaluates `companyId` ownership and roles; do not scatter `SecurityUtils` checks through services.

## 7. Production Readiness

| Layer | Score / 10 | Evidence |
|---|---:|---|
| Entity/domain | 5 | Clear UUID/auditing and lazy relations; missing association constraints and lifecycle invariants |
| Repository | 3 | Interfaces exist; most have only inherited methods and no production query plan |
| DTO | 5 | Request/response separation and basic validation; incomplete cross-field validation and mutable models |
| Mapper | 5 | Basic mappings present; silent unmapped targets and inconsistent update null policy |
| Service | 2 | Only category service exists; it lacks cycle/job-reference protection |
| Controller | 2 | Only one controller, no status semantics, unsafe pagination/sort validation |
| Exception handling | 3 | Advice exists; error-code status/code contract is not honoured and raw exception messages leak |
| Config/security | 3 | Stateless JWT foundation exists; roles are not authorities, actuator exposure is broad, default secrets/logging are unsafe |
| Swagger | 4 | OpenAPI bean and category annotations exist; most advertised features have no endpoints |
| Overall architecture | 3 | Viable foundation, but not a complete or production-safe recruitment service |

**Operational verdict:** not production-ready. Build health is positive (`mvn test` succeeds), but functional coverage is effectively one context-load test and essential job-management/API/security controls are missing.

## 8. Recommended Development Order

| Priority | Remaining work | Estimated effort |
|---:|---|---:|
| P0 | Remove source password; create isolated test profile; restrict actuator and production logging | 1–2 days |
| P0 | Correct exception status/code contract, JWT authorities, ownership authorization policy | 2–4 days |
| P0 | Fix category cycle/delete/pagination defects and write focused tests | 2–3 days |
| P1 | Implement Job lifecycle, category/company validation, search/pagination, and tests | 6–10 days |
| P1 | Implement Skills and Benefits catalogue APIs plus duplicate/active semantics | 3–5 days |
| P1 | Implement job skills, benefits, locations with database constraints and query plans | 4–6 days |
| P1 | Add Flyway V2+ integrity/index migrations and test migration from a clean database | 2–3 days |
| P2 | Add company integration/event or outbox contract, observability, API contract tests, load/security testing | 5–10 days |

Estimates are implementation effort for one engineer and exclude product decisions, review, and external-service coordination.

## 9. Estimated Completion %

**Estimated completion: 25%.** The persistence model and cross-cutting foundation are present, and one of seven domain areas has an API/service path. The central Job feature, all association and catalogue APIs, authorization rules, integration, reliability controls, and meaningful automated tests remain.

## 10. Final Verdict

The project is a credible Spring Boot foundation and compiles/tests successfully, but it is an early implementation rather than a complete recruitment microservice. Do not release it beyond local development until the P0 items are resolved, the Job feature and dependent APIs are implemented, database invariants are added, and realistic automated test coverage validates the supported workflows.
