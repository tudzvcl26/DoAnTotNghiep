# Backend Hardening Phase 2 Report

## 1. Executive Summary

Hardening Phase 2 identified 15 actionable findings: the 11 requested audit items (AUD-009 through AUD-019) plus four cross-cutting findings covering soft delete, API error contract, observability, and resilience. Fourteen are fixed and verified. The structural extraction portion of AUD-011 is deliberately deferred because migrating seven independently working JWT stacks into a new shared module would create disproportionate regression risk; their externally visible authentication behavior was standardized instead.

Final verification is green: `mvn clean verify --batch-mode` passed 87 tests, the PostgreSQL integration profile passed 5 additional Testcontainers tests, and the real 36-step runtime E2E passed against PostgreSQL 17, RabbitMQ, Redis, MinIO, and Ollama. No existing migration was edited, no commit or push was performed, and all temporary JVMs/test data were removed.

## 2. Starting State

- Phase 1 HIGH fixes AUD-001 through AUD-007 were present in the working tree and preserved.
- Baseline: 77 tests, clean reactor build, real PostgreSQL/RabbitMQ/Notification/Resume/Application/IDOR/refresh-token/AI regression previously passed.
- The working tree was already intentionally dirty with Phase 1 changes. Those changes were treated as user-owned and were not reset or rolled back.
- `git log -10 --oneline` was inspected; no history-changing operation was performed.

## 3. Findings Before Fix

| Finding | Before | Final status |
|---|---|---|
| AUD-009 | Four synchronous REST clients had no bounded connect/read configuration | FIXED |
| AUD-010 | Downstream failures could collapse into `Optional.empty()`/false 404 | FIXED |
| AUD-011 | Seven services duplicated JWT/filter/security behavior | PARTIAL FIX / STRUCTURAL DEFERRED |
| AUD-012 | Malformed input and unexpected exceptions had inconsistent/unsafe handling | FIXED |
| AUD-013 | User profile asset validation trusted weak client metadata | FIXED |
| AUD-014 | Invalid JWT/refresh-token semantics drifted between 401/403/404 | FIXED |
| AUD-015 | PostgreSQL-specific behavior was primarily exercised through H2 | FIXED |
| AUD-016 | Swagger/Actuator production exposure was too broad/inconsistent | FIXED |
| AUD-017 | `cors(withDefaults())` lacked an explicit configurable policy | FIXED |
| AUD-018 | Application and Recruitment lacked the common health contract | FIXED |
| AUD-019 | Compose used `minio/minio:latest` | FIXED |
| PH2-SD | AI resume deletion physically removed audit/history data | FIXED |
| PH2-ERR | Error and malformed-body semantics drifted across services | FIXED |
| PH2-OBS | Six services lacked a request correlation filter; SQL bind logs were unsafe by default | FIXED |
| PH2-RES | Dependency calls were unbounded and failure classes were indistinguishable | FIXED |

## 4. Changes Made

- Added bounded REST client factories and stable downstream error mapping.
- Added profile asset allowlists, content signatures, server-generated keys, size limits, safe downloads, and ownership regressions.
- Standardized invalid JWT handling, refresh-token semantics, malformed JSON handling, safe 500 responses, CORS, health, and correlation headers.
- Added PostgreSQL 17 Testcontainers migration tests to Auth, User, Application, Notification, and AI.
- Added production Swagger/Actuator restrictions and safe actuator health exposure to all services.
- Added AI resume soft delete through a new V6 migration and active-record repository filters.
- Pinned the MinIO compose image to the exact release reported by the running server.
- Disabled SQL value binding logs by default while retaining explicit environment overrides.
- Preserved Phase 1 outbox, notification, resume versioning, lifecycle, IDOR, admin, and refresh-token fixes.

## 5. AUD-009 Status

- **BEFORE:** Application→User/Job/Company and Recruitment→Company calls could wait without a bounded connect/read policy.
- **FIX:** Added centralized per-service `RestClientFactory` configuration with environment-overridable connect and read timeouts.
- **FILES:** `application-service/client/RestClientFactory.java`, `recruitment-service/client/RestClientFactory.java`, both services' `application.yml`, and the four client implementations.
- **TEST:** Normal response and timeout/connection-failure client tests; real E2E calls remained green.
- **RESULT:** **FIXED**. No unbounded call remains in the audited client set.

## 6. AUD-010 Status

- **BEFORE:** Broad exception handling could represent 400/401/403/5xx/timeout/connection failure as not-found.
- **FIX:** Only downstream 404 maps to absence. 400, 401, 403, 5xx, timeout, and connectivity errors map to stable dependency error codes and appropriate HTTP semantics without internal exception text.
- **FILES:** Both `DownstreamClientSupport.java` implementations, Application/Recruitment `ErrorCode.java`, and four REST clients.
- **TEST:** `ApplicationRestClientsTest` and `CompanyClientResilienceTest` cover the relevant status/failure classes.
- **RESULT:** **FIXED**. Downstream 500 is no longer converted to a false 404.

