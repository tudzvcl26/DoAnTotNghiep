# BACKEND FULL AUDIT REPORT

Audit date: 2026-08-09 (Asia/Bangkok)
Repository root: `D:\DoAnTotNghiep`
Project root: `D:\DoAnTotNghiep\Projects\RecruitmentSystem`
Branch: `main`
Commit: `ccd7f86 feat(ai): integrate Ollama provider and unify environment configuration`
Audit mode: read/build/test/runtime only; no source, dependency, migration, or schema changes.

## 1 Executive Summary

The repository contains a real Maven reactor with seven Spring Boot 3.5.4 / Java 21 services. All seven services compile, pass their own test suites, pass independent `clean verify`, and pass the parent reactor `clean verify`. The aggregate test result is 69/69 passing.

All seven services were started against the real PostgreSQL 17, Redis 8, RabbitMQ 4, MinIO, and local Ollama infrastructure. PostgreSQL schema validation and Flyway startup succeeded. The main Auth, Company, Recruitment, Application, and AI runtime paths were exercised with real JWTs and PostgreSQL data. Test accounts, business records, and the MinIO object created by the audit were removed afterward.

The backend is **not ready for the next feature phase**. The complete business E2E is blocked by the following:

1. Application Service defines event classes but never publishes them and has no AMQP dependency/publisher.
2. Notification consumer is disabled in the current runtime environment; no business queue exists.
3. User Service has no resume asset type/API integrated with Application Service. An application can be submitted without a resume; its `ResumeSnapshot` is actually a profile JSON snapshot.
4. Application status can move backward, for example `SCREENING -> APPLIED`.
5. Authenticated users can read another candidate's nested profile data by ID; this was reproduced at runtime.

Overall result: **BUILD GREEN, CORE RUNTIME PARTIAL, CROSS-SERVICE E2E FAIL, SECURITY NOT READY**.

## 2 Repository Structure

```text
D:\DoAnTotNghiep                         # Git repository root
└── Projects\RecruitmentSystem           # application project root
    ├── backend
    │   ├── auth-service                  # implemented Maven module
    │   ├── user-service                  # implemented Maven module
    │   ├── company-service               # implemented Maven module
    │   ├── recruitment-service           # implemented Maven module
    │   ├── application-service           # implemented Maven module
    │   ├── notification-service          # implemented Maven module
    │   ├── ai-service                    # implemented Maven module
    │   ├── api-gateway                   # empty directory
    │   ├── common-lib                    # empty directory
    │   └── pom.xml                       # valid parent/reactor POM
    ├── database
    ├── docs
    ├── frontend\web-app                  # empty directory
    ├── infrastructure
    ├── storage
    ├── .env                              # exists locally, not tracked
    └── docker-compose*.yml
```

Git evidence at audit start and end:

- `pwd`: `D:\DoAnTotNghiep`
- branch: `main`
- status: clean, tracking `origin/main`
- last commits: `ccd7f86`, `0a81120`, `e64293e`, `4a15bdb`, `fe23c47`, `2d1de92`, `074db88`, `478e506`, `cb9eb65`, `e837037`
- no source changes were made by this audit

## 3 Service Inventory

All implemented services inherit Spring Boot `3.5.4`, use Java `21`, PostgreSQL, Flyway, JPA, Spring Security, validation, JJWT, and Springdoc. Exact direct dependencies were read from each `pom.xml`.

| Service | Main class | Port | Main integrations | Source inventory |
|---|---|---:|---|---|
| Auth | `com.recruitment.auth.AuthServiceApplication` | 8081 | PostgreSQL, Flyway, JWT | 34 Java; 2 controllers; 5 entities; 4 repositories; 2 tests; 6 migrations |
| User | `com.recruitment.user.UserServiceApplication` | 8082 | PostgreSQL, Flyway, MinIO, JWT | 123 Java; 11 controllers; 24 entities; 12 repositories; 12 mappers; 1 test; 14 migrations |
| Company | `com.recruitment.company.CompanyServiceApplication` | 8083 | PostgreSQL, Flyway, JWT | 39 Java; 2 controllers; 2 entities; 1 repository; 2 tests; 1 migration |
| Recruitment | `com.recruitment.recruitmentservice.RecruitmentServiceApplication` | 8084 | PostgreSQL, Flyway, Company REST client, JWT | 83 Java; 4 controllers; 12 entities; 7 repositories; 8 mappers; 2 tests; 2 migrations |
| Application | `com.recruitment.application.ApplicationServiceApplication` | 8085 | PostgreSQL, Flyway, User/Company/Recruitment REST clients, JWT | 58 Java; 2 controllers; 6 entities; 4 repositories; 4 mappers; 2 tests; 1 migration |
| Notification | `com.recruitment.notification.NotificationServiceApplication` | 8086 | PostgreSQL, Flyway, RabbitMQ consumer, JWT | 75 Java; 5 controllers; 12 entities; 6 repositories; 3 mappers; 2 tests; 1 migration |
| AI | `com.recruitment.ai.AiServiceApplication` | 8087 | PostgreSQL, Flyway, Redis, RabbitMQ, MinIO, Ollama/OpenAI, REST clients, JWT | 166 Java; 8 controllers; 20 entities; 15 repositories; 20 tests; 5 migrations |
| Gateway | none | — | none | directory exists; zero files |
| Common | none | — | none | directory exists; zero files |

Profiles/configuration:

- Main configuration is `application.yml` except Auth (`application.yaml`).
- Each service imports `.env` from current/parent/grandparent paths.
- Production/default JPA uses `ddl-auto: validate`; tests use H2 and `create-drop`.
- User and AI integrate MinIO. AI integrates Redis, RabbitMQ, Ollama, and optional OpenAI.
- Application and Recruitment REST clients exist but do not configure connect/read timeouts.
- Exception handlers, security filters, DTOs, repositories, services, and controllers exist in all implemented modules; their behavior is not considered passing merely from presence.

## 4 Build Status

| Service | `clean compile` | `clean verify` | Evidence |
|---|---|---|---|
| Auth | PASS | PASS | wrapper; verify 8 tests |
| User | PASS | PASS | Maven; verify 4 tests |
| Company | PASS | PASS | Maven; verify 3 tests |
| Recruitment | PASS | PASS | wrapper; verify 4 tests |
| Application | PASS | PASS | Maven; verify 2 tests |
| Notification | PASS | PASS | Maven; verify 5 tests |
| AI | PASS | PASS | Maven; verify 43 tests |

