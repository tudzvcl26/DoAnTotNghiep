# API Gateway Implementation Report

Date: 2026-08-09
Repository: `D:\DoAnTotNghiep\Projects\RecruitmentSystem`
Branch: `main`
Backend baseline: `d70223d`
Freeze checkpoint: `e6a0b4a`

## Result

The reactive API Gateway is implemented as a standalone Java 21/Spring Boot 3.5.4 project under `backend/api-gateway`. It uses Spring Cloud 2025.0.3 and Spring Cloud Gateway Server WebFlux 4.3.5, listens on port 8080, and has no database, JPA, JDBC, repository, service discovery, common library, or business controller.

All seven frozen service source/configuration/migration areas remained unchanged. The backend parent reactor also remained unchanged so its regression command continues to represent exactly the frozen seven-service baseline.

## Implemented behavior

- Static environment-configurable routing to Auth, User, Company, Recruitment, Application, Notification, and AI.
- Specific Application Service route for `GET /api/v1/jobs/{jobId}/applications` ordered ahead of the general Recruitment `/jobs/**` route.
- JWT signature/expiry/access-token validation compatible with the backend JJWT implementation and shared Base64 `JWT_SECRET`.
- Method-aware public allowlist derived from current controllers and `SecurityConfig` files.
- Backend role and ownership authorization preserved; the Gateway does not duplicate `@PreAuthorize` or ownership rules.
- Central CORS configuration with explicit origins, methods, headers, exposed correlation header, and wildcard/credentials validation.
- Generated or preserved `X-Correlation-ID` on request and response.
- Safe metadata-only request logs with no request body or sensitive headers.
- Configurable connect and response timeouts. Default response timeout is 120 seconds to accommodate local Ollama generation while remaining bounded.
- Stable gateway-owned JSON errors: 401 invalid/missing token, 504 timeout, 503 unavailable upstream, and 502 malformed/premature upstream failures.
- Backend response statuses and bodies are passed through when the upstream responds.
- Gateway health contract and restricted Actuator health exposure.
- Multi-stage Java 21 Docker image running as a non-root user.

## Automated verification

Gateway command:

```text
cd backend/api-gateway
mvn --batch-mode clean verify
```

Result: PASS, 13 tests, 0 failures, 0 errors, 0 skipped.

Coverage includes application startup, health, public auth, missing/invalid/tampered/expired/refresh JWT rejection, valid JWT forwarding, all seven routes, route precedence, CORS preflight, correlation generation/preservation, backend 400/401/403/404/409/500 preservation, Gateway 504, upstream 503, and absence of Authorization/token values in request logs.

Frozen backend regression command:

```text
cd backend
mvn clean verify --batch-mode
```

Result: PASS. Auth, User, Company, Recruitment, Application, Notification, and AI all succeeded; total reactor time 03:56. Existing tests were not disabled or changed.

Docker command:

```text
docker build -t recruitment-api-gateway:local backend/api-gateway
```

Result: PASS. The first attempt encountered incomplete Maven Central downloads; an unchanged retry used cached layers, completed dependency downloads, compiled the project, and produced the image successfully.

## Real runtime E2E through port 8080

Eight JVMs were started from verified JARs. Readiness for backend ports 8081–8087 was checked only from listening socket state; every business HTTP request used `http://localhost:8080`.

Verified flow:

1. Gateway and Actuator health.
2. Candidate, second candidate, and employer registration/login/current-user authentication.
3. Candidate profile initialization and retrieval.
4. User resume upload to real MinIO and current-resume behavior.
5. Cross-candidate resume access rejected with 403.
6. Employer company creation; Candidate company creation rejected by backend with 403.
7. Employer job creation and publication; anonymous public job search.
8. Candidate application with immutable current resume snapshot.
9. Employer application listing through the specific Application route and APPLIED → SCREENING transition.
10. Other Candidate application access rejected with 403.
11. Application outbox → RabbitMQ → Notification consumer → Candidate notification visible through Gateway.
12. AI TXT resume upload to MinIO, deterministic analysis, deterministic job matching, and Ollama explanation generation.
13. Missing and tampered JWT rejection at the Gateway.

The initial 30-second Gateway timeout correctly returned a stable 504 during local Ollama generation. This exposed a real Gateway configuration mismatch; the default was changed to 120 seconds, rebuilt, restarted, and the same explanation completed through port 8080 in approximately 53 seconds. The timeout remains environment-configurable and the automated 504 test remains green.

E2E result: PASS.

After verification, both exact MinIO test objects and all test records identified by the unique `gateway.*.1786278582` prefix were removed. Counts for test users, companies, jobs, categories, and AI resumes were all zero. The eight audit JVMs were stopped. Existing PostgreSQL, RabbitMQ, Redis, and MinIO containers were left running and healthy; Ollama remained listening because it pre-existed the audit.

## Quality and scope checks

- No blocking calls, `Thread.sleep`, `.block()`, JDBC, JPA, Eureka, TODO, FIXME, `System.out`, or `printStackTrace` in Gateway production code.
- No URL is hard-coded in Java; all upstream URLs are configuration properties with local defaults and environment overrides.
- No secret value is committed or documented.
- No retry filter is configured, so POST mutations are not retried.
- Framework-provided hop-by-hop header handling is retained; no duplicate custom implementation was added.
- Swagger aggregation was deliberately omitted rather than generating fake documentation. Direct service Swagger remains untouched.
- No commit or push was performed.

## Remaining issues

None blocking frontend integration. Aggregate Compose wiring is intentionally deferred to the deployment composition step because the frozen infrastructure/Compose files were not modified; the exact service block is documented in `backend/api-gateway/README.md`.

## Acceptance

Gateway acceptance criteria: PASS.
Backend regression: PASS.
Real E2E through Gateway: PASS.
Ready for frontend: YES.