## 7. AUD-011 Status

- **BEFORE:** JWT parsing/filter/entry-point behavior was duplicated and had observable drift.
- **FIX:** Standardized missing/invalid/tampered JWT as authentication failure (401), retained role/ownership authorization as 403, and aligned health/docs/security route rules. Business ownership stays service-local.
- **FILES:** Security configs and JWT filters in Auth, User, Company, Application, Recruitment, Notification, and AI; new Auth `JwtAuthenticationEntryPoint`.
- **TEST:** Auth malformed/tampered/missing JWT regressions, service authorization suites, and runtime invalid-JWT probes on all seven ports.
- **RESULT:** **DEFERRED (structural extraction only)**. A new shared-security/common-lib module was not introduced. Risk: a seven-service dependency migration could break token/role compatibility. Dependency: a separately versioned shared contract and staged service-by-service rollout. Next action: extract only stable parsing/entry-point primitives after consumer contract tests exist for every service.

## 8. AUD-012 Status

- **BEFORE:** Malformed JSON/enum input could become 500; one generic 500 path exposed raw exception text.
- **FIX:** Added `HttpMessageNotReadableException` handling across services, safe generic 500 messages, and retained existing response envelopes for compatibility.
- **FILES:** All seven `GlobalExceptionHandler` implementations, associated error codes/tests.
- **TEST:** Invalid AI task/malformed body and auth/error regressions; full unit and runtime suites.
- **RESULT:** **FIXED**. Validation/malformed requests are 400; authentication is 401; authorization is 403; real not-found is 404; unexpected failures do not expose internals.

## 9. AUD-013 Status

- **BEFORE:** User assets relied on filename/content-type more than file content and reused unsafe client-controlled naming.
- **FIX:** Added per-kind allowlists, empty/size/MIME/extension/magic-byte checks, DOCX ZIP validation, filename normalization/traversal rejection, UUID object keys, checksum/actual content type, `nosniff`, and UTF-8 attachment disposition.
- **FILES:** `ProfileAssetFileValidator.java`, `ValidatedProfileAssetFile.java`, `ProfileAssetService.java`, storage interfaces/MinIO implementation, controllers/entities/config.
- **TEST:** Valid, empty, oversized, fake extension, wrong MIME, bad signature, traversal, ownership, cross-owner, and admin cases.
- **RESULT:** **FIXED**. Resume accepts PDF/DOC/DOCX; avatar accepts PNG/JPEG/WebP; portfolio/certificate use the explicit document/image allowlist.

## 10. AUD-014 Status

- **BEFORE:** Invalid JWT could fall through and become 403; unknown refresh token used 404.
- **FIX:** JWT filters reject malformed/tampered/expired tokens as 401. Unknown/revoked/reused refresh tokens use authentication semantics. Signing algorithm and secret strategy were unchanged; SHA-256 token hashing remains intact.
- **FILES:** JWT filters/security entry points, Auth `ErrorCode`, `AuthenticationService`, and integration tests.
- **TEST:** Missing/malformed/tampered JWT, unknown/reused/revoked refresh token, rotation/logout, insufficient role/ownership.
- **RESULT:** **FIXED**.

## 11. AUD-015 Status

- **BEFORE:** H2 could not validate Flyway/PostgreSQL JSONB/index/schema behavior.
- **FIX:** Added an opt-in `integration-test` Maven profile and Testcontainers 1.21.4 PostgreSQL 17 tests without removing H2 tests or requiring Compose.
- **FILES:** Auth/User/Application/Notification/AI POMs and five `*MigrationsPostgresIT.java` tests.
- **TEST:** `mvn verify -Pintegration-test --batch-mode` — 5 tests, 0 failures, 0 errors, 0 skipped.
- **RESULT:** **FIXED**. Flyway ran against real disposable PostgreSQL containers.

## 12. AUD-016 Status

- **BEFORE:** Production OpenAPI/Swagger and actuator policies were incomplete; health details could be public.
- **FIX:** Added Actuator where absent, limited public actuator access to health, disabled Swagger/OpenAPI in `prod`, exposed only health in production, and set health details to `never`.
- **FILES:** All seven POM/config/security sets and seven `application-prod.yml` files.
- **TEST:** Runtime development `/v3/api-docs` and `/actuator/health` returned 200 on all seven services; static verification confirms Swagger disabled in all seven production profiles.
- **RESULT:** **FIXED**.

## 13. AUD-017 Status