Parent `backend/pom.xml` is a valid packaging `pom` reactor and lists all seven real modules. Parent `mvn clean verify --batch-mode` passed all modules in 2:32 after audit-started JVMs were stopped (Windows had locked their JARs during the first reactor attempt).

## 5 Test Status

| Service | Tests | Result | Important limitation |
|---|---:|---|---|
| Auth | 8 | PASS | H2 integration profile |
| User | 4 | PASS | H2 integration profile |
| Company | 3 | PASS | H2 integration profile |
| Recruitment | 4 | PASS | H2 integration profile |
| Application | 2 | PASS | H2 integration profile; no event test |
| Notification | 5 | PASS | unit tests only; no real RabbitMQ event E2E |
| AI | 43 | PASS | integration tests use H2/test provider; real Ollama separately verified |

Total: **69 tests, 0 failures, 0 errors, 0 skipped**. H2 results were not used as proof of PostgreSQL runtime correctness; PostgreSQL was verified separately.

## 6 Runtime Status

| Service | Source | Build | Test | Runtime | Selected API flow | Status |
|---|---|---|---|---|---|---|
| Auth | YES | PASS | PASS | PASS | PASS | GREEN |
| User | YES | PASS | PASS | PASS | PARTIAL | YELLOW |
| Company | YES | PASS | PASS | PASS | PASS | GREEN |
| Recruitment | YES | PASS | PASS | PASS | PARTIAL | YELLOW |
| Application | YES | PASS | PASS | PASS | PARTIAL/FAIL lifecycle | RED |
| Notification | YES | PASS | PASS | PASS REST only | FAIL event flow | RED |
| AI | YES | PASS | PASS | PASS | PASS valid flows | GREEN |
| Gateway | NO | N/A | N/A | N/A | N/A | NOT IMPLEMENTED |
| Common | NO | N/A | N/A | N/A | N/A | NOT IMPLEMENTED |

All seven JVMs bound their configured ports and served live OpenAPI. Custom `/api/v1/health` returned 200 for Auth, User, Company, Notification, and AI. Recruitment and Application have no such controller and returned 500 instead of 404. AI Actuator health was `UP` with `db`, `redis`, `rabbit`, `aiMinio`, and `ollama` all `UP`. Audit JVMs were stopped afterward.

## 7 Database Status

PostgreSQL 17 was healthy and accepted real connections. Schemas and application table counts at audit time were:

| Schema | Tables | Flyway versions | Result |
|---|---:|---|---|
| `public` (Auth) | 7 | V1–V6 | PASS |
| `user_service` | 13 | V1–V14 | PASS |
| `company_service` | 2 | V1 | PASS |
| `recruitment_service` | 8 | V1–V2 | PASS |
| `application_service` | 5 | V1 | PASS |
| `notification_service` | 7 | V1 | PASS |
| `ai_service` | 16 | V1–V5 | PASS |

No duplicate or missing migration version was found. Every Flyway history row had `success=true`. Hibernate `ddl-auto: validate` succeeded when each service started against PostgreSQL.

Type/constraint findings:

- UUID columns map to Java `UUID` and validated at runtime.
- Application `resume_snapshots.snapshot_data` and `job_snapshots.snapshot_data` are PostgreSQL `JSONB`; entities use `String` with `@JdbcTypeCode(SqlTypes.JSON)`. Real PostgreSQL insert/read passed.
- Application unique `(candidate_id, job_id)`, snapshot uniqueness, FKs, and indexes exist.
- AI JSON/JSONB persistence, rule-match breakdown, analysis items, explanation, interview, and assistant data passed on PostgreSQL.
- No cross-service JPA repository or direct cross-schema SQL was found in Java services. Cross-service identifiers intentionally have no database FKs.
- Soft deletion is inconsistent: `deleted_at` in User/Company/Notification state, `active` flags in Recruitment/Application, and hard delete semantics in parts of AI.
- Exact automated comparison of every nullable DTO field against every DB column was not independently proven; startup validation covers entity/schema compatibility, not DTO semantics.

## 8 Infrastructure Status

| Component | Container | Health/function | Port | Network | Persistence |
|---|---|---|---|---|---|
| PostgreSQL | `recruitment-postgres` (`postgres:17`) | healthy; `pg_isready` PASS; SQL PASS | 5432 | `recruitment-network` | named volume + init bind mount |
| Redis | `recruitment-redis` (`redis:8`) | healthy; authenticated `PING` = `PONG` | 6379 | `recruitment-network` | named volume |
| RabbitMQ | `recruitment-rabbitmq` (`rabbitmq:4-management`) | healthy; diagnostics ping PASS | 5672/15672 | `recruitment-network` | named volume |
| MinIO | `recruitment-minio` (`minio/minio:latest`) | healthy; live/ready HTTP 200 | 9000/9001 | `recruitment-network` | named volume |
| Ollama | host process/API | `/api/tags` reachable; 2 models; structured generation PASS | 11434 | host | local model store |

RabbitMQ had no business queue or custom exchange. This is consistent with `NOTIFICATION_AMQP_CONSUMER_ENABLED=false` and the missing Application publisher. The audit did not print credentials. `minio/minio:latest` is an unpinned reliability risk.

## 9 API Inventory

Status meanings: `OPENAPI` means the runtime OpenAPI endpoint advertised the route; it does not mean the business flow passed. `TESTED` adds runtime flow evidence. OpenAPI generally omits method-level role metadata even where source has `@PreAuthorize`, so effective access below comes from source security rules plus runtime authorization checks.

