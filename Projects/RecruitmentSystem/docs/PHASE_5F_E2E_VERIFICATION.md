# Phase 5F E2E verification

Verified on 2026-08-20 against the local development stack. This report intentionally excludes credentials, access tokens, uploaded CV contents, and other runtime secrets.

## Completed phases

| Phase | Capability | Result |
| --- | --- | --- |
| 5F.1 | Owner-scoped employer job list with database pagination, filters, search, and sorting | PASS |
| 5F.2 | Owner-scoped job and application statistics | PASS |
| 5F.3 | Candidate/employer registration whitelist and employer onboarding | PASS |
| 5F.4 | Minimal immutable candidate profile snapshot captured during apply | PASS |
| 5F.5 | Admin user, company, and application management | PASS |
| 5F.6 | One-time password reset and email verification flows | PASS |
| 5F.7 | Read-only cached recommendations plus asynchronous refresh worker | PASS |
| 5F.8 | Full E2E, authorization regression, responsive verification, and final hardening | PASS |

## Runtime health

- PostgreSQL, Redis, RabbitMQ, and MinIO containers: healthy.
- Auth, User, Company, Recruitment, Application, Notification, AI, and API Gateway health endpoints on ports 8080-8087: `UP`.
- Ollama 0.32.14: online with the configured local `Qwen2.5:3B-Instruct` model. No paid provider was called.
- Frontend Vite runtime: reachable on `http://localhost:5173`.

## Quality gates

Each changed backend service passed `mvn clean compile`, `mvn clean test`, and `mvn clean verify` before its phase checkpoint:

- 5F.1: Recruitment Service.
- 5F.2: Recruitment Service and Application Service.
- 5F.3: Auth Service.
- 5F.4: Application Service, including PostgreSQL migration verification.
- 5F.5: Auth, Company, Application, and API Gateway.
- 5F.6: Auth Service and API Gateway.
- 5F.7: AI Service and Recruitment Service.

Final frontend gates passed:

- `npm run lint`
- `npm run build`

The production build reports only the existing non-fatal JavaScript chunk-size advisory.

## E2E evidence

A controlled candidate and employer flow was completed through the API Gateway and the real service stack:

1. Candidate registration/login, profile creation, resume upload, AI resume analysis, job matching, application creation, resume/job/candidate snapshots, and notification retrieval.
2. Employer registration/login, authenticated company onboarding with owner derived from JWT, draft job creation, publication, owner-scoped job/application queries, resume snapshot download, status transition, and statistics.
3. Candidate observed the changed application status and corresponding notification.
4. Admin login and real Users, Companies, Applications, Catalog, Notifications, Templates, Delivery Logs, and AI Provider queries.
5. AI recommendation refresh returned `202`, completed asynchronously through RabbitMQ, persisted results, and served the candidate from the read-only recommendation endpoint/cache.

The final read-only API smoke suite passed 20/20 checks. It covered role-specific `auth/me`, candidate profile/applications/notifications/AI, employer jobs/applications/statistics, admin users/companies/applications/provider, unauthenticated `401`, and cross-role `403` responses. Earlier controlled IDOR checks also verified cross-employer job/application denial, cross-candidate AI resume denial, and successful explicit admin access.

## Frontend verification

All browser requests used the frontend configured API Gateway endpoint on port 8080. Source and runtime checks found no direct browser calls to ports 8081-8087 or Ollama 11434.

Exact viewport overrides were used for 360, 390, 768, 1024, 1280, and 1440 pixels:

- Public, Jobs, Companies, Login, Register: 30/30 route/viewport checks passed.
- Candidate dashboard, profile, resumes, applications, notifications, AI Career: 36/36 passed.
- Employer dashboard, company, jobs, applications: 24/24 passed.
- Admin dashboard, users, companies, applications, AI Provider: 30/30 passed.

All 120 checks rendered the expected protected/public route without horizontal document overflow. Browser console error/warning checks were clean. RoleGuard also redirected an authenticated employer away from Candidate and Admin routes.

## Migrations

- Application Service `V3__create_candidate_profile_snapshots.sql`.
- Auth Service `V9__create_account_action_tokens.sql`.

Both migrations were additive; no existing migration or Flyway history was edited.

## Known limitations and deferred scope

- Production email delivery is not configured. Development token delivery is profile-gated; the production adapter fails safely until a real provider is supplied.
- Saved Jobs/Favorites remains P2 and was not implemented before core E2E completion.
- WebSocket notifications, real-time recommendation streaming, API Gateway business aggregation, and bulk synchronous LLM generation remain explicitly deferred.
- The frontend production bundle is functional but the main JavaScript chunk remains a future code-splitting optimization.