- **BEFORE:** CORS used framework defaults without a declared origin/header/method policy.
- **FIX:** Added configurable origin lists, bounded methods/headers/exposed headers/max-age, and validation that rejects wildcard origins when credentials are enabled.
- **FILES:** Seven `CorsConfig.java` files, seven service configs, `.env.example`.
- **TEST:** Auth OPTIONS regression plus real preflight response containing the requested local origin and allowed methods/headers.
- **RESULT:** **FIXED**.

## 14. AUD-018 Status

- **BEFORE:** Application and Recruitment had no matching `/api/v1/health`; other payloads differed.
- **FIX:** All seven now return top-level `status`, `service`, and `version` without secrets. Actuator health remains available for infrastructure.
- **FILES:** Seven health controllers, including new Application and Recruitment controllers.
- **TEST:** Real JAR runtime checked all seven endpoints; every endpoint returned 200/UP with service/version.
- **RESULT:** **FIXED**.

## 15. AUD-019 Status

- **BEFORE:** Compose referenced `minio/minio:latest`.
- **FIX:** Inspected the healthy running server version and pinned Compose to that exact release, avoiding a downgrade or random version selection.
- **FILES:** `infrastructure/compose/docker-compose.infrastructure.yml`.
- **TEST:** Existing User and AI MinIO upload/read paths plus real runtime User resume and AI resume upload/analyze flows passed.
- **RESULT:** **FIXED**. The already-running container retains its original display tag until normal recreation; its server release matches the pin.

## 16. Soft Delete Audit

- User/Company/Recruitment active/deleted filters and Phase 1 resume versioning behavior were retained.
- Application snapshots, status history, outbox, and Notification receipts remain historical records.
- AI resume hard deletion was replaced with `deleted_at`; active queries exclude deleted documents while analyses/matches/storage history remain available for audit integrity.
- New migration only: `ai-service/V6__add_resume_soft_delete.sql`.
- Regression: deleted AI resume returns 404 through the active API while its document/analysis remains persisted.

## 17. Error Contract Audit

- Existing service response models were retained; no competing envelope was introduced.
- Stable downstream codes cover invalid request/authentication/authorization/bad gateway/unavailable/timeout.
- `400 APP_007` for invalid application transition was deliberately preserved for Phase 1 API compatibility.
- Correlation IDs are returned in `X-Correlation-Id`; internal class/path/SQL/stack details are not returned.

## 18. Observability

- Correlation filters now cover all seven services and preserve incoming valid IDs or generate UUIDs.
- MDC/log patterns retain service name and correlation ID.
- Application event IDs and Notification receipt event IDs remain logged/persisted.
- Dependency failures are mapped to named codes rather than silently discarded.
- SQL value binding is OFF by default; SQL logging defaults to WARN and is explicitly overridable.
- Passwords, JWTs, refresh tokens, resume contents, secrets, and private profile data are not intentionally logged.

## 19. Resilience

- Connect/read timeouts are bounded and configurable.
- No retry was added to POST business mutations, preventing duplicate application/event/notification creation.
- No unbounded retry exists in the audited REST clients.
- Existing bounded outbox and Rabbit listener retry/DLQ behavior was retained and verified.
- 4xx calls are not retried or converted into generic absence.

## 20. Security Scan

- Scanned 697 tracked files without printing secret values.
- No private-key, provider-key, AWS-key, or hard-coded credential pattern was found.
- `.env` is ignored; `.env.example` contains placeholders/configuration only.
- The historical V2 refresh migration still contains the old column as required by immutable migration history; V8 migrates runtime storage to `token_hash` and removes plaintext `token`.
- `System.out`, `printStackTrace`, TODO, and FIXME hits exist only inside historical Markdown reports, not executable backend source.
- `git diff --check` passed (line-ending notices only).

## 21. PostgreSQL Integration Result

`mvn verify -Pintegration-test --batch-mode`: **PASS**.

| Suite | Result |
|---|---|
| Auth migrations V1–V8, disabled legacy admin, token hash | 1/1 PASS |
| User migrations through V15/current resume indexes | 1/1 PASS |
| Application JSONB snapshots/outbox | 1/1 PASS |
| Notification receipt event version/idempotency persistence | 1/1 PASS |
| AI JSONB and V6 soft delete | 1/1 PASS |

Total: 5 tests, 0 failures, 0 errors, 0 skipped.

## 22. Full E2E Result

**PASS** against seven JARs on ports 8081–8087 and the existing healthy Docker infrastructure.