| Method | Path | Service | Auth | Role | Status |
|---|---|---|---|---|---|
| POST | `/api/v1/auth/register`, `/login`, `/refresh` | Auth | Public | — | TESTED |
| POST | `/api/v1/auth/logout` | Auth | Authenticated | any | TESTED |
| GET | `/api/v1/auth/me` | Auth | Authenticated | any | TESTED |
| GET | `/api/v1/health` | Auth | Public | — | TESTED |
| POST | `/api/v1/profiles/initialize` | User | Authenticated | Candidate/Admin | TESTED |
| GET/PUT/DELETE | `/api/v1/profiles/me` | User | Authenticated | owner; writes Candidate/Admin | TESTED GET/PUT |
| GET | `/api/v1/profiles` | User | Authenticated | Employer/Admin | OPENAPI |
| GET/POST/PUT/DELETE | `/api/v1/users/{userId}/educations[/{educationId}]` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | PARTIAL; read IDOR reproduced |
| GET/POST/PUT/DELETE | `/api/v1/users/{userId}/experiences[/{experienceId}]` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | OPENAPI |
| GET/POST/PUT/DELETE | `/api/v1/users/{userId}/skills[/{userSkillId}]` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | TESTED create |
| GET/POST/PUT/DELETE | `/api/v1/users/{userId}/languages[/{userLanguageId}]` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | TESTED create |
| GET/POST/PUT/DELETE | `/api/v1/users/{userId}/certificates[/{certificateId}]` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | OPENAPI; one invalid runtime request 400 |
| GET/POST/PUT/DELETE | `/api/v1/users/{userId}/social-links[/{socialLinkId}]` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | TESTED create |
| GET/PUT/DELETE | `/api/v1/users/{userId}/career-objective` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | TESTED PUT |
| GET/POST/PUT/DELETE | `/api/v1/users/{userId}/candidate-preference` | User | Authenticated | reads any auth; writes Candidate/Admin + owner check | TESTED POST |
| POST | `/api/v1/users/{userId}/assets/upload` | User | Authenticated | Candidate/Admin + owner | OPENAPI; success NOT VERIFIED |
| GET/DELETE | `/api/v1/users/{userId}/assets[/{assetId}]` | User | Authenticated | mixed read/owner rules | OPENAPI |
| GET | `/api/v1/users/{userId}/assets/avatar`, `/download/{assetId}` | User | Authenticated | mixed read/owner rules | OPENAPI |
| GET | `/api/v1/health` | User | Public | — | TESTED |
| GET | `/api/v1/companies`, `/{id}`, `/slug/{slug}`, `/search` | Company | Public | — | TESTED selected |
| POST/PUT/DELETE | `/api/v1/companies[/{id}]` | Company | Authenticated | Employer/Admin + owner | TESTED |
| GET | `/api/v1/health` | Company | Public | — | TESTED |
| GET | `/api/v1/jobs`, `/{id}`, `/search` | Recruitment | Public | — | TESTED; own DRAFT defect |
| POST/PUT/DELETE/PATCH | `/api/v1/jobs`, `/{id}`, `/{id}/publish`, `/{id}/close` | Recruitment | Authenticated | Employer/Admin + owner | TESTED create/update denial/publish; close source only |
| GET/POST/PUT/DELETE | `/api/v1/job-categories[/{id}]`, `/search` | Recruitment | GET public; writes auth | Admin writes | TESTED create/search |
| GET/POST/PUT/DELETE | `/api/v1/skills[/{id}]`, `/search` | Recruitment | GET public; writes auth | Admin writes | OPENAPI |
| GET/POST/PUT/DELETE | `/api/v1/benefits[/{id}]`, `/search` | Recruitment | GET public; writes auth | Admin writes | OPENAPI |
| POST | `/api/v1/applications` | Application | Authenticated | Candidate/Admin | TESTED |
| GET | `/api/v1/applications/my`, `/{id}` | Application | Authenticated | owner/company owner/Admin | TESTED |
| PATCH | `/api/v1/applications/{id}/withdraw` | Application | Authenticated | Candidate owner/Admin | TESTED |
| PATCH | `/api/v1/applications/{id}/status` | Application | Authenticated | Employer company owner/Admin | TESTED; transition defect |
| GET | `/api/v1/jobs/{jobId}/applications` | Application | Authenticated | Employer company owner/Admin | TESTED |
| GET/POST/PATCH/DELETE | `/api/v1/notifications...` | Notification | Authenticated | self; create/broadcast Admin | REST TESTED list only; event FAIL |
| GET/PUT | `/api/v1/notifications/preferences` | Notification | Authenticated | self | OPENAPI |
| GET/POST/PUT/PATCH | `/api/v1/notification-templates...` | Notification | Authenticated | Admin | OPENAPI |
| GET | `/api/v1/admin/notification-delivery-logs` | Notification | Authenticated | Admin | OPENAPI |
| GET | `/api/v1/health` | Notification | Public | — | TESTED |
| POST/GET/DELETE | `/api/v1/ai/resumes...` | AI | Authenticated | Candidate/Admin + owner | TESTED upload/analyze/get/delete |
| POST/GET | `/api/v1/ai/matching...` | AI | Authenticated | Candidate/Employer/Admin by resource | TESTED create/get data |
| POST/GET | `/api/v1/ai/matching/{id}/explanation`, `/interview` | AI | Authenticated | resource owner/Admin | TESTED |
| POST | `/api/v1/ai/assistant/candidate`, `/recruiter` | AI | Authenticated | Candidate or Employer/Admin | candidate TESTED |
| GET | `/api/v1/ai/tasks...`, `/recommendations...` | AI | Authenticated | contextual roles | OPENAPI |
| GET | `/api/v1/ai/providers` | AI | Authenticated | Admin | TESTED |
| GET | `/api/v1/health` | AI | Public | — | TESTED |

No duplicate runtime route was found. Swagger route inventory matched source route presence. Swagger security/role metadata is incomplete and should not be treated as the authorization source of truth.

## 10 Auth Flow

`AUTH FLOW = PASS WITH ERROR-SEMANTICS DEFECTS`

Runtime evidence:

- Register: 201 and access/refresh pair present.
- Login: 200 and access/refresh pair present.
- `/me`: 200, correct email, role `CANDIDATE`.
- JWT payload contained `sub`, `userId`, `email`, `roles`, `iat`, `exp`, and `token_type=access`; `exp > iat`.
- JWT signing uses JJWT `verifyWith(getSigningKey()).parseSignedClaims(...)`; a tampered token was rejected.
- Refresh: 200 and new access token present; old refresh rotated/revoked.
- Logout: 200; reuse of revoked refresh returned 401.
- Defects: tampered access token returned 403 instead of 401; malformed/unknown refresh returned 404 instead of 400/401.
- Password hashing uses `BCryptPasswordEncoder`.

## 11 User Flow

`USER FLOW = PARTIAL / SECURITY FAIL`

Passed on real runtime/PostgreSQL: profile initialize, get/update profile, education create in the first scenario, skill create, language create, social-link create, career-objective upsert, candidate-preference create, and completion-score execution. A test request for Certificate returned 400 and the complete certificate CRUD was not verified. Experience CRUD and successful User Service asset upload/download/delete were not fully exercised.

Critical functional gap: `ProfileAssetKind` only contains `AVATAR`, `PORTFOLIO`, and `CERTIFICATE_ATTACHMENT`; there is no `RESUME`. Cross-candidate nested reads do not call `assertProfileOwner`. Runtime reproduced another authenticated employer reading Candidate A's education with HTTP 200. Write services generally call `assertProfileOwner`; cross-owner Application/Company/Job writes were correctly denied.

## 12 Company Flow

`COMPANY FLOW = PASS FOR TESTED CORE`

- Employer A and Employer B created separate companies (201).
- Public get/search/slug routes exist; selected public get was exercised indirectly by Recruitment/Application clients.
- Candidate create was denied 403.
- Employer A updating Company B was denied 403.
- Soft-delete source exists; delete runtime was not executed to preserve the E2E record until cleanup.
- Admin bypass is implemented and admin-only category setup succeeded using a test admin JWT.

## 13 Recruitment Flow

`RECRUITMENT FLOW = PARTIAL`

- Admin category creation: PASS.
- Employer job creation: 201, initial status `DRAFT`.
- Employer B updating Employer A's job: 403.
- Candidate apply before publish: rejected (downstream surfaced 404).
- Publish: 200, status `PUBLISHED`.
- Candidate search/get published job: 200.
- Close transition is implemented (`PUBLISHED -> CLOSED`) but was not run in the retained Application scenario.
- Defect: Employer A cannot GET its own DRAFT job; `JobServiceImpl.getById` only allows Admin or `PUBLISHED`, producing 404 for the owner.

## 14 Application Flow

`APPLICATION FLOW = FAIL BUSINESS INTEGRITY / PASS CORE PERSISTENCE`

Runtime successes on PostgreSQL:

- Candidate applied to a published job: 201.
- Candidate list (`my`): 200 with one item.
- Employer job application list/detail: 200.
- Employer B and Candidate B application detail: 403.
- Resume snapshot, job snapshot, and initial status history persisted.
- Employer updated to `SCREENING`; Candidate observed status.
- Candidate withdraw persisted and history count increased.
- JSONB snapshots round-tripped on PostgreSQL.

Failures:

- `SCREENING -> APPLIED` was accepted with HTTP 200. Only terminal status and `WITHDRAWN` checks exist; an explicit transition matrix does not.
- Application succeeds without any uploaded resume. `ResumeSnapshot.snapshotData` is populated from `/api/v1/profiles/me`, not from a resume version/content/asset.
- No event is published for create/status/withdraw.

## 15 Notification Flow

`NOTIFICATION EVENT FLOW = FAIL`

- Notification REST service started and list endpoint returned 200.
- RabbitMQ itself was healthy and reachable.
- Before/after an application create and status changes, notification count delta was 0.
- RabbitMQ business queue count remained 0.
- `ApplicationCreatedEvent`, `ApplicationStatusChangedEvent`, and `ApplicationWithdrawnEvent` are referenced only by their own definition files.
- Application `pom.xml` has no AMQP dependency; `ApplicationServiceImpl` has no publisher call.
- Current environment sets the notification consumer disabled.
- Mark read/all read/unread count/preferences are present in source/OpenAPI but were not considered event-flow PASS.

## 16 AI Flow

`AI FLOW = PASS FOR VALID TESTED PATHS`

Real runtime evidence:

- AI health: `UP`; PostgreSQL, Redis, RabbitMQ, MinIO, and Ollama components `UP`.
- Ollama list/API: reachable with two local model tags. Direct `Qwen2.5:3b` structured JSON generation succeeded.
- Resume TXT upload: success, document status `READY`; MinIO object created.
- Resume analysis: 200; 15 skills and 5 keywords persisted; analysis retrieved as structured data.
- Rule engine match: 200; overall score 87, breakdown present, missing-skill list produced. Rule-based engine remained the scorer; AI was used for explanation afterward.
- Explanation: 200 with validated structured response.
- Interview preparation: 200 with structured question output.
- Candidate assistant with valid enum `RESUME_IMPROVEMENT`: 200 via `ollama`, model `Qwen2.5:3B-Instruct`, structured response.
- Invalid assistant task enum returned 500 rather than 400 (error mapping defect).
- OpenAI was disabled in the current environment and therefore `NOT VERIFIED` at runtime.
- Audit resume was deleted via AI API, removing the MinIO object and persisted dependent data.

## 17 Cross-Service Flow

`CROSS-SERVICE FLOW = FAIL OVERALL`

| Hop | Result | Evidence |
|---|---|---|
| Auth -> all services | PASS | real JWT propagated and verified; role/identity worked |
| User -> Application | PARTIAL | profile REST call and snapshot passed; actual resume integration missing |
| Company -> Recruitment | PASS | owner lookup and cross-owner denial passed |
| Recruitment -> Application | PASS core | published job lookup and apply rule passed |
| Application -> Notification | FAIL | no producer/event publication; consumer disabled |
| Recruitment/User -> AI | PASS tested | job REST lookup and uploaded AI resume used for matching |
| Error propagation | FAIL quality | downstream errors are swallowed/conflated; several invalid/missing cases become 500/404 |
| REST timeouts | FAIL | Application and Recruitment clients have no explicit timeout |

## 18 Security Audit

| Control | Result | Evidence |
|---|---|---|
| JWT signature | PASS | signed JJWT parse and tamper rejection |
| JWT claims/role normalization | PASS tested | Auth emits `ROLE_CANDIDATE`; downstream `hasRole` worked |
| Stateless sessions / CSRF | PASS design | all SecurityConfigs use STATELESS and disable CSRF |
| Password hashing | PASS | BCrypt |
| Refresh rotation/logout | PASS behavior | rotation/revocation tested |
| Refresh token storage | FAIL hardening | full bearer refresh token stored/queryable in DB, not a hash |
| RBAC | PARTIAL | role gates worked in tested writes; Swagger metadata incomplete |
| Ownership | PARTIAL | Company/Job/Application pass; User nested reads fail |
| IDOR | FAIL | cross-user Education read returned 200 |
| Public endpoints | PARTIAL | intended company/job reads public; docs and broad actuator exposure public |
| CORS | NOT VERIFIED/likely incomplete | `cors(withDefaults())` but no explicit `CorsConfigurationSource` found |
| Error leakage | FAIL | Company catch-all returns `ex.getMessage()`; public detailed health in several services |
| SQL injection | no direct finding | repositories/derived queries used; no string-built native query finding |
| Mass assignment | no direct finding | controllers accept DTOs, not entities |
| User file upload | FAIL hardening | size/empty check only; no MIME/extension/signature allowlist |
| AI file upload | PASS tested | size, filename normalization, content type, extension, and magic/signature checks |
| Path traversal | no AI finding | basename normalization; MinIO uses generated object keys |
| Swagger exposure | INFO risk | public on every service |

## 19 Performance Quick Check