- Five isolated identities: Candidate A/B, Employer A/B, Admin.
- Register/login/profile/company/DRAFT ownership/publish/search passed.
- No resume → `400 APP_010`; resume v1/v2/current, IDOR/admin, immutable v2 snapshot passed.
- Application creation, cross-owner denial, APPLIED→SCREENING, reverse rejection, withdraw/ownership passed.
- Application-created/status/withdraw notifications persisted for correct recipients.
- Duplicate Rabbit event retained exactly one receipt; invalid v999 event became FAILED after retry and reached DLQ.
- Refresh rotation, reuse denial, logout, revoked-token denial passed.
- AI MinIO upload, analysis, deterministic matching, Ollama explanation/interview/assistant, and AI soft delete passed.
- Seven health contracts and seven development OpenAPI endpoints passed.
- All temporary identities/primary database rows and 17 MinIO objects were removed; seven temporary JVMs were stopped. PostgreSQL/Redis/RabbitMQ/MinIO remained healthy.

## 23. AI Regression

- Unit/integration module: 43 tests, all green.
- Real AI provider health reported available.
- Real MinIO TXT resume upload, extraction/analysis, matching, Ollama explanation, interview, and assistant passed.
- Soft-deleted resumes are hidden from active APIs without deleting analysis/history/storage.
- Invalid assistant input is mapped to 400, not 500.

## 24. Remaining Issues

1. **AUD-011 shared module extraction — DEFERRED:** behavior is standardized, but code duplication remains. Address through a versioned, narrowly scoped security library and staged rollout with consumer contract tests.
2. **API Gateway/common-lib — planned architecture only:** both remain intentionally empty; no fake gateway or cosmetic shared module was created because frontend/routing requirements do not yet justify the architectural change.
3. **Mockito future-JDK warning — LOW technical debt:** tests pass on Java 21, but Mockito currently self-attaches its instrumentation agent; configure the documented test JVM agent before a future JDK disables dynamic attach.

No remaining acceptance blocker was observed in Phase 2 runtime/build/security verification.

## 25. Files Changed

The complete working tree contains Phase 1 plus Phase 2 changes: 82 modified and 54 untracked entries before this report (136 total; this report adds one entry). Major Phase 2 groups:

- REST/error handling: Application and Recruitment clients, factories, support/error codes, handlers, tests, and YAML.
- Upload: User validator/value object, asset service/controller/storage/entity/repository/config/tests.
- Security/CORS/observability: seven security/config sets, six new correlation filters (AI already had one), seven CORS configs, Auth entry point/filter/tests.
- Health/docs/actuator: seven health controllers, seven production profiles, seven service POM/config/security updates.
- PostgreSQL tests: five module POMs and five `*PostgresIT` suites.
- AI soft delete: entity/repository/services/tests and V6 migration.
- Infrastructure/config: `.env.example` and MinIO Compose pin.
- Report: `docs/BACKEND_HARDENING_PHASE_2_REPORT.md`.

Phase 1 files (outbox, notification receipt/DLQ, resume V15/versioning, lifecycle policy, IDOR, admin disable/bootstrap, refresh hashing) remain present and passing.

## 26. Migrations Added

No previously executed migration was edited.

| Migration | Phase | Purpose |
|---|---|---|
| Auth V7 | Phase 1 retained | Disable/randomize legacy seeded admin |
| Auth V8 | Phase 1 retained | SHA-256 refresh-token hash storage |
| User V15 | Phase 1 retained | Versioned current resumes |
| Application V2 | Phase 1 retained | Transactional outbox |
| Notification V2 | Phase 1 retained | Event contract version receipt |
| AI V6 | Phase 2 new | Resume `deleted_at` soft delete/index |

## 27. Test Counts

| Stage | Tests | Failures | Errors | Skipped | Result |
|---|---:|---:|---:|---:|---|
| Before Phase 2 | 77 | 0 | 0 | 0 | PASS |
| Final normal reactor | 87 | 0 | 0 | 0 | PASS |
| PostgreSQL integration profile (additional distinct ITs) | 5 | 0 | 0 | 0 | PASS |
| Distinct automated tests after hardening | 92 | 0 | 0 | 0 | PASS |

Final normal module distribution: Auth 12, User 11, Company 3, Recruitment 5, Application 7, Notification 6, AI 43.

## 28. Final Verdict

**PASS — HARDENING PHASE 2 acceptance criteria met, with one explicitly documented non-blocking structural deferment (AUD-011 shared-module extraction).**

- `mvn clean verify --batch-mode`: PASS, 87/87.
- `mvn verify -Pintegration-test --batch-mode`: PASS, 5/5 additional PostgreSQL ITs.
- Real E2E: PASS.
- Security/IDOR/admin/refresh hashing: PASS.
- Application/resume/snapshot/lifecycle: PASS.
- Notification/outbox/idempotency/retry/DLQ: PASS.
- Upload validation/ownership/traversal protection: PASS.
- Health/CORS/dev Swagger/prod restriction/Actuator: PASS.
- AI/Ollama/MinIO/matching: PASS.
- No commit, push, migration rewrite, framework change, or architecture rewrite performed.