- Most list APIs use `Pageable` or bounded page/size; no obvious unbounded main entity list was found. Notification preferences intentionally return a small list.
- Application response detail performs separate snapshot and history queries; acceptable at single-detail scope but should be watched.
- No explicit connect/read timeouts in Application's three `RestClient`s or Recruitment's Company client. AI clients do configure timeouts.
- Application REST clients catch broad `Exception` and return `Optional.empty`, conflating timeout/401/500 with not-found.
- Transaction annotations exist across services; Application create/snapshots/history are in one transaction.
- JPA `open-in-view=false` reduces lazy-loading leakage; no reproduced lazy-loading exception.
- Hikari pool uses defaults; no workload-specific pool configuration found.
- User multipart config allows 20 MB while service validation rejects above 10 MB; inconsistent limits.
- AI has configured provider timeouts and model fallback/router logic. OpenAI fallback was not runtime tested.
- Notification config names DLQ resources, but no active consumer/topology existed; retry/DLQ behavior is `NOT VERIFIED`.

## 20 Git/Secret Audit

- Git worktree was clean before report creation.
- Project `.env` exists but is **not tracked**.
- Main tracked `application*.yml` and compose secrets use environment placeholders; no real value is printed in this report.
- Tracked test profiles contain literal **test-only** DB/JWT values. These are not production credentials but should remain scoped to tests.
- A fixed default admin account and fixed BCrypt password hash are committed in `auth-service` migration V5. This is a production security blocker unless the credential is forced to rotate/disabled outside development.
- No tracked private-key file/pattern was found.
- Sensitive-key word scans naturally found DTO/config/token code; this is not evidence that every hit is a real secret.
- No secret value was printed by the audit.

## 21 Frontend Status

`FRONTEND = NOT IMPLEMENTED`

`frontend/web-app` exists but contains no source/package manifest. Build, dependencies, API base URL, and auth compatibility are therefore `N/A`.

## 22 Gateway Status

`API GATEWAY = NOT IMPLEMENTED`

`backend/api-gateway` exists but contains zero files and is not a reactor module. Services are exposed directly on ports 8081–8087.

## 23 End-to-End Scenario

`END-TO-END SCENARIO = FAIL`

Tested chain:

```text
Candidate/Admin/Employer accounts + real JWTs
  -> Candidate profile
  -> Employer company
  -> Admin category
  -> Employer DRAFT job -> PUBLISHED
  -> Candidate job search
  -> Candidate application
  -> PostgreSQL resume/profile snapshot + job snapshot + status history
  -> Employer list/detail/status change
  -> Candidate updated status/withdraw
  -> AI TXT resume upload/analysis
  -> deterministic job match
  -> Ollama explanation/interview/assistant
```

The chain passes through AI but not through the required unified User resume or Notification event path. Application accepted a candidate with no User resume, and Application produced no RabbitMQ message or notification. Therefore the requested final E2E cannot be marked PASS.

All audit-created users (`audit.%@example.test`), profiles, companies, jobs, categories, applications/snapshots/history, and AI resume data were removed. Verification after cleanup found zero audit users, companies, jobs, or categories. Infrastructure containers were left healthy because they were already part of the workspace environment; audit-started JVMs were stopped.

## 24 Bugs Found

### AUD-001

- **Severity:** HIGH
- **Service:** Application / Notification
- **File:** `backend/application-service/pom.xml`; `backend/application-service/src/main/java/com/recruitment/application/service/impl/ApplicationServiceImpl.java:65`; event classes under `.../event/`
- **Problem:** Application events are never published.
- **Evidence:** Event classes have no usages outside their definition; no AMQP dependency/publisher; RabbitMQ queue count and notification delta stayed zero after create/status/withdraw.
- **Impact:** Required notifications and event-driven integrations never occur.
- **Recommended Fix:** Add an outbox-backed publisher or transactional event/outbox flow, declare versioned contracts, publish create/status/withdraw events, and add real RabbitMQ integration tests.
- **Blocker:** YES

### AUD-002

- **Severity:** HIGH
- **Service:** Notification
- **File:** `.env` (untracked runtime setting, value not secret); `backend/notification-service/src/main/java/com/recruitment/notification/consumer/NotificationEventConsumer.java:11`
- **Problem:** Notification AMQP consumer is disabled in the current environment.
- **Evidence:** conditional consumer property evaluated false; no queue/exchange; consumer received nothing.
- **Impact:** Even a future producer would not create notifications in this runtime.
- **Recommended Fix:** Enable the consumer in runnable environments, declare durable topology/DLQ, and prove consume/persist/idempotency/retry behavior.
- **Blocker:** YES

### AUD-003

- **Severity:** HIGH
- **Service:** User / Application
- **File:** `backend/user-service/src/main/java/com/recruitment/user/entity/ProfileAssetKind.java:3`; `backend/application-service/src/main/java/com/recruitment/application/service/impl/ApplicationServiceImpl.java:99`
- **Problem:** There is no User resume asset integration; `ResumeSnapshot` contains profile JSON.
- **Evidence:** asset enum has no RESUME; application snapshots `userProfile.rawJsonData`; apply without uploaded resume returned 201.
- **Impact:** Employers do not receive an immutable resume snapshot; audit/compliance and matching inputs can be wrong.
- **Recommended Fix:** Define a private versioned resume resource, require/select it during apply, fetch with propagated JWT/service auth, and snapshot verified resume metadata/content reference.
- **Blocker:** YES

### AUD-004

- **Severity:** HIGH
- **Service:** Application
- **File:** `backend/application-service/src/main/java/com/recruitment/application/service/impl/ApplicationServiceImpl.java:215`
- **Problem:** No allowed-transition matrix; non-terminal statuses can move arbitrarily.
- **Evidence:** runtime accepted `SCREENING -> APPLIED` with 200.
- **Impact:** Recruitment workflow history can be invalid or manipulated.
- **Recommended Fix:** Encode explicit transitions and test every allowed/denied edge, including role-specific transitions.
- **Blocker:** YES

### AUD-005

- **Severity:** HIGH
- **Service:** User
- **File:** `backend/user-service/src/main/java/com/recruitment/user/service/EducationService.java:30` and analogous nested read services
- **Problem:** Nested profile reads do not enforce owner/visibility policy.
- **Evidence:** `getAll/getById` omit `assertProfileOwner`; cross-user runtime read returned 200.
- **Impact:** IDOR disclosure of education, experience, certificate, preference, social, language, skill, or asset data depending on endpoint.
- **Recommended Fix:** Centralize visibility/ownership authorization, query by both owner and child ID, and add Candidate A/B and Employer tests for every nested resource.
- **Blocker:** YES

### AUD-006

- **Severity:** HIGH
- **Service:** Auth
- **File:** `backend/auth-service/src/main/resources/db/migration/V5__seed_admin_account.sql:20`
- **Problem:** A fixed admin identity and password hash are committed and automatically seeded.
- **Evidence:** tracked migration contains fixed admin email/hash; row exists in real DB migration history.
- **Impact:** Known/default credentials can yield full-system compromise if enabled in a deployed environment.
- **Recommended Fix:** Never seed deployable admin credentials in schema migrations; bootstrap through a one-time external secret/workflow and require rotation.
- **Blocker:** YES

### AUD-007

- **Severity:** HIGH
- **Service:** Auth
- **File:** `backend/auth-service/src/main/java/com/recruitment/auth/service/AuthenticationService.java:98`; `.../entity/RefreshToken.java:18`
- **Problem:** Refresh bearer tokens are stored in plaintext and queried by full token.
- **Evidence:** entity stores `token TEXT`; service calls `findByToken(refreshToken)`.
- **Impact:** Database read access immediately exposes reusable session credentials.
- **Recommended Fix:** Store a cryptographic hash/token identifier, compare hashes, retain rotation/revocation metadata, and add reuse detection.
- **Blocker:** YES

### AUD-008

- **Severity:** MEDIUM
- **Service:** Recruitment
- **File:** `backend/recruitment-service/src/main/java/com/recruitment/recruitmentservice/service/impl/JobServiceImpl.java:108`
- **Problem:** Employer cannot retrieve its own DRAFT job.
- **Evidence:** only Admin sees any active job; everyone else is filtered to PUBLISHED; owner runtime GET returned 404.
- **Impact:** Employer edit/review UX cannot reliably reload drafts.
- **Recommended Fix:** Add authenticated owner-aware retrieval or separate management/public endpoints.
- **Blocker:** NO

### AUD-009

- **Severity:** MEDIUM
- **Service:** Application / Recruitment
- **File:** `application-service/.../client/UserClientImpl.java:24`, `JobClientImpl.java:26`, `CompanyClientImpl.java:24`; `recruitment-service/.../client/CompanyClientImpl.java:20`
- **Problem:** REST clients have no explicit connect/read timeout.
- **Evidence:** bare `RestClient.builder().baseUrl(...).build()`.
- **Impact:** thread exhaustion and cascading hangs during dependency failure.
- **Recommended Fix:** configure bounded request factories, resilience policy, metrics, and contract-specific error mapping.
- **Blocker:** NO

### AUD-010

- **Severity:** MEDIUM
- **Service:** Application / Recruitment
- **File:** same REST clients, broad catches at `UserClientImpl.java:64`, `JobClientImpl.java:70`, `CompanyClientImpl.java:56`
- **Problem:** all downstream failures are swallowed and returned as `Optional.empty`.
- **Evidence:** broad `catch (Exception)`; draft apply surfaced downstream 404 as local not-found.
- **Impact:** 401/403/timeout/500 becomes misleading 404/business error and impedes incident diagnosis.
- **Recommended Fix:** distinguish 404 from auth, timeout, connection, and 5xx; propagate stable error codes/correlation IDs.
- **Blocker:** NO

### AUD-011

- **Severity:** MEDIUM
- **Service:** All
- **File:** seven separate `security/JwtService.java` and authentication filter implementations
- **Problem:** JWT verification/role mapping is duplicated across every service.
- **Evidence:** seven independent implementations and hashes; `common-lib` is empty.
- **Impact:** security drift and inconsistent 401/403 behavior.
- **Recommended Fix:** use a maintained shared security module or gateway/resource-server standard after blockers are fixed; retain service-level authorization.
- **Blocker:** NO

### AUD-012

- **Severity:** MEDIUM
- **Service:** Multiple
- **File:** each `GlobalExceptionHandler.java`; Company handler line 139
- **Problem:** inconsistent error mapping; broad catch-all converts client/missing-route errors to 500, and Company exposes exception messages.
- **Evidence:** missing Recruitment/Application health route returned 500; invalid AI enum returned 500; invalid token/refresh statuses vary; Company response uses `ex.getMessage()`.
- **Impact:** API contract instability and possible internal-detail leakage.
- **Recommended Fix:** map validation/deserialization/no-resource/security/downstream exceptions consistently; never expose raw exception text.
- **Blocker:** NO

### AUD-013

- **Severity:** MEDIUM
- **Service:** User
- **File:** `backend/user-service/src/main/java/com/recruitment/user/service/ProfileAssetService.java:75`
- **Problem:** User asset upload validates only non-empty and size, then trusts supplied content type.
- **Evidence:** no extension/MIME/signature allowlist; AI validator demonstrates the stronger expected pattern.
- **Impact:** arbitrary object upload, unsafe download/content handling, and storage abuse.
- **Recommended Fix:** per-kind allowlist, signature sniffing, safe download headers, malware scanning where appropriate, and tests.
- **Blocker:** NO

### AUD-014

- **Severity:** MEDIUM
- **Service:** Auth / API contracts
- **File:** Auth/security exception handling and global handlers
- **Problem:** authentication/error HTTP semantics are inconsistent.
- **Evidence:** tampered access returned 403; unknown refresh returned 404; invalid AI enum returned 500.
- **Impact:** clients cannot reliably distinguish authentication, authorization, validation, and server failure.
- **Recommended Fix:** standardize 400/401/403/404 mapping and error envelope across services.
- **Blocker:** NO

### AUD-015

- **Severity:** MEDIUM
- **Service:** Tests / Database
- **File:** each service `src/test/resources/application-test.yml`
- **Problem:** core integration suites use H2 rather than PostgreSQL/Testcontainers.
- **Evidence:** H2 PostgreSQL compatibility mode and Flyway disabled in tests; JSONB conclusions required manual runtime testing.
- **Impact:** PostgreSQL-specific types, SQL, constraints, and migrations can regress without CI detection.
- **Recommended Fix:** add Testcontainers PostgreSQL/RabbitMQ/Redis/MinIO integration suites while retaining fast unit tests.
- **Blocker:** NO

### AUD-016

- **Severity:** LOW
- **Service:** Multiple
- **File:** SecurityConfigs and main application YAML files
- **Problem:** Swagger is public everywhere; several services expose broad Actuator paths/details.
- **Evidence:** public `/swagger-ui/**`, `/v3/api-docs/**`, sometimes `/actuator/**`; `show-details: always` in public services.
- **Impact:** reconnaissance and environment/component information disclosure.
- **Recommended Fix:** restrict/disable docs and detailed health outside development; expose only readiness/liveness as needed.
- **Blocker:** NO

### AUD-017

- **Severity:** LOW
- **Service:** Multiple
- **File:** all `SecurityConfig.java`
- **Problem:** CORS uses defaults with no explicit application CORS configuration found.
- **Evidence:** `cors(Customizer.withDefaults())`; no `CorsConfigurationSource`.
- **Impact:** future browser frontend may be blocked or later receive an overly broad ad-hoc configuration.
- **Recommended Fix:** define environment-specific exact origins/methods/headers and test preflight behavior.
- **Blocker:** NO

### AUD-018

- **Severity:** LOW
- **Service:** Recruitment / Application / OpenAPI
- **File:** missing HealthController; controller OpenAPI annotations
- **Problem:** health endpoints and OpenAPI security metadata are inconsistent.
- **Evidence:** Recruitment/Application `/api/v1/health` return 500; OpenAPI advertised routes but generally no effective role metadata.
- **Impact:** misleading monitoring/client documentation.
- **Recommended Fix:** standardize health endpoints and annotate security requirements/roles or generate them consistently.
- **Blocker:** NO

### AUD-019

- **Severity:** INFO
- **Service:** Infrastructure
- **File:** compose configuration
- **Problem:** MinIO image uses `latest`.
- **Evidence:** running image `minio/minio:latest`.
- **Impact:** non-reproducible upgrades.
- **Recommended Fix:** pin a tested immutable version/digest in a planned dependency change.
- **Blocker:** NO

## 25 Technical Debt

- `common-lib` and `api-gateway` are empty placeholders.
- Security, API envelopes, paging envelopes, error codes, and JWT filters are duplicated.
- 44 broad `catch (Exception)` occurrences exist; many are legitimate boundary wrapping, but REST-client/error paths need narrowing.
- Soft-delete mechanisms and audit columns differ per service.
- No complete automated API contract/E2E suite exists.
- Runtime configuration depends on a root `.env`; service-specific typed validation is incomplete outside AI.
- Main image/version reproducibility is incomplete (`latest`).
- Exact unused-class/dependency analysis is `NOT VERIFIED`; no source deletion is recommended without compiler/static-tool evidence.
- No TODO/FIXME, `System.out`, `printStackTrace`, empty catch, entity request-body mass assignment, or password hash in response DTO was found.

## 26 Missing Features

- API Gateway implementation.
- Shared Common library implementation.
- Frontend implementation.
- Unified candidate resume ownership/version API in User Service.
- Application event producer/outbox and active Notification event topology.
- Explicit Application lifecycle transition policy.
- Full PostgreSQL/RabbitMQ/MinIO/Redis automated integration suite.
- Cross-service observability/correlation and resilient REST client policy.

## 27 Recommended Next Steps

**Single next action:** implement and prove the Application event outbox/publisher plus enabled Notification consumer on real RabbitMQ, including idempotent persistence, retry/DLQ, and an integration test from Application create/status to recipient notification. Do not start new feature work until AUD-001/AUD-002 are closed; in the same blocker-remediation phase, align the event payload with the new versioned resume reference required by AUD-003.

# POST-FIX HARDENING RESULT

## 28 Scope and Final Verdict

- **Hardening date:** 2026-08-09 (Asia/Bangkok).
- **Requested scope:** the seven HIGH findings `AUD-001` through `AUD-007`.
- **Final verdict:** **7/7 HIGH findings fixed and verified.** No HIGH finding from the original audit remains open.
- **Additional acceptance correction:** the employer-owner DRAFT read case from `AUD-008` was corrected because it was an explicit required E2E assertion. The other MEDIUM/LOW/INFO findings remain outside this hardening scope.
- **Source-control handling:** no commit was created. The already-applied V5 auth migration was not edited.

## 29 HIGH Finding Closure Matrix

| ID | Final status | Implemented hardening | Verification evidence |
|---|---|---|---|
| AUD-001 | **FIXED** | Added transactional Application outbox table/entity/service; Application create/status/withdraw persist versioned events in the same DB transaction; scheduled Rabbit publisher uses durable topic exchange, publisher confirms, persisted retry/backoff, and terminal FAILED status. | PostgreSQL migration `application_service` V2 applied; real E2E produced 3 events and all reached `PUBLISHED`; main Rabbit queue returned to 0 messages. |
| AUD-002 | **FIXED** | Enabled Notification consumer by default; retained durable exchange/queue/DLX/DLQ; added event contract version; persisted idempotency/failure receipts; listener retry is 3 attempts with reject-to-DLQ. Recipient rules are submitted/withdrawn -> employer owner, status change -> candidate. | Real Rabbit E2E delivered all three event types to the required recipients. Duplicate republish kept one receipt and one notification. Invalid event version produced FAILED receipt and a DLQ message after retry. |
| AUD-003 | **FIXED** | Added `ProfileAssetKind.RESUME`, immutable object metadata/checksum, monotonic `assetVersion`, one active `current` resume, and dedicated upload/list/current/download/delete APIs. Applying without current resume returns stable `APP_010`/400. Application snapshot now stores the actual current resume metadata/reference and version, not profile JSON. Logical resume deletion retains the immutable object needed by historical snapshots. | User V15 applied on PostgreSQL. Runtime verified no-resume 400, v1, v2 becomes current, cross-owner 403, deleted current 404, and Application snapshot `v1` containing storage reference metadata. AI upload/analyze/matching and Ollama flows remained green. |
| AUD-004 | **FIXED** | Added centralized explicit transition policy: `APPLIED -> SCREENING/REJECTED`, `SCREENING -> INTERVIEW/REJECTED`, `INTERVIEW -> OFFER/REJECTED`, `OFFER -> HIRED/REJECTED`; terminal, same-state, backward, arbitrary, and employer-withdraw transitions are rejected. Candidate withdraw is permitted only from non-terminal workflow states. | Exhaustive enum-pair unit matrix passed; real E2E accepted APPLIED -> SCREENING, rejected SCREENING -> APPLIED, denied cross-owner withdraw, and accepted owner withdraw. |
| AUD-005 | **FIXED** | Centralized all private nested-resource read enforcement through `ProfileService.assertProfileOwner`, retaining ADMIN bypass. Education, Experience, Skill, Language, Certificate, Social Link, Career Objective, Candidate Preference, and Profile Asset/Resume reads are now owner/admin only; existing write ownership checks remain enforced. Recruitment-required Application access remains through candidate-owned current-resume fetch and Application authorization. | Integration matrix covers owner 200, other candidate 403, employer 403, and admin 200 across all nine resource families. Real E2E repeated asset/resume/application cross-owner tests. |
| AUD-006 | **FIXED** | Added migration-safe V7 that randomizes and disables the legacy V5 seeded credential and revokes its refresh tokens. Added opt-in environment-driven secure admin bootstrap; default/production behavior is disabled and requires an explicit email plus password of at least 12 characters when enabled. | V5 checksum/history was untouched; PostgreSQL auth V8 is current; legacy account was verified disabled. Test context verified bootstrap absent by default. Runtime admin authorization was exercised with ephemeral test setup only, then removed. |
| AUD-007 | **FIXED** | Added SHA-256 refresh-token hashing. API clients still receive raw tokens, while DB lookup/save/rotation/logout use only the 64-character hash. V8 renames the storage column to `token_hash` and hashes already-issued rows in place. | PostgreSQL schema contains `token_hash` and no `token`; runtime DB check showed every stored token matched 64 lowercase hex; fresh raw token refresh/rotation/logout passed; automated integration test confirms raw value differs from stored value. |

## 30 Important Files and Migrations

- **Application/outbox/state/resume integration:** `backend/application-service/src/main/java/com/recruitment/application/outbox/`, `ApplicationServiceImpl.java`, `ApplicationStatusTransitionPolicy.java`, `UserClientImpl.java`, and `V2__create_application_outbox.sql`.
- **Notification delivery:** `NotificationAmqpConfig.java`, `NotificationEventConsumer.java`, `NotificationEventHandlerImpl.java`, `NotificationFailureRecorder.java`, and `V2__add_notification_event_version.sql`.
- **User authorization/resume:** the nine affected User service classes, `ProfileAssetService.java`, `ResumeController.java`, `ProfileAsset.java`, `ProfileAssetRepository.java`, and `V15__add_versioned_current_resumes.sql`.
- **Auth hardening:** `RefreshTokenHasher.java`, `AuthenticationService.java`, `SecureAdminBootstrap.java`, `V7__disable_legacy_seed_admin.sql`, and `V8__hash_refresh_tokens.sql`.
- **Acceptance compatibility:** `JobServiceImpl.java` now returns an active DRAFT to its company owner/admin while continuing to hide it from public/unrelated users.

## 31 Build and Automated Test Evidence

- `mvn clean verify` from `backend/`: **BUILD SUCCESS**, all eight reactor modules successful.
- Final `mvn verify` after the Rabbit topology compatibility correction and DRAFT-owner test: **BUILD SUCCESS**, all eight reactor modules successful.
- Surefire aggregate: **77 tests, 0 failures, 0 errors, 0 skipped**.
- New/expanded coverage includes:
  - complete nested-profile owner/other-candidate/employer/admin read matrix;
  - resume v1/v2/current/inactive/admin/cross-owner behavior;
  - no-current-resume Application failure and real resume snapshot assertion;
  - exhaustive Application transition matrix and lifecycle authorization;
  - refresh token hash persistence, rotation, logout, and bootstrap-default-off;
  - Notification processed-event idempotency;
  - owner-visible versus unrelated-hidden DRAFT job.
- `git diff --check`: no whitespace errors.

## 32 Real Infrastructure and Migration Evidence

- Existing Docker containers remained healthy: PostgreSQL 17, Redis 8, RabbitMQ 4 Management, and MinIO.
- All seven Spring Boot services started on ports 8081-8087 from the hardened JARs. Auth/User/Company/Notification/AI health endpoints returned 200; Recruitment public jobs returned 200; Application correctly returned 401 on an unauthenticated protected endpoint. The known inconsistent `/api/v1/health` behavior in Recruitment/Application remains tracked by out-of-scope `AUD-018`.
- Applied migration heads on real PostgreSQL:
  - Auth: **V8**
  - User: **V15**
  - Application: **V2**
  - Notification: **V2**
- Rabbit topology after startup:
  - `notification-service.events`: durable, one active consumer, zero pending messages after E2E;
  - `notification-service.events.dlq`: durable; invalid-contract probe reached it after listener retries;
  - the probe message was purged after evidence capture.
- An existing queue initially rejected an added dead-letter routing argument as inequivalent. The final configuration was made migration-safe by retaining its deployed DLX arguments and binding the DLQ with `#`; restart then succeeded against the existing broker topology.

## 33 Full Runtime E2E Result

The runtime suite used distinct Candidate A, Candidate B, Employer A, Employer B, and Admin identities and exercised the real services directly:

1. registration/login/profile initialization: **PASS**;
2. Employer A/B company creation: **PASS**;
3. Employer A DRAFT job create and own GET: **PASS**;
4. Employer B DRAFT GET hidden and publish denied: **PASS**;
5. Employer A publish: **PASS**;
6. Candidate A apply without resume -> `APP_010`/400: **PASS**;
7. resume upload/ownership and Application apply with immutable snapshot: **PASS**;
8. Candidate B and Employer B Application IDOR denial: **PASS**;
9. Employer A application list and valid status transition: **PASS**;
10. backward transition and cross-owner withdraw rejection: **PASS**;
11. Candidate A own withdraw and Admin bypass: **PASS**;
12. Rabbit notifications to correct candidate/employer recipients: **PASS**;
13. duplicate event idempotency plus retry/FAILED receipt/DLQ: **PASS**;
14. raw refresh token -> hashed lookup -> rotation -> logout: **PASS**;
15. resume v1 -> v2/current -> cross-owner denied -> delete current/inactive 404: **PASS**.

## 34 AI Regression on Real Dependencies

- TXT resume upload to real MinIO: **PASS**.
- Resume analysis and structured persistence on real PostgreSQL: **PASS**.
- Deterministic rule-based job match against a real published job: **PASS**.
- Ollama match explanation: **PASS**.
- Ollama interview preparation: **PASS**.
- Ollama candidate assistant (`RESUME_IMPROVEMENT`): **PASS**.
- AI service health reported `status=UP`, `phase=COMPLETE`, and `aiProviderAvailable=true`.

## 35 Cleanup and Remaining Work

- Removed 20 ephemeral runtime identities plus their exact cross-schema business records and exact MinIO owner prefixes.
- Purged the single deliberate DLQ probe message.
- Stopped the seven temporary service JVMs after final verification; Docker infrastructure was intentionally left running and healthy.
- No secrets, raw refresh tokens, runtime passwords, or private keys are recorded in this report.
- Remaining non-HIGH work is the original MEDIUM/LOW/INFO backlog, except the DRAFT-owner portion of `AUD-008` noted above. No remaining item blocks closure of `AUD-001` through `AUD-007`.
