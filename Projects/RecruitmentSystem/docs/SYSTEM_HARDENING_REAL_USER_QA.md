# Final P2 closure and Git release — 2026-09-04 (authoritative)

**Release gate: PASS.** All safely closable P2 work is complete. Before staging, the project contains **106 modified tracked files and 42 untracked files** (40 untracked entries in default `git status` because directories are collapsed), 0 staged. All 148 individual paths were classified as source, test, migration, configuration or documentation; ignored runtime evidence, Maven targets and `node_modules` are excluded.

## P2 closure

- **Native IME:** the contenteditable editor preserves composition state, ignores IME Enter/key code 229, delays input commits and autosave until `compositionend`, protects text when blur precedes composition end, and retains undo/redo behavior. Automated composition plus completed-Enter regression passes. Physical Windows input cannot be driven honestly by this environment, so the status is **Native physical Windows Telex/VNI: MANUAL VERIFICATION REQUIRED**. The exact Telex/VNI procedure is in `docs/MANUAL_WINDOWS_IME_QA.md`.
- **Bundle:** all public, authentication and Candidate pages now use the same route-level `React.lazy`/`Suspense` pattern already used by Employer/Admin/AI/CV routes. The main production JS chunk fell from **636.46 kB (182.28 kB gzip)** to **318.14 kB (98.97 kB gzip)**. The >500 kB Vite warning is gone; guards, layouts, routes and recovery behavior remain unchanged.
- **Legacy timestamps:** database provenance for old zone-less `TIMESTAMP` values is unknown. No UTC assumption, mass conversion or historical rewrite was made. New application/status events keep explicit `Instant` columns and API fields; the frontend prefers them and falls back to legacy local values. A new integration regression proves a fixed legacy `LocalDateTime` and status-history value remain byte-for-byte equivalent in JSON while all instant fields stay null.
- **Notification localization:** event types (including `JOB_PUBLISHED`), channels, active/inactive/disabled states and common resource metadata now use centralized Vietnamese mappings. Candidate/Employer notifications reuse the same event labels; Candidate/Employer/Admin application status rendering shares the central application map. Raw `ACTIVE`, `INACTIVE` and `DISABLED` presentation in Admin Catalog/Users was removed. Backend enums and stored historical QA text remain unchanged.

## Final regression

| Gate | Result |
|---|---|
| Backend `mvn clean test --batch-mode` | PASS |
| Backend `mvn clean verify --batch-mode` | PASS — 183 tests, 0 failures/errors/skips |
| Application legacy timestamp targeted test | PASS — 4/4 integration tests |
| Gateway `mvn clean verify --batch-mode` | PASS — 17/17 |
| Frontend `npm test` | PASS — 56/56 in 12 files |
| Frontend lint | PASS |
| Frontend production build | PASS — main 318.14 kB; no >500 kB warning |
| Docker Compose | PASS — config valid; application/frontend rebuilt; 14/14 healthy |
| Runtime smoke | PASS — frontend and gateway HTTP 200; lazy-loaded public/login route rendered |
| Security scan | PASS — no JWT literal, OpenAI key, private key or plaintext QA password |
| Production frontend hygiene | PASS — no QA bootstrap/helper, QA password marker or port 8765 reference |
| `git diff --check` | PASS; only platform line-ending notices |

Evidence is retained only under ignored `runtime-logs/st-qa/final-p2-20260904/`. No runtime logs, target output, `node_modules`, cached browser session or temporary bootstrap is intended for staging.

---
# Final release hardening — 2026-09-04 (authoritative)

**STATUS: RELEASE READY.** Không còn P0/P1 sau các bản sửa và vòng kiểm tra cuối trên localhost. Native Windows Telex/VNI vật lý vẫn là **NATIVE IME NOT VERIFIED** vì browser automation hiện tại không cung cấp IME vật lý; synthetic composition, Unicode, autosave, blur, undo/redo và reload đã PASS. Theo release rubric, điều này không chặn phát hành vì môi trường không hỗ trợ phép thử vật lý và không có bằng chứng giả mạo.

Toàn bộ thao tác dùng tài khoản/dữ liệu QA đã được cho phép trên localhost. Không reset, clean, checkout, restore, stash, commit, push, cấp role, đổi mật khẩu, xóa source/test/artifact/volume hoặc truy cập production/public. Working tree cuối là **104 modified + 38 untracked, 0 staged**; tăng từ checkpoint 96 + 36 vì các sửa timezone, localization, Employer notifications và report/evidence cuối, đồng thời giữ nguyên mọi thay đổi đã có. `HEAD` và `origin/main` vẫn là `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`.

## Defects fixed in final hardening

- Resume facts are source-grounded: education, employment grouping, explicit duration, projects, negative skills/certificates, contacts and languages are validated against source evidence. Project text is not promoted to employment and bullet count is not treated as years.
- Candidate assistant, interview, roadmap, learning, certificate, portfolio, job search, resume improvement, candidate chat and recruiter summary now enforce task, audience, Vietnamese, factual grounding and complete actionable structure. The selected CV is authoritative for CV questions.
- Durable AI tasks preserve duplicate-request idempotency, convert stale `RUNNING` work to an explicit retryable failure after restart, and prevent generic fallback prose from being recorded as a successful generation.
- Application detail/summary now expose `updatedAtInstant` derived from the latest status-history instant. This fixed the reproduced 09:30/02:30 mismatch without inventing a timezone for legacy rows.
- Admin notification event/channel/status labels use centralized Vietnamese mappings.
- Employer now has a protected `/employer/notifications` route, header/sidebar access, role-aware application deep links and responsive layout. A newly exposed 768 px header overflow was fixed at the layout breakpoint.
- Plaintext QA password literals were removed from eight retained QA scripts. Runtime session helpers use environment/cached localhost QA sessions; the final credential scan found zero JWT, OpenAI key, private-key or QA-password literal hits.

## Release gate

| Gate | Result | Evidence |
|---|---|---|
| Resume factual grounding | PASS | 10 requested adversarial contracts plus pipeline/matching tests; final real-provider fixture inspection |
| AI task semantics | PASS | Final task-specific candidate/interview/chat/recruiter runs in `ai-results.jsonl` |
| AI Vietnamese | PASS | Response policies and final semantic inspection |
| AI reliability | PASS | 202/polling/idempotency/retry/restart regressions; no stale indefinite `RUNNING` task |
| AI latency | PASS | Deterministic endpoints 31–203 ms; real CPU resume analysis 28.4–69.4 s, bounded and recorded |
| Candidate runtime | PASS | Fresh authenticated navigation, application/CV/AI/notification and persistence checks |
| Employer runtime | PASS | Dashboard/jobs/applications/workflows plus final notification route at four viewports |
| Admin runtime | PASS | 28/28 authorized access matrix and CRUD/runtime checks |
| Responsive 390 | PASS | Authenticated route matrix, dialog checks and Employer notifications |
| Responsive 430 | PASS | Authenticated route matrix, dialog checks and Employer notifications |
| Responsive 768 | PASS | Authenticated route matrix; final Employer header overflow regression closed |
| Responsive 1024 | PASS | Authenticated route matrix, detail routes and Employer notifications |
| CV Builder | PASS | Unicode/long text, blur, undo/redo, autosave, fresh login and reload persistence |
| PDF | PASS | Candidate CV PDF/export validation retained from final QA artifacts |
| Timezone | PASS | New application/status instants end in Z/offset; detail/summary equal latest history; legacy fallback retained |
| Localization | PASS | Requested raw-token scan: 0 rendered hits; remaining identifiers are internal enum/schema/map keys |
| Security | PASS | Anonymous/Candidate/Employer/Admin and owner/foreign access matrix; Admin 28/28 |
| Native IME | NOT VERIFIED | Physical Telex/VNI unavailable; synthetic composition and Unicode lifecycle PASS |
| Backend regression | PASS | Reactor clean test + clean verify: 182 tests, 0 failures/errors/skips; AI 111 |
| Gateway regression | PASS | Clean verify: 17/17 |
| Frontend regression | PASS | 53/53 in 11 files; oxlint clean; production build PASS |
| Docker | PASS | Compose config PASS; affected images rebuilt; 14/14 healthy; frontend/gateway HTTP 200 |

## Real AI and latency evidence

Seven fresh fixtures were exercised with the local Ollama Qwen2.5:3B-Instruct CPU provider: Vietnamese student, explicit Java three-year, frontend, sparse, English, mixed DOCX and PDF. Final reruns pass factual grounding, audience, task fit, Vietnamese output, actionability, absence of raw JSON/QA leakage/hiring-state hallucination and absence of invented university/language/location/project/experience/skill. Initial failed attempts remain in the JSONL as historical evidence; authoritative successful reruns are the later entries. Deterministic final tasks completed in about 31–203 ms. Real provider resume analysis completed in about 28.4–69.4 s on CPU, including a 46.4 s student recheck; these are measured values rather than expanded timeouts.

## Runtime, responsive and workflow evidence

The authenticated matrix now covers **120 route/viewport checks**: the earlier 116 Candidate/Employer/Admin main/detail combinations plus Employer notifications at all four requested sizes. All have no horizontal overflow or clipped interactive control. CV template dialog title/close checks pass 4/4. The 390 px CV editor preserved `Nguyễn Đặng Ánh`, long wrapped text, blur, undo, redo, autosave and reload. Logout, fresh login, refresh and persisted CV data pass. Final browser console inspection on the new Employer route reports zero application errors/warnings.

Both Candidate → Employer terminal workflows pass with synthetic applications: one reaches `HIRED`, one reaches `REJECTED`, with authorized status history and Candidate-visible results. ST-026 runtime used application `e999d4dd-4f68-47db-bea2-19b8ae4c1af2`; applied/changed/detail/summary instants agree and the UI renders the same local time basis.

## Security, Docker and credential evidence

Authorization remains enforced in the owning services. Anonymous private access returns 401, foreign Candidate/Employer access returns 403/404, owners receive 200, non-admin users cannot enter Admin endpoints and the existing QA Admin session can. No role grant was used.

Final Docker state is 14/14 running and healthy after recreating the frontend from its clean final image. Compose configuration, frontend `/` and gateway health pass. The temporary browser-session bootstrap exists only as ignored QA evidence; it is absent from the final frontend image. Persistent volumes and synthetic QA data were retained.

## Remaining limitations (P2)

- Physical Windows Telex/VNI cannot be driven by this automation surface, so native IME remains NOT VERIFIED. No PASS is claimed.
- Vite reports a ~636.46 kB main chunk. This is a performance/code-splitting follow-up and does not affect core behavior, security, data integrity or AI factual correctness.
- Legacy zone-less application timestamps remain untouched because their source timezone is unknown. New events use explicit instants.
- Some legacy synthetic notification payload titles/content are stored in English by the backend; the notification UI chrome, enum labels, states and controls are localized. This does not expose raw enum tokens or affect workflow correctness.

## Final evidence index

- `runtime-logs/st-qa/final-release-20260904/ai-results.jsonl`
- `runtime-logs/st-qa/final-release-20260904/authenticated-responsive-matrix.json`
- `runtime-logs/st-qa/final-release-20260904/employer-notifications-responsive.json`
- `runtime-logs/st-qa/final-release-20260904/timezone-runtime.json`
- `runtime-logs/st-qa/final-release-20260904/localization-scan.json`
- `runtime-logs/st-qa/final-release-20260904/credential-scan.json`
- `runtime-logs/st-qa/final-release-20260904/regression-summary.json`
- `runtime-logs/st-qa/final-release-20260904/docker-health.json`
- `runtime-logs/st-qa/final-release-20260904/backend-clean-test.log`
- `runtime-logs/st-qa/final-release-20260904/backend-clean-verify.log`
- `runtime-logs/st-qa/final-release-20260904/gateway-clean-verify.log`
- `runtime-logs/st-qa/final-release-20260904/frontend-test-final.log`
- `runtime-logs/st-qa/final-release-20260904/frontend-lint-final.log`
- `runtime-logs/st-qa/final-release-20260904/frontend-build-final.log`
- `runtime-logs/st-qa/final-release-20260904/frontend-docker-release-build.log`

---
# Phase continuation — 2026-09-04 (latest authoritative checkpoint)

**RELEASE READINESS: NOT READY.** The implementation and automated regression gates below pass, including source-grounded resume facts, task-specific AI answer contracts, durable AI-task restart handling, explicit instants for new application events, localization, and rebuilt localhost images. Release remains blocked because the changed AI policies have not completed a fresh authenticated real-provider semantic/latency run for every task, and authenticated responsive coverage at all four requested viewports is incomplete. Native Windows IME also remains a stated manual check.

The incoming **72 modified + 32 untracked** checkpoint remains preserved. Current Git state is **96 modified + 36 untracked**, **0 staged**. `HEAD` and local `origin/main` both remain `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`. No reset, checkout, restore, stash, commit, push, production/public access, real account, password change, role change, source deletion or data-volume deletion was used. `git diff --check` exits successfully; only the existing LF/CRLF conversion notices remain.

## Completed hardening in this continuation

- Resume validation now parses explicit source sections, treats labelled education/experience/projects/languages/certificates/achievements as authoritative, keeps one employment block with many bullets as one experience fact, excludes project text from employment evidence, and does not convert entry count into years of experience.
- Candidate assistant answers now have task-specific semantic contracts for Career Roadmap, Learning, Skill Roadmap, Certificate, Portfolio, Job Search and Resume Improvement. Career Roadmap requires complete `0–3 tháng` and `3–6 tháng` phases. Recruiter job summaries have a separate audience/task policy. Internal QA terms, invented hiring state, role confusion and raw embedded JSON are rejected.
- Invalid provider output retries once and then returns a typed retryable failure for runtime candidate/recruiter/career/interview paths instead of recording generic fallback prose as AI success.
- Interview output requires complete Vietnamese structure, question marks and grounding to matched/missing skills; project questions become conditional when the CV has no project evidence.
- On process startup, persisted `RUNNING` AI tasks from a previous process are marked failed/retryable with a clear restart message, while queued `PENDING` work remains available to the worker. Duplicate queued requests continue to reuse the same task.
- Application events now write additive nullable `TIMESTAMP WITH TIME ZONE` columns (`applied_at_instant`, `changed_at_instant`) for new records and expose ISO instants through the API. Legacy zone-less values are deliberately not backfilled without provenance. Frontend application views prefer the instant when present and retain legacy fallback compatibility.
- User-facing application/company/job/planned states on the touched Admin, Employer and AI surfaces use centralized Vietnamese labels. The rebuilt homepage contains `Chưa triển khai` and no raw `PLANNED`, `APPLIED`, `REJECTED` or `HIRED` token.

## AI quality acceptance

| Task | Grounded | Correct audience | Task-specific | Vietnamese | Actionable | Latency | Verdict |
|---|---|---|---|---|---|---|---|
| Resume analysis | PASS in source-contract tests | Candidate | PASS | PASS policy | PASS | Real provider not recertified | PARTIAL runtime |
| Match explanation/recommendation | PASS, deterministic match fields | Candidate | PASS | PASS | PASS | Deterministic | PASS scoped |
| Interview preparation | PASS in sparse/project/skill tests | Candidate | PASS | PASS | PASS | Real provider not recertified | PARTIAL runtime |
| Career Roadmap | PASS contract | Candidate | PASS: two required phases | PASS | PASS | Real provider not recertified | PARTIAL runtime |
| Learning / Skill Roadmap | PASS contract; gaps restricted to match context | Candidate | PASS | PASS | PASS | Real provider not recertified | PARTIAL runtime |
| Certificate / Portfolio / Job Search / Resume Improvement | PASS policy fixtures | Candidate | PASS | PASS | PASS | Real provider not recertified | PARTIAL runtime |
| Recruiter job summary | Job-only evidence policy PASS | Recruiter | PASS | PASS | PASS | Real provider not recertified | PARTIAL runtime |
| Career companion chat | Acceptance/retry/refusal tests PASS | Candidate | PASS | PASS | PASS | Real provider not recertified | PARTIAL runtime |

No row is marked runtime PASS merely because an endpoint can return HTTP 200. The contract suite proves rejection/retry behavior, but a fresh real-provider multi-fixture run is still required to close the release gate.

## Regression and runtime evidence

| Gate | Result |
|---|---|
| Backend reactor `mvn clean test --batch-mode` | PASS — 195 tests total; AI 107/107 |
| Backend reactor `mvn clean verify --batch-mode` | PASS — all seven services/build reactor successful |
| Application PostgreSQL migration test | PASS — Flyway V1–V4 on PostgreSQL 17; both new columns are `timestamp with time zone` |
| Application instant integration | PASS — new apply/status JSON values end in `Z`/explicit offset |
| API Gateway `mvn clean verify --batch-mode` | PASS — 17/17 |
| Frontend `npm test`, lint, build | PASS — 53/53; oxlint clean; production build successful |
| Docker Compose | PASS — config valid; rebuilt local images; 14/14 running and healthy; application schema at V4 |
| Fresh Admin API session | PASS — users, companies, jobs, applications and AI providers each returned 200 through localhost gateway |
| Localization on rebuilt image | PASS for required raw tokens on checked surface |
| Responsive public/login sample | PASS — 390×844, 430×932, 768×1024 and 1024×768; home/jobs/companies/login had no horizontal overflow |
| Responsive authenticated product matrix | PARTIAL — Candidate/Employer/Admin/AI/CV/dialog combinations were not all rerun at all four sizes |
| Native Windows IME | **NATIVE IME NOT VERIFIED** |

The frontend build still reports the existing approximately 635.77 kB main-chunk warning. Legacy application timestamps remain zone-less and intentionally unchanged; a future backfill requires deployment-specific timezone provenance. The local Docker rebuild reused persistent volumes and did not discard QA data.

## Release blockers remaining

1. Run the real provider against the approved Candidate/Employer sessions for every changed semantic task and record groundedness, audience, task fit and latency after this build.
2. Complete authenticated responsive checks for AI, Admin, Employer, Candidate, CV Builder, dialogs, long text, dropdowns, notifications and application detail at all four requested viewports.
3. Perform physical Telex/VNI composition testing on Windows, including composition events, caret, autosave, blur, undo and navigation. Current status must remain **NATIVE IME NOT VERIFIED** until that manual evidence exists.

---
# System hardening / real-user QA — checkpoint 2026-09-03

Baseline: `ca6ade6` (`main`, initially clean and equal to `origin/main`).
Continuation instruction: preserve every existing change; NO commit or push,
even after QA passes, until the user separately requests it. The latest explicit
localhost QA authorization supersedes historical missing-permission notes; no
real accounts, production access or Admin role changes are authorized.
This audit includes the original pre-fix observations below. Historical STATUS
lines describe reproduction, not the current resolution; the resolution table
and the latest continuation checkpoint are authoritative. No commit/push is
authorized, including after regression passes.

## Final localhost continuation — 2026-09-03 (authoritative)

**RELEASE READINESS: NOT READY.** The former positive-Admin-runtime blocker is
closed: a fresh authorized Admin QA login completed the runtime checks below and
the final frontend build retained the Admin session after reload. The release is
still blocked by inconsistent AI roadmap/assistant usefulness and latency, the
remaining source-grounded extraction gaps, and the absence of a native Telex/VNI
composition run. A disclosed fallback or HTTP 200 is not counted as a useful AI
quality pass.

All work stayed on `localhost`. The incoming tree remains preserved; the current
Git state is **72 modified + 32 untracked status entries**, with nothing staged.
HEAD and local `origin/main` remain
`ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`. No reset, clean, stash, checkout,
revert, source/artifact deletion, commit, push, public/production access, real
account, role grant, password reset or password change was used.

### Admin runtime acceptance

- Fresh login reached `/admin`; refresh and a second fresh login retained the
  authorized Admin shell. Logout worked (landing at `/`, rather than `/login`).
- Dashboard, users, companies, jobs, applications and AI-provider pages loaded
  without unexpected 401/403. Users pagination advanced to page 2 and back; jobs
  and applications exposed two pages. AI provider reported ONLINE with the local
  Ollama deployment.
- Category, skill and benefit forms returned field-level required errors for
  empty input. Unique synthetic records created successfully; duplicate slugs
  returned domain conflicts instead of 500. Category/skill/benefit edits survived
  reload. Category and skill deactivation paths were exercised. After the final
  assertions, the remaining QA benefit and route-regression category were also
  soft-deactivated through their localhost API; no production or non-QA row was
  changed.
- A synthetic notification template was created, updated, reloaded and disabled.
  The event/channel uniqueness conflict returned a controlled error. Delivery-log
  filter/empty state loaded; pagination could not be exercised because the local
  table had zero records. No notification was sent merely to manufacture a log.
- Desktop and 390x844/768x1024 Admin samples had no horizontal page overflow.
  Mobile application cards were readable. Raw `APPLIED`/`REJECTED`/`HIRED` enums
  remain a P2 localization issue. Final browser console inspection returned no
  warnings or errors.
- A real route-state defect was found: the reusable catalog component retained
  the prior category success status after navigating to Skills. `AdminCatalogPage`
  now resets editing/form/feedback state when `kind` changes. Its regression test
  failed against the old behavior and passes after the fix. On the deployed build,
  a fresh synthetic category produced success feedback; direct navigation to
  Skills showed the correct heading and no stale category message. Reload retained
  the Admin session.

### AI explanation quality repair and runtime result

- The interrupted real-provider quality run reproduced a P0 counterexample: the
  student explanation advised gaining software-testing experience despite the CV
  containing only a Java library coursework project, and mixed an English `Study`
  instruction into Vietnamese. That explanation took about 81.5 seconds.
- Match explanations now compose only the authoritative versioned rule-match
  result and published job title. Generated prose cannot enter the persisted
  explanation. Missing skills receive explicit high-priority evidence gaps;
  recommendations say the skills are not evidenced and must only be added after
  real proof. Provider/model metadata is
  `deterministic-grounded/grounded-explanation-v1` with zero tokens.
- The focused deployed localhost run created a new match, queued its explanation,
  reached COMPLETED and retrieved the result in about 1.3 seconds. It contained
  three grounded gaps and neither the software-testing-role hallucination nor the
  mixed English instruction. Targeted tests pass 9/9; the complete AI service
  suite passes 97/97.
- The broader quality script was interrupted when the local Docker stack
  unexpectedly restarted during the Frontend case. The stack recovered to 14/14
  healthy. Evidence before interruption remains valid and is retained; the run is
  not represented as a complete multi-fixture pass. Java Career Roadmap still
  reached a safe fallback after about 188 seconds and Skill Roadmap returned a
  retryable 503 after about 59 seconds. These remain release-blocking usefulness
  and reliability failures.

### Final acceptance matrix

| Module | Automated | Runtime | Visual | Security | Verdict |
|---|---|---|---|---|---|
| Auth | PASS | Fresh Candidate/Employer/Admin sessions; refresh/logout checked | PASS sampled | Negative role/session probes PASS | PASS |
| Candidate | PASS | Core profile/preferences/applications and terminal statuses passed | Desktop/mobile sampled | Ownership checks PASS | PASS, sampled |
| Employer | PASS | Jobs/applications and HIRED/REJECTED workflow passed | Desktop sampled | Company/owner checks PASS | PASS, sampled |
| Admin | PASS | Dashboard, lists, pagination and catalog/template CRUD passed | Desktop + 390/768 samples pass | 21/21 negative guards plus positive Admin pages | PASS, except delivery-log pagination |
| CV Builder | PASS | Save/reload/draft/composition-event flows passed | 24 templates previously sampled | Owner isolation PASS | PARTIAL: native OS IME unverified |
| CV PDF | PASS | Export/structure tests pass | Four exports/seven rendered pages previously inspected | Auth/owner probes PASS | PASS, sampled |
| AI Resume Analysis | PASS tests | Real provider remains nondeterministic on some extracted facts | UI sampled | Owner/role probes PASS | FAIL quality |
| AI Matching | PASS | Deterministic scores/explanation retrieval pass | UI sampled | Owner/role probes PASS | PASS, scoped grounding |
| AI Interview | PASS | Student/Java grounded samples; sparse fallback remains generic | UI sampled | Owner/role probes PASS | PARTIAL |
| AI Roadmap | PASS guard tests | Safe fallback/503 and long latency reproduced | UI sampled | Candidate task policy PASS | FAIL |
| Application | PASS | APPLIED through HIRED plus separate REJECTED branch pass | Candidate mobile/Admin mobile sampled | 22/22 workflow/ownership matrix PASS | PASS |
| Notification | PASS | Template CRUD and empty delivery-log state pass | Admin list/forms sampled | Admin guards PASS | PARTIAL: no delivery rows/pagination |
| Gateway | 17/17 PASS | Local routes and recovery checks pass | N/A | CORS/status/redaction/role gates PASS | PASS |
| Responsive | PASS targeted tests | Candidate/Admin 390 and Admin 768 samples pass | No sampled overflow | N/A | PARTIAL across full product |

### Final regression and residual risk

| Gate | Result |
|---|---|
| Backend reactor `clean test` | PASS — 168 tests, zero failures/errors/skips |
| Backend reactor `verify` | PASS — 168 tests, build success |
| AI service focused/full | PASS — 9/9 focused; 97/97 full |
| API Gateway `clean verify` | PASS — 17/17 tests |
| Frontend | PASS — 53/53 tests; oxlint and production build pass |
| Docker | PASS — Compose config valid; 14/14 running and healthy |
| Browser post-deploy | PASS — fresh Admin login, reload persistence, route-state retest, empty console |

The production build retains the 635.63 kB main-chunk warning. Deferred product
risks include ST-033 source-grounded extraction fidelity, ST-035 task-specific
assistant usefulness, ST-041 provider failure/fallback behavior, timezone contract
ST-026, partial localization ST-027, full notification-delivery pagination,
complete responsive/modal coverage and native Windows IME. The deterministic
explanation fix closes the reproduced prose hallucination for that surface; it
does not certify semantic entailment for resume extraction, interviews, chat or
roadmaps.

## Previous localhost continuation — 2026-09-02 (historical checkpoint)

**RELEASE READINESS: NOT READY.** Candidate and Employer positive runtime paths,
the two terminal application branches, AI resume grounding and deterministic job
recommendations pass the scoped checks below. Release remains blocked because no
usable pre-existing Admin QA browser session or credential was available for
positive Admin runtime E2E. Native Telex/VNI composition, general semantic
entailment for every assistant surface and the already-listed open reliability/UI
findings also remain outside a complete release acceptance.

Incoming **67 modified + 28 untracked** remains preserved; current state is **71
modified + 31 untracked**, with nothing staged. HEAD and local `origin/main` are
still `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`. No reset, checkout, clean, stash,
commit, push, production/public access, real account, role grant or password change
was used.

### P0 AI grounding fixes and real-provider acceptance

- `ResumeAnalysisJsonValidator` now requires positive source evidence for skills
  and every factual array (`education`, `experience`, `projects`, `languages`,
  `certificates`, `achievements`). A textual object is retained only when all of
  its textual leaves are supported. Explicitly labelled source facts are
  authoritative, so a model paraphrase cannot add a second education/experience
  row or inflate the quality score.
- The reproduced Java CV hallucination is now removed after a real localhost
  Ollama analysis: no invented university, AWS certificate or language; education
  is exactly `Cử nhân CNTT 2019-2023.`, experience is exactly the source-labelled
  three-year `07/2023-07/2026` record, and the source project remains. Analysis
  completed in about 73.8 seconds while the UI showed the background state and
  prevented duplicate submission. All remaining extracted skill phrases occur in
  the source; canonical alias deduplication can still be improved.
- Job and candidate recommendation prose now comes from
  `GroundedRecommendationComposer`, using only versioned `JobMatchResult` fields.
  It labels rule result, evidence, unverified gaps and recommendation, and never
  consumes free-form CV/JD text or noisy missing-keyword prose. Persisted runtime
  rows identify `provider=deterministic-grounded`,
  `model=grounded-recommendation-v1`, zero prompt/completion tokens. The policy
  version is included in the checksum, invalidating the old hallucinated cache.
- A fresh Candidate login refreshed three job recommendations from PENDING to
  COMPLETED in about four seconds. The deployed UI showed only supported evidence;
  the QA route-B reason cited Java only and contained no synthetic QA fixture token.
  AI-service restart preserved the login session and the completed workflow data.
- A fresh Employer login reached the owner-scoped dashboard for
  `ST QA Employer A 20260830`: five jobs and four applications, including two
  HIRED and one REJECTED, remained persisted after the AI deployment. Backend
  candidate-recommendation endpoints exist and have integration coverage, but the
  frontend exposes no Employer recommendation route/component; therefore no
  positive Employer recommendation **UI E2E** is claimed.

### Final regression and environment state

| Gate | Result |
|---|---|
| AI service | PASS — 97/97 tests, zero failures/errors/skips |
| Backend reactor `test` | PASS — 168/168 tests, zero failures/errors/skips |
| Backend reactor `verify` | PASS — 168/168 tests, build success |
| API Gateway `verify` | PASS — 17/17 tests, build success |
| Frontend | PASS — 52/52 tests; oxlint and production build pass |
| Docker | PASS — 14/14 containers running and healthy after deployment |
| Diff hygiene | PASS — `git diff --check`; only existing CRLF conversion warnings |

The production build still warns that the main minified chunk is 635.63 kB; this
is a performance follow-up, not a failed build. Admin negative authorization
coverage remains 21/21 and workflow/ownership coverage remains 22/22 from the
authorized 2026-08-31 matrix. Positive Admin dashboard/moderation/pagination and
Admin responsive/error/loading/empty-state runtime remain blocked without the
approved pre-existing session or credential; the audit did not manufacture one.

## Authorized localhost E2E continuation — 2026-08-31 (current audit)

**RELEASE READINESS: NOT READY.** This checkpoint supersedes earlier blockers about
permission to log in as QA Candidate/Employer. Both approved accounts were used
successfully through fresh browser logins. It does not supersede untested Admin
runtime or unresolved AI semantic/reliability findings. No real accounts, public
servers, password resets, role grants, source deletions, reset, checkout, stash,
staging, commit or push were used.

Incoming **67 modified + 28 untracked** preserved; current **70 modified + 31
untracked**. HEAD/local origin/main remain `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`.
Evidence in this section is under `runtime-logs/st-qa/authorized-final/` unless
otherwise noted. That folder stays ignored; credentials are not report artifacts.

### Current acceptance matrix

| Area | Status | Evidence and scope |
|---|---|---|
| Admin runtime E2E | BLOCKED — credentials/session missing | Existing enabled Admin QA records identified read-only. No logged-in Admin session or known credential was available; user asked for a configured credential file or browser login. No role/password modification. Dashboard, moderation, tables/pagination and Admin mobile/error/loading/empty states remain unverified. |
| Candidate → Employer workflow | PASS — two synthetic branches | Fresh UI logins, two published QA jobs, two applications using existing CV v1, Employer review/status notes, Candidate final states/history and notification deep link. HIRED path has every transition; separate direct REJECTED path persists after refresh. |
| Candidate CV after fresh login | PASS — scoped Unicode edit | Final frontend build: multiline Vietnamese text edited/saved/reloaded, then original experience restored and confirmed saved. Native OS Telex/VNI remains unverified. |
| Route-state regression | PASS | Applied to QA route A then clicked related B in the same SPA. B remains unapplied without reload; two automated cases also cover a late response after navigation. |
| Security/access matrix | PARTIAL overall; verified probes PASS | 21/21 Admin API guards for anonymous/Candidate/Employer; 22/22 workflow/read/CV/terminal-state/foreign-company probes. Fresh UI `/admin` redirects both non-Admin roles. Positive Admin operations remain blocked. |
| AI quality | FAIL / partial fixes | Real local-provider outputs were assessed for facts, audience, actionable guidance and latency, not merely HTTP success. Details below. |
| CV templates / PDFs | PASS — prior sampled scope retained | Earlier 24 individual desktop/mobile previews and four exports/seven rendered PDF pages remain evidence. This continuation does not claim a new exhaustive 24-template run. |
| Responsive | PARTIAL | Fresh Candidate application list and HIRED detail at 390×844 both client/scroll width 375/375. Inspected mobile screenshot shows readable status/history. Admin and every modal remain outside verified scope. |
| Backend regression | PASS | Final root clean test and clean verify each 166 tests, zero failures/errors/skips; separate gateway clean verify 17/17. |
| Frontend regression | PASS | Three default-parallel runs each 52/52, zero failed tests; lint and production build pass. No worker-count workaround. |
| Docker/recovery | PASS health; PARTIAL recovery scope | Final AI/frontend builds deployed; 14/14 healthy at 22:33:34 +07. Compose config valid; frontend and public jobs via gateway return 200. Earlier PostgreSQL/MinIO fingerprint persistence sample retained; in-flight AI auto-resumption remains open. |

### Completed synthetic workflow and access checks

- Employer job `576ea5e0-06ce-430d-8a21-c915af0e41d3`, application
  `51901319-0727-45af-81fb-744b03d0c3f8`: APPLIED → SCREENING → INTERVIEW → OFFER → HIRED.
- Employer job `3ce51cae-3a35-4bb1-8c35-f6d026a6dbce`, application
  `5c4994ae-8169-4482-b595-eb356b506bef`: APPLIED → REJECTED.
- All postings/cover letters/status notes explicitly identify synthetic localhost
  QA. No real hiring outcome was decided. Candidate's fresh session saw both
  terminal states; notification unread count rose 5→10 for five transitions.
  Reading the rejected notification opened its correct application and reduced
  the count 10→9. Both employer CV downloads are PDF and share SHA-256
  `af1afa2238484b855be8ffa3313ea58f0776619b2ea1fbc76f4a3da290cec345`.
- Anonymous application/CV access is 401; Candidate status PATCH is 403.
  Authorized Employer PATCH from either terminal state to SCREENING is 400
  `APP_007`, with final state unchanged. Employer A cannot read another company's
  QA application or its CV (403). These are sampled resource checks, not a claim
  of exhaustive penetration testing.
- Candidate preferences were saved via UI with required fields and recommendation
  consent enabled, then confirmed after reload. This supersedes the earlier 409
  consent blocker. No account privilege was added.
- Runtime route-state repro originally showed “Đã ứng tuyển” on another job after
  successful apply. New QA A/B jobs isolate retest; A application
  `07ccb010-6c77-43c0-8533-1c06fa9c5195` remains APPLIED; B remains unapplied.
- Deployment once left an old SPA requesting an obsolete lazy chunk. The existing
  error boundary displayed its reload action; reload recovered with the session
  retained. This recovery observation does not certify zero deployment disruption.

Evidence: `workflow-{screening,interview,offer,hired,rejected}.txt`,
`candidate-final-statuses.txt`, `notification-deeplink.txt`,
`candidate-consent-persisted.txt`, `cv-unicode-after-refresh.txt`,
`route-A-applied.txt`, `route-B-unapplied-without-reload.txt`,
`admin-role-matrix.json`, `security-workflow.json`. Initial expectation mismatches
and startup 503 responses were retained separately; they were not silently erased.

### Fixes in this continuation and their limits

- **ST-049 / HIGH / FIXED:** JobDetailsPage reused mutation success and dialog state
  between route IDs. A keyed content component remounts per job and isolates late
  callbacks/cache keys. Both regressions failed before the fix and pass after;
  fresh deployed browser A→B retest passes without reload.
- **ST-050 / HIGH / PARTIAL:** Candidate assistant accepts a bounded role/fact
  policy in addition to Vietnamese prose checks. It rejects reproduced hired
  claims, recruiter-directed tasks and “two languages” for Java/Spring Boot;
  Career Roadmap requires 3- and 6-month milestones. A shared two-generation budget
  retries once with the original context/contract, then explicitly reports that
  no verified plan is available. No timeout increase. This is a phrase heuristic,
  not a general hallucination detector, and it may reject benign third-person
  language. Real student roadmap now has both milestones; Java roadmap fell back
  after 215,235 ms and therefore is NOT a successful roadmap.
- **ST-051 / HIGH / PARTIAL:** Interview context retains up to 12 array entries
  instead of dropping all after three. The output policy rejects first-person
  invented achievements and unfinished outlines. Student/Java runtime questions
  now use their real library/order-management projects. Sparse output still used
  a generic fallback; its text was revised to ask conditionally and explicitly
  allow no experience/project evidence. Generic fallback is not personalized AI
  success and response metadata still lacks an explicit fallback flag.
- **ST-052 / HIGH / PARTIAL:** Explicit source-labelled projects are recovered if
  the model leaves `projects` empty. Basic Java canonicalizes to Java for matching
  while retaining “cơ bản” in displayed facts. Explicit labelled employment
  duration is preserved verbatim when generation drops it; no calendar-duration
  calculation or education-year inference was added. Runtime student Java and
  Java project recovery passed. A later Java response still omitted its 3-year
  phrase, motivating the final duration repair and targeted retest below.
- **ST-053 / HIGH / FIXED IN CODE, runtime assessment below:** Career chat with a
  selected CV uses that analysis as its candidate fact source, excluding unrelated
  account profile/skills/application history. Both repair paths retain the original
  selected-CV prompt. Regression covers contradictory account history and retry.
  This controls context mixing, not whether every generated statement is true.
- **ST-047 / runtime recheck PASS:** Frontend CV matching recognizes 2 years,
  no longer interprets “Tháng 7 năm 2024” as 7 years. ST-044 contact/negative-skill
  checks also remain effective in the new samples: no invented contact URLs and
  AWS/Kafka/Kubernetes are not positive Java CV skills.

### Real-provider results (current continuation)

Provider remains localhost Ollama `Qwen2.5:3B-Instruct` on CPU. No provider
configuration, time budget or worker limit was increased to manufacture a pass.
Concurrent compilation may affect the observed timings; this is acceptance
sampling, not an isolated performance benchmark.

| Case | Request / generation ms | Semantic assessment |
|---|---:|---|
| Student analysis + matching | 86,765 / 86,239 | PARTIAL: contacts null, real library project preserved and basic Java matched; name is also misclassified as an education entry. |
| Java analysis + matching before duration repair | 71,829 / 71,681 | PARTIAL: real project recovered and negated cloud skills excluded, but “3 years” was omitted and language proficiency inferred without source evidence. Final duration retest below. |
| Frontend analysis + matching | 117,234 / 117,187 | PARTIAL: 2 years now scored correctly, but an unsupported July graduation month appears; ordinary English words “forms/navigation” remain in prose. |
| Student interview | result GET 32 / 69,397 | PARTIAL: real library project, neutral complete outlines. Rubric/category alignment and relevance remain weak (e.g. Git question labelled HR). |
| Sparse interview before final fallback wording | result GET 16 / 86,262 | FAIL: generic fallback after one retry, still presumes experience/project in question wording. Final conditional fallback retest below. |
| Java interview | result GET 31 / 39,099 | PARTIAL: order-management project retained; no invented first-person accomplishments in the returned outline. Category fit is weak. |
| Student Career Roadmap | 92,859 / 92,668 | PARTIAL: 3-/6-month milestones present, no hired claim; some existing Java/Git knowledge is redundantly framed as needing evidence. |
| Java Career Roadmap | 215,235 / 215,317 | FAIL: one retry then explicitly disclosed no verified plan. |
| Java Skill Roadmap | client timed out at 240,015 / server 295,772 | FAIL: server log later confirms retry + fallback; the caller did not receive useful output within its wait. No second client generation request was issued. |
| Frontend Skill Roadmap | 158,516 / 158,329 | FAIL: hired/wrong-recipient claims absent, but risk text falsely says JD does not require Java/Spring Boot while those are explicit requirements. “job description” remains ordinary English prose. |

Candidate chat recheck: HTTP 200 in 40,172 ms, generation 39,858 ms, zero
correction attempts. It used the selected Java CV's order-management project and
skills without importing the account's different name or hiring history. Advice
is still generic and redundantly suggests learning Git/JUnit already in the CV;
context-isolation check passes, personalized coaching remains PARTIAL.
Frontend explanation: HTTP 200 in 53,172 ms (generation 53,047 ms), correct 2-year
rationale and unchanged deterministic match score. Advice is conditional but
shallow, with “workshop” retained in Vietnamese prose. PARTIAL.
The three recommendation GETs returned empty persisted lists (47/46/47 ms);
these are not generation-quality passes. Explicit refresh is assessed below.

Recruiter SUMMARIZE_JOB: HTTP 200 in 40,156 ms (generation 40,110 ms).
Mentions the synthetic nature and supported Java/Spring/PostgreSQL skills, but
starts with Candidate-style “Ứng tuyển…” wording and gives screening advice
beyond a concise job summary. PARTIAL. The fixture's experience-level enum is
NO_EXPERIENCE while its requirement prose includes a three-year phrase; this
fixture inconsistency prevents a clean judgment of the “no years required” claim.

Final deployed Java duration re-analysis: HTTP 200 in 56,016 ms, generation
55,769 ms. Source-labelled 3-year line and original order-management project
are retained; matching explicitly recognizes 3 years. This scoped repair PASSES.
The same output invents “Trường Đại học Công nghệ thông tin” and both Vietnamese/
English proficiency; neither is in the source CV. One job remains split into
four experience strings, and quality scoring still rewards entry count. Thus
analysis as a whole remains FAIL and nondeterministic extraction affects skills/
scores. No general factual-accuracy claim is made.

Final sparse interview: 108,114 ms generation, result GET 15 ms. The deployed
fallback now asks conditional questions and explicitly allows absent projects,
work experience and practice evidence. No made-up coursework or first-person
achievements appear in that response. Fallback wording safety PASSES; useful
personalized AI generation remains PARTIAL, with no explicit fallback metadata.
Recommendation refresh: HTTP 202 in 78 ms; duplicate POST 47 ms returns the
same task `d1af3540-bbdb-4cf1-adca-735acece2bd1`. It completed in approximately
158.93 seconds with progress 100 and `recommendations=3`, observed at poll 32
(5-second polling). Stored rows score 95, 95 and 85; their generation durations
are 51,918 / 43,400 / 62,646 ms. Final GET is 94 ms. Provider log has three
successful generation calls and no recommendation retry/fallback for this run.
Fresh Candidate browser session displays all three cards and “Hoàn tất”.
Queue/dedup/storage/UI integration PASSES; semantic quality FAILS: advice asks
for QA tokens “phase/published/controlled/runtime/verification” and
“local/route/regression”, and the route-B recommendation invents Git/JUnit/
PostgreSQL requirements although that synthetic job's only skill requirement is
Java. Resume facts are being presented as job requirements. A successful task
status is not semantic acceptance.

After the final deployment a second fresh Candidate browser login succeeded;
its AI screen fetched the new Java analysis (55.8 seconds, preserved 3 years)
and the completed recommendations. `access-final.log` also confirms all 43
scoped access probes with new API logins on the final deployment.

Evidence index: `ai-results.jsonl` retains every response, error and task poll;
`quality-recheck.log` and `last-runtime.log` retain timing/status summaries;
`ai-provider-before-final-deploy.log` and `ai-provider-final.log` record retries,
fallbacks and provider usage. `regression-summary.json`, `deployment-health.json`
and `git-audit.json` record final verification. Browser evidence includes
`candidate-final-build-new-login.txt`, `ai-career-final-build-new-login.txt`,
and `recommendations-final-ui.txt`.

Final regression commands: root `mvn clean test` and `mvn clean verify` each
166 tests with zero failures/errors/skips; gateway separate `mvn clean verify`
17 tests; frontend three `npm test -- --run` suites each 52 tests, `npm run lint`
and `npm run build`. See `root-{test,verify}-final.log`, `gateway-verify.log`,
`frontend-test-{1,2,3}.log`, `frontend-lint.log`, `frontend-build.log`.
`git diff --check` passes, nothing staged; every path in the prior final-status
manifest exists. One generated `.pyc` file was moved into ignored QA evidence,
not deleted and not included as a source change.
The bounded acceptance policies do **not** establish semantic entailment: the
Frontend JD contradiction passed them. This is a concrete release-blocking
counterexample, not a reason to label every HTTP 200 response successful.

### Remaining release gates

1. Obtain a valid existing Admin QA session/credential, then complete Admin runtime
   and positive Admin security rows. Enabled account discovery is not login proof.
2. Resolve remaining extraction/semantic quality errors and reliable useful output
   for sparse/English profiles. A disclosed fallback prevents misleading advice
   but does not fulfill the requested AI feature.
3. Native Telex/VNI caret/composition QA and untested Admin/mobile/error states.
4. In-flight AI tasks after restart currently expire as retryable FAILED instead
   of automatically resuming; prior recovery evidence remains a limitation.
5. Existing timezone/raw-enum localization and irrelevant keyword suggestions
   remain open. Synthetic job markers must not become CV advice.

No release approval, commit or push is implied by the passing regression suites.

## Earlier final product QA continuation — 2026-08-31 (historical checkpoint)

**RELEASE READINESS: NOT READY.** This section supersedes historical PASS claims
only for the explicitly retested scope below. The available runtime AI run and
final regression/recovery checks are complete; blocked journeys remain incomplete.
Do not interpret automated regression success as product sign-off.
No reset, checkout, stash, staging, commit, push or source/artifact deletion.
HEAD and local origin/main remain `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`.

### Acceptance matrix

| Area | Status | Current evidence and limits |
|---|---|---|
| AI Quality Review | FAIL | Fresh synthetic student, Java 3-year, Frontend, sparse, English student, DOCX and PDF uploads. Hallucinated contact/negative skills reproduced and corrected; broader semantics still fail. |
| Admin Runtime | NOT VERIFIED | No active authorized Admin session. Dashboard, tables, pagination, moderation, loading/error/empty/mobile states cannot be signed off. |
| Candidate E2E | PARTIAL | Login, profile, CV inline edit/save/refresh, 24 switches, PDF trigger, saved analysis/match/explanation/interview UI observed before session ended. Fresh apply/status/notification round trip not completed. |
| Employer E2E | NOT VERIFIED | Browser rejected fresh Employer login; historical journey remains historical. No alternate login was used to bypass rejection. |
| CV Builder | PASS (sampled scope) | Unicode multiline edit persisted through explicit save and reload; all 24 template switches retained the edited experience. Original description and Developer template restored. |
| 24 Template Visual QA | PASS (sampled CV) | Each template opened separately at desktop 1440×1200 and mobile 390×844, screenshot inspected. No missing CV glyphs, overlapping CV sections or page horizontal overflow observed in this short sample. |
| PDF | PASS (sampled engines) | Four fresh backend exports, seven pages rendered with Poppler and inspected. Single-column 1 page; header/left-sidebar/right-sidebar 2 pages each. Exact Vietnamese name and all QA-01…QA-36 markers present. |
| Vietnamese IME | PARTIAL — AUTOMATION LIMITATION | Source and synthetic composition tests only. Native Telex/VNI, actual IME caret behavior and OS event ordering are not certified. |
| Security Matrix | PARTIAL | Fresh 21/21 anonymous/Candidate/Employer Admin API GET guards; 65 resource GET probes plus six unauthorized mutations. Candidate `/admin` redirected to `/candidate`. Positive Admin and fresh Employer UI guard remain unverified. |
| Responsive | PARTIAL | All 24 short template previews at both dimensions passed width checks (desktop client/scroll 1425, mobile 375). Rebuilt public home mobile also 375/375. Admin and every modal/long-text editor viewport not fully covered. |
| Backend Regression | PASS | Final root clean test and clean verify each 159 tests, zero failures/errors/skips. Gateway separate clean verify 17 tests, zero failures/errors/skips. |
| Frontend Regression | PASS | After final IME fixes: three default-parallel runs, 50/50 each; lint and production build pass. No one-worker workaround. |
| Docker | PASS (sampled recovery), PARTIAL (in-flight task reliability) | Final compose config valid; AI/frontend rebuilt/deployed; 14/14 healthy at 12:25:21 +07. PostgreSQL/MinIO restart retained sample fingerprints; gateway/frontend/public routes return 200. Interrupted AI task becomes retryable FAILED rather than resuming. |

### Fixed defects and remaining AI findings

- **ST-044 / HIGH / FIXED, scoped grounding:** real `java-three-years.txt`
  analysis invented a LinkedIn URL and classified `AWS (không làm việc)`, Kafka
  and Kubernetes as technical skills despite explicit negative source evidence.
  Root cause: JSON/schema/language validation did not verify contact identifiers
  or negated skill claims against source text before scoring. The validator now
  clears unsupported contact/location fields and removes explicitly negated
  skills/keywords unless a separate affirmative source line supports the skill.
  A sentence-final period previously defeated whole-term matching; comparison
  now ignores sentence-final periods while retaining internal dots such as Node.js.
  Targeted grounding/scorer tests 16/16; fresh deployed re-analysis HTTP 200 in
  71,188 ms retains exactly 3 years, null LinkedIn, and excludes AWS/Kafka/Kubernetes.
  This bounded check is not a general semantic entailment verifier. Mixed
  affirmative/negative clauses on one line and paraphrased locations remain limits.
- **ST-045 / MEDIUM / FIXED, autosave composition race:** start editing `Ng`,
  begin composition and advance beyond the 900ms save interval. Before fix,
  the previous timer submitted while the new composition was unfinished.
  The editor now observes composition capture events, cancels/pauses scheduled
  autosave and disables save/export until composition ends. Regression failed
  before fix, then passed without increasing waits or suppressing assertions.
- **ST-046 / MEDIUM / FIXED, blur ordering:** a synthetic blur before
  compositionend committed unfinished `Nguyễ` and released DOM protection.
  EditableText now waits for the final composition event before committing and
  ending the edit; a stale external value cannot overwrite the composing DOM.
  The new test failed before fix and passes in all three final 50-test suites.
  These two fixes do not prove native Telex/VNI PASS. Runtime re-login to the final
  editor build was blocked, so the final blur change has source/unit evidence only.
- **ST-047 / HIGH / FIXED in source/tests, final runtime retest NOT VERIFIED:**
  Frontend CV explicitly says 2 years, but its translated `Tháng 7 năm 2024`
  date was parsed as 7 years and propagated into matching/explanation rationale.
  `MatchingText.explicitYears` now excludes a year-count match immediately
  preceded by Vietnamese `tháng`. A regression covers both an explicit 2-year
  statement plus calendar dates and dates without any experience duration.
  Before fix: score 20 instead of 8 against a 5-year requirement; after fix:
  14 scorer tests pass. Saved historical matches are not automatically rewritten.
- **ST-048 / MEDIUM / FIXED in source/tests, bounded language check:**
  Frontend explanation returned `Study and practice more on Java, Spring Boot,`
  followed by Vietnamese text. The total-language ratio let that English
  instruction pass. The policy now rejects three consecutive known English
  prose tokens while preserving technical names. The exact runtime sentence
  failed its regression before the fix. This is not a complete language detector:
  the sparse explanation also contains `Pelatihan` and remains an open quality
  counterexample. Fresh provider output on the final policy build is NOT VERIFIED.
- **ST-033 / OPEN:** after contact/negative-skill repair, the Java analysis still
  splits one job into multiple experience strings and places its declared order
  management project in experience while `projects` is empty. Student analysis
  invents a location from its university/year phrase; source-location filtering
  addresses future unsupported location strings, but that student was not freshly
  reanalyzed after deployment. `Java cơ bản` is still scored as absent Java by
  exact skill canonicalization. Do not certify complete factual/score accuracy.
- **ST-035 / OPEN:** student Interview gives first-person answer outlines that
  assert RESTful API use absent from the CV, contradicts the actual Java project
  in an HR answer, and produces an unfinished behavioral outline. Student Career
  Roadmap repeats keyword/CV advice including the synthetic date/job marker,
  duplicates the surname (`Nguyễn Minh An An`), and omits 3-/6-month milestones.
  Existing prompt instructions are insufficient; schema/language validation
  does not establish semantic grounding or task completion. Not fixed by the
  contact validator and not marked PASS because HTTP is 200.
  The sparse interview similarly invents basic Java/Spring Boot coursework.
  Student and Frontend Skill Roadmap summaries falsely state that the applicant
  has already been hired; no application/hiring decision is supplied in context.
  Frontend advice even asks the Candidate to send an assignment to the applicant,
  confusing the assistant's audience. Sparse Career Roadmap suggests adding an
  unverified project to the CV instead of first asking for missing evidence.
  These are release blockers, not harmless phrasing issues.
- **ST-041 / OPEN/PARTIAL:** real English student analysis returned 502
  `AI_RESUME_008` after 134,953 ms. Student explanation used a clearly disclosed
  Vietnamese fallback after generation/language retries; deterministic scores
  remained unchanged, but detailed advice is unavailable. The HTTP status alone
  cannot distinguish semantic success. Provider deadline was not increased.
- Existing ST-026 timezone and ST-027 localization remain visible: profile/raw
  status enums, public-job labels and dates are not uniformly Vietnamese. These
  are separate from the AI rationale language requirement.

### Real-provider semantic review and timing

Provider: local Ollama `Qwen2.5:3B-Instruct` on CPU. Seven synthetic uploads:
five TXT profiles, one mixed-language DOCX and one generated PDF. Six initial
analyses succeeded and the English student failed with 502. Java was reanalyzed
once after the grounding fix. The same synthetic published job was used, and it
contains QA/E2E/date tokens; irrelevant keyword advice is a real finding, but this
fixture is not representative of every production job or a model benchmark.

| Input | Analysis request / generation ms | Semantic acceptance |
|---|---:|---|
| Student, Vietnamese | 73,891 / 72,993 | FAIL: invented location; basic Java canonicalization misses a supported skill |
| Java, 3 years | 99,485 / 99,410; recheck 71,188 / 70,634 | PARTIAL: contacts/negative skills corrected; project extraction and quality scoring still wrong |
| Frontend, English | 85,063 / 85,014 | FAIL: invented contact URLs before repair; calendar month becomes 7-year matching claim |
| Sparse, Vietnamese | 29,344 / 29,276 | PARTIAL: absence of experience retained; downstream answers invent knowledge |
| Student, English | 134,953 / unavailable | FAIL: 502 `AI_RESUME_008`; no synthetic fact-extraction fallback substituted |
| Mixed-language DOCX | 61,437 / 61,452 | PARTIAL: upload/extract/analyze/match work; no complete feature journey for this file |
| PDF | 45,453 / 45,403 | PARTIAL: upload/extract/analyze/match work; no complete feature journey for this file |

The small timing discrepancies between request and server duration reflect separate
clocks/instrumentation; values are recorded, not adjusted. Every initial successful
analysis had a successful deterministic matching response. This does not establish
score correctness, given the extraction/canonicalization defects above.

| CV | Explanation generation ms / polls | Interview generation ms / polls | Review |
|---|---:|---:|---|
| Student | 113,940 / 38 | 150,735 / 48 | Explanation PARTIAL: disclosed fallback, no detailed plan. Interview FAIL: unsupported REST API claim and incomplete outline |
| Frontend | 111,496 / 37 | 110,784 / 37 | Explanation FAIL: English prose and stale wrong years. Interview PARTIAL: generic fallback, weak personalization |
| Sparse | 48,718 / 17 | 80,207 / 27 | FAIL: mixed-language advice and invented coursework despite missing facts |
| Java | 91,180 / 30 | 74,429 / 25 | Explanation PARTIAL: disclosed fallback. Interview FAIL: substitutes document-management project for order-management project and leaves outlines unfinished |

Successful background task submissions took 78–156 ms and returned 202. The
script then submitted a duplicate request and polled every 3 seconds. Result GETs
took 15–125 ms; these fast retrievals are not inference latency. The raw JSONL
retains task IDs/statuses and duplicate responses for audit. All 9 duplicate
submissions returned the same task ID, including the task intentionally interrupted
by deployment (`queue-dedup-checks.json`). No client automatic
inference retry or backend deadline increase was introduced.

The post-restart provider log confirms one language retry for Frontend explanation,
one retry plus deterministic fallback for Frontend interview, and one retry plus
fallback for Java explanation. Student explanation's response explicitly declares
fallback; its pre-recreate provider log was not retained, so exact retry count is
not independently certified. `recordedLanguageRetries: 0` in generated summary
means no matching retained log line, not proof that no retry occurred.

| CV | Career request / generation ms | Skill request / generation ms | Review |
|---|---:|---:|---|
| Student | 135,531 / 134,455 | 130,110 / 130,040 | FAIL: duplicated name, no milestones, false hired statement |
| Frontend | 96,313 / 96,216 | 79,000 / 78,952 | FAIL: generic CV advice, false hired statement and wrong audience |
| Sparse | 149,203 / 149,182 | 85,125 / 85,127 | FAIL/PARTIAL: some practice suggestions, but advises adding unverified evidence and lacks a tailored plan |
| Java | 103,875 / 103,867 | 84,125 / 84,155 | FAIL: asks Candidate to interview/test the applicant; ignores supplied Git/API evidence and calls Spring Boot a language |

Career/Skill outcomes use the Candidate Assistant endpoints; they are not a
separate verified skill-assessment product. Successful recommendation content and
Recruiter Assistant remain NOT VERIFIED. User-facing null/raw-enum/JSON rendering
across these fresh outputs is also not certified through API inspection alone.

Candidate Chat returned 200 in 48,828 ms (generation 47,425 ms,
`correctionAttempts=0`). Vietnamese prose and Java/Spring Boot/PostgreSQL advice
are understandable, but generic. It names profile identity `Nguyễn Anh QA` while
the selected synthetic resume is `Trần Quốc Bình`. Source confirms the prompt
includes both account profile and selected analysis, so this is an unresolved
context-priority ambiguity rather than proof of an invented identity. It also
asserts that the job is written in English without demonstrating that premise.
Classification: PARTIAL, not a factual/actionability PASS across all profiles.

The Java interview context retains only the first three entries of each fact
array. Combined with the extractor putting the project in experience entry four,
that loses the actual project before generation. Output schema checks require
nonempty bounded strings but do not check factual entailment, sentence completion,
HR-question relevance, hiring-state claims or roadmap milestones. These explain
why structurally valid output can still fail acceptance; they remain open work.

### Template-by-template sample classification

All rows below cover one short Unicode CV at desktop/mobile plus an editor
switch retaining the Unicode experience. They do not certify every long-text,
font, avatar, custom-section or template/PDF combination.

| Template | Classification |
|---|---|
| Classic Green | PASS |
| Executive | PASS |
| Modern Mint | PASS |
| ATS Focus | PASS |
| First Step | PASS |
| Modern Professional | PASS |
| Corporate | PASS |
| Minimal | PASS |
| Clean Grid | PASS |
| Sidebar Emerald | PASS |
| Sidebar Navy | PASS |
| Developer | PASS |
| Software Engineer | PASS |
| Data Professional | PASS |
| AI Engineer | PASS |
| Creative Accent | PASS |
| Portfolio | PASS |
| Marketing | PASS |
| Graduate | PASS |
| Intern | PASS |
| Fresher | PASS |
| Academic | PASS |
| Classic Formal | PASS |
| Elegant | PASS |

Evidence: `runtime-logs/st-qa/final-product/` contains 48 individual template
screenshots, inspected review sheets, `visual-metrics.json`, `template-switches.json`,
`pdf-exports.json`, `pdf-text-checks.json`, rendered PDF pages, before-edit values,
and the student AI UI snapshot. Mobile full-page screenshot stitching repeats
part of the lower action card; source and DOM have a single card. CV preview
regions themselves are fully visible; this is not certification of the stitched
lower-page pixels. Header PDF places the final short skills section alone on
page 2; no content loss, but pagination could be more compact.

### Authorization and uncompleted user journeys

The initial synthetic Candidate login succeeded. Later Browser explicitly
rejected Employer login and Candidate re-login for lack of trusted account-specific
authorization. No new browser login or alternate authentication was attempted
after the corresponding rejection. An already-started Candidate API run retains
its own established session; its subsequent results are API evidence, not a fresh
frontend journey. No Admin privilege was granted or restored.

The profile recommendation checkbox was clicked, but incomplete form validation
prevented saving; no successful consent change is claimed. The fresh recommendation
GET cases returned 409 consent-required. Successful ranked recommendation quality
and Recruiter Assistant remain NOT VERIFIED in this continuation. The Recruiter
request made before the login block hit intentional restart downtime (503), not
a usable assistant result.

Needed to complete the requested frontend journeys: explicit approval for the
named local synthetic Candidate/Employer logins, plus an active authorized Admin
session. No actual hiring decision or real applicant data was involved in this QA.

### Docker recovery and persistence

- At 12:01:39 +07, AI/frontend deployment intentionally interrupted the running
  Java explanation task `e9fd1521-6d6e-4117-b817-ded85d26581c`. Its next poll and
  later baseline feature calls received 503 during startup. These are induced
  downtime observations, not independent model-generation failures.
- The interrupted RUNNING task was later finalized as
  `FAILED|AI_PROVIDER_002|retryable=true|input_payload IS NULL` after the worker's
  10-minute expiration window. Read-only task-state evidence is in
  `restart-task-state.json`. It did not automatically resume. A later separately
  submitted Java explanation completed (with disclosed fallback); no permanent
  RUNNING task is claimed. Frontend retry interaction after restart is NOT VERIFIED.
- Once the established AI run completed, the final AI image was deployed at
  12:24:25 +07 and PostgreSQL/MinIO were restarted. No volumes were deleted.
  Before/after hashes match for the synthetic saved CV JSON, its resume document,
  its analysis row and the known MinIO object's storage file. Evidence:
  `persistence-before.json`, `persistence-after.json`, and
  `final-product-persistence-restart.log`. This proves sampled storage persistence,
  not every object or a fresh authenticated download after restart.
- At 12:25:21 +07 all 14 containers were healthy. Frontend `/`, gateway health,
  public jobs, public companies and MinIO live health each returned 200.
  `docker-final-health.json` retains exact statuses. Source/test changes ST-047
  and ST-048 are in the final AI image; a new authenticated model/match retest
  on that build remains NOT VERIFIED due to the login authorization block.
- The preexisting Ollama volume project-name warning remains. No volume metadata
  was changed merely to suppress it. Public mobile empty search and clearing the
  search back to four results worked; viewport was restored after browser QA.

### Regression evidence and artifacts

- Final backend: `final-product-root-final-test.log` (12:17:33 +07) and
  `final-product-root-final-verify.log` (12:22:09 +07), each 159 tests: auth 19,
  user 19, company 4, recruitment 14, application 9, notification 6, AI 88.
  The earlier 157-test root passes predate the month/language regression tests.
- Gateway: `final-product-gateway.log`, 17 tests; completed 11:55:17 +07.
- Frontend final: `final-product-frontend-final-{1,2,3}.log`, 50 tests each;
  `final-product-frontend-final-{lint,build}.log`. Earlier 49-test passes predate
  the added blur regression. Existing >500KB build-chunk warning remains.
- No timeout increase, test disabling, assertion reduction or serial-worker fix.
- API JSON envelopes legitimately contain metadata enums and nullable fields;
  their presence in raw evidence is not proof that raw JSON/null/enums reached
  the UI. The previously observed Candidate UI and public page localization are
  documented separately. Do not extend API-only review into a frontend PASS.
- Classification and keep/ignore recommendations: `docs/FINAL_QA_ARTIFACT_INVENTORY.md`.
  Useful QA scripts now live in `scripts/qa/`, take credentials from environment
  variables, and retain raw generated evidence under the already-ignored QA folder.
  No generated PDF, screenshot, log, credentials or Docker artifact is proposed
  for commit. All existing source/tests/docs remain intact.

### Release blockers

1. AI semantic/factual and task-specific quality is not acceptable across the
   requested CV diversity; see exact counterexamples above and final runtime results.
2. Positive Admin E2E, full final Employer/Candidate round trip and successful
   recommendation/Recruiter Assistant acceptance are incomplete.
3. Native Vietnamese IME needs a manual Telex/VNI session; automated safety
   coverage is useful but insufficient for the requested native behavior claim.
4. Long-running/restarted AI task behavior and repeated provider-output failures
   must be accepted or resolved before release; there is no full reliability PASS.

## Latest hardening continuation — 2026-08-31 (explicit Employer QA authorization)

Overall: **PARTIAL, not a full-system PASS**. No commit, push, fetch, reset,
checkout, stash or discard. This section supersedes the older checkpoint below.

### Changes and defect evidence

- **ST-041 / HIGH / PARTIAL:** Interview now has additive durable task routes,
  uses the existing locked queue/worker and keeps synchronous routes compatible.
  V9 activates a compact four-question prompt; generated output is bounded to
  640 tokens, repeated match/CV/contact data is omitted, and rubric metadata is
  deterministic. Candidate UI queues quickly, polls, restores saved results,
  reports task/read failures and retries reads without inference. Failed/stale
  tasks cannot overwrite an already saved interview. No timeout was increased.
  Candidate Assistant no longer includes the entire CV twice in its context.
- Real Interview before: HTTP 504 at 182 s, provider timeout. After: POST 202
  in **602 ms**, duplicate POST returned the same task, foreign task GET 404;
  completed **49,712 ms**, 581 input / 270 output tokens with real local
  Qwen2.5:3B-Instruct. Task `f7ac814c-b125-4a47-92b8-b237af2de641`, persisted
  result `aa3b9da0-b14b-4445-b6f9-6d55e19c0805`. Background execution does not
  retain browser tokens. Integration verifies 202/dedup/401/404/once-only
  generation/GET-only recovery and retryable provider failure with payload cleanup.
- **PASS, live Interview UI reload/polling:** button click issued POST 202
  (backend 252 ms), displayed the background-progress hint and disabled duplicate
  submission. Browser reloaded while running; selecting `cv-mixed.docx` again
  recovered the same task `a8382df3-3e86-40f6-ac3b-11a9192adba3`, still disabled.
  Inference completed in 50,090 ms (581/300 tokens), and polling displayed the
  saved interview automatically without another POST. The selected CV itself is
  not retained across reload; it defaults to the first CV and must be reselected.
  Existing recommendation-consent warning is expected; consent was not changed.
- **ST-042 / MEDIUM / FIXED (test harness timing):** the previous frontend
  flake waited one wall-clock second for several React Query timer notifications
  and effects while CPU-heavy work ran. Isolated query clients existed but were
  not explicitly cleared. Tests now flush query notifications in React `act`
  using microtasks, clear clients on teardown and restore notification scheduling.
  Assertions and the default parallel worker configuration are retained. Repeated
  default parallel runs: 48/48 PASS, including one during Maven regression;
  final post-CSS run also 48/48 PASS. No one-worker workaround is required.
- **ST-043 / MEDIUM / FIXED:** Candidate dashboard at 1024px had client width
  1009 but scroll width 1015. Its implicit grid track grew to 723px inside a
  689px workspace: search columns required 15rem + 25rem before accounting for
  the sidebar. Fix `candidate-page.css`: bounded dashboard track and delay the
  two-column search layout until 1280px. No global overflow hiding. Deployed CSS
  hash verified after a full browser reload before the final measurement:
  1024px client/scroll both 1009px; widths 375,390,768,1280,1920 also have
  scroll width equal to client width (360,375,753,1265,1905 respectively).
- A newly authored backend failure-path test initially failed during mock setup:
  `when(generate(any()))` invoked the previous answer with null. Corrected to
  `doThrow(...).when(...)`, retaining every assertion. Targeted suite then PASS
  10/10; the full root reactor was restarted from the beginning. Original failed
  run retained as `hardening-root-test-mock-error.log`; not a production defect.

### Real journeys and authorization

- **PASS, tested hiring branch:** Employer QA login, owner company/dashboard,
  published QA job, application list and full detail (profile snapshot, QA cover
  letter, PDF v1) verified in the browser. Employer changed APPLIED → SCREENING
  through the UI; INTERVIEW → OFFER → HIRED transitions were then exercised via
  its authenticated API. Candidate API confirmed every transition and history.
  Candidate browser confirmed INTERVIEW, OFFER and HIRED after refresh; its
  history/notifications include SCREENING. This is not a claim that Candidate UI
  was observed while SCREENING was still the current state.
- Candidate notifications for SCREENING/INTERVIEW/OFFER/HIRED arrived and were
  visible in API and UI. Delivery is asynchronous: an immediate read can precede
  the event consumer; subsequent read-only checks confirmed delivery. Existing
  English notification titles/raw status strings remain ST-027, not localized PASS.
- Terminal HIRED → SCREENING rejected HTTP 400. Fresh rejection/withdrawal
  branches and all onboarding invalid-input combinations were not rerun here;
  prior company/draft/edit/publish evidence and regression tests remain relevant.
- **PASS, scoped API IDOR checks:** 65 GET probes across Anonymous, Candidate A,
  Candidate B, Employer A and newly registered synthetic Employer B. Private
  application/snapshot: owner Candidate and owning Employer 200, foreign actors
  403, anonymous 401. Private CV/PDF: Candidate owner 200, other Candidate 404,
  Employers 403, anonymous 401. Uploaded CV: owner 200, foreign/Employer 403.
  AI resume/result/task and notification deny foreign access (404 or role 403).
  Public published job/company are intentionally 200 for all five actors.
  Own-profile route is scoped by session; empty Candidate B profile returns 404.
- Direct mutations: anonymous application status 401; both Candidates and foreign
  Employer status mutation 403; foreign Candidate withdrawal 403; foreign
  Employer job PUT with a fully valid body 403. An initial incomplete job body
  returned validation 400 and was NOT counted as ownership evidence.
- **NOT VERIFIED:** live Admin matrix in this continuation. The earlier synthetic
  Admin account remains revoked/disabled; no real Admin credentials or privilege
  grants were used. Admin authorization coverage in integration tests is not a
  substitute for a live Admin journey. Full six-account/resource Cartesian matrix
  remains incomplete.

### CV, file processing, AI quality and responsive QA

- CV opened after logout/login with correct saved name and two-line text. Unicode
  long-text input/autosave was verified against the API, then PDF downloaded via
  the browser. Original language `vi` and original two-line experience description
  were restored and verified through API; uploaded application PDF is unchanged.
- **PASS for sampled Developer template exports:** browser downloads
  `ST Browser Journey (2).pdf` (1 page), `(3).pdf` (2 pages Vietnamese), `(4).pdf`
  (2 pages English labels, Vietnamese body). Poppler rendered all five pages and
  every image was inspected. pypdf confirms name Unicode and all 24 numbered QA
  paragraphs in both multi-page exports. Sidebar/background retained on page 2;
  no missing text or clipped glyphs observed. Long paragraphs necessarily continue
  across pages. The PDF skill guided render-and-inspect verification.
- **PARTIAL, 24-template QA:** default frontend regression exercises all 24
  presets and content preservation. Only the sampled Developer PDFs were freshly
  downloaded/rendered in this continuation; this is not fresh visual certification
  of all 24 templates, every section operation or every font/layout combination.
- **NOT VERIFIED, native IME:** automation confirms Unicode replacement and
  saved multiline text, not physical Telex/VNI composition, precise native caret
  or every clipboard shortcut. Existing composition/history unit tests pass.
  No new editor event-logic change was made based on synthetic caret artifacts.
- **PARTIAL, AI semantics:** Interview output is Vietnamese and persisted, but
  its HR question focuses on a project instead of motivation and an answer outline
  introduces RabbitMQ without confirmed CV evidence. Career Roadmap after context
  dedup returned HTTP 200 in **69,181 ms** (provider metadata 71,626 ms;
  1649 input / 339 output tokens), but mainly repeats keyword/CV advice instead of
  career milestones. Do not interpret HTTP success as task relevance/factual PASS.
  ST-033 and ST-035 remain open. No cache of potentially changed inputs was added.
- Prior completed AI batch: chat 200/104s, explanation 200/118s, interview 504/182s,
  career roadmap 504/183s, learning 200/107s, skill 200/69s, certificate 200/92s,
  portfolio 200/99s, job search 200/72s, resume improvement 200/61s. The later
  isolated benchmarks above do not prove equivalent latency under contention.
  PDF/DOCX/TXT extraction and null/name regressions retain earlier evidence; no
  fresh full semantic acceptance run across every format/assistant is claimed.
- Responsive widths sampled: 375,390,768,1024,1280,1920. CV, AI Career, Login,
  Register, public jobs/detail and Employer dashboard/jobs/applications measured
  without global horizontal overflow; populated Employer Applications at 375px
  visually checked. Candidate dashboard exposed ST-043 and is rechecked after
  deployment below. Admin and every modal/dropdown/touch interaction are not
  comprehensively certified. Canvas scrolling remains separate from body scrolling.

### Regression and runtime checkpoint

- Actual backend root reactor `mvn clean test --batch-mode` PASS at 02:47:01 +07;
  `mvn clean verify --batch-mode` PASS at 02:49:05 +07. Both run **156 tests**:
  auth 19, user 19, company 4, recruitment 14, application 9, notification 6,
  AI 85; zero failures/errors/skips. No disabled or reduced assertions.
- Gateway separate `mvn clean verify --batch-mode`: **17 PASS** at 02:40:22 +07.
- Frontend final default parallel test **48 PASS**; lint/build PASS. Logs:
  `hardening-frontend-{test,lint,build}.log` and `hardening-parallel-test-{1,2,3}.log`.
- Docker rebuilt AI/frontend with these changes; compose config validates and
  14/14 healthy. AI service explicitly restarted; persisted interview result read
  through Gateway with HTTP 200 and the same result id. Browser hit the expected
  stale-chunk recovery boundary after frontend replacement; reload restored UI and
  session, not a blank screen. Mid-inference process death remains unverified.
- Known operational limits: CPU-only model and contention; 180s provider deadline
  unchanged; legacy synchronous endpoints remain blocking; polling capped at 900
  successful reads per cached query (about 30 minutes at 2s, excluding request time),
  stopping on errors until read retry; no claim of multi-instance crash exactly-once
  inference. Timezone migration ST-026, partial non-AI localization ST-027, session
  draft lifetime/multi-tab merge limits and >500KB frontend chunk warning remain.
- Git start: 87 dirty/untracked files; current: **91 files (67 modified + 24
  untracked)**. HEAD and local origin/main unchanged:
  `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`, branch main, ahead 0 / behind 0.
  No staging or Git history changes. New synthetic Employer B and the QA hiring
  history are retained for reproduction; no production data touched.

Current inventory is the 87 paths in the historical inventory below plus:

```text
backend/ai-service/src/main/java/com/recruitment/ai/interview/CompactInterview.java
backend/ai-service/src/main/resources/db/migration/V9__compact_interview_prompt.sql
backend/ai-service/src/test/java/com/recruitment/ai/interview/CompactInterviewTest.java
frontend/web-app/src/features/candidate/candidate-page.css
```

Current issue totals: **43 confirmed; 0 critical, 22 high, 19 medium, 2 low**.
38 implemented fixes; five open/partial (ST-026, ST-027, ST-033, ST-035, ST-041).

## Previous continuation checkpoint — 2026-08-31

Status: **NOT a full-system QA PASS**. All automated regression suites pass,
but AI long-generation reliability, factual quality and remaining live journeys
are not fully signed off. No commit, push, reset, checkout, stash or discard.

- Root `mvn clean test --batch-mode`: PASS, 153 tests, zero failures/errors/skips,
  completed 02:07:45 +07. Root `mvn clean verify --batch-mode`: PASS, same 153
  tests, completed 02:13:26 +07. Counts: auth 19, user 19, company 4,
  recruitment 14, application 9, notification 6, AI 82.
- Gateway is outside that reactor: separate `mvn clean verify --batch-mode`
  PASS, 17 tests, 02:05:44 +07. Frontend latest code has 47 tests PASS with
  `npm run test -- --maxWorkers=1`; lint/build PASS. The final parallel test run
  hit the default one-second UI wait in the new retrieval test (46 pass/1 fail).
  The unchanged assertions pass with one worker. This is a test timing limitation
  under concurrent CPU-heavy inference, not an unqualified parallel-suite PASS.
  Logs: `resumed-frontend-serial-test.log` and `resumed-frontend-{lint,build}.log`.
- Docker compose config validates; 14/14 services healthy. AI/company/frontend
  images include continuation changes. Build warnings: large frontend chunk,
  existing Ollama volume was created under a different Compose project name.
- Explanation async runtime: POST 202 in 1310 ms, immediate duplicate returned
  the same task, foreign candidate task GET 404; task completed with real
  Qwen2.5:3B-Instruct on 100% CPU, generation 73696 ms. Polling connection was
  interrupted, but task continued and its saved result could be read afterward.
  Resume on Aug 31: backward-compatible synchronous explanation also returned
  200 in 118 s. Browser loaded that saved explanation for the selected DOCX
  match, with 92/100 and source-backed five years unchanged.
- Candidate browser: found the new QA job, saw the attached `ST Browser
  Journey.pdf` version 1, submitted a synthetic cover letter, got POST 201,
  saw `Đã ứng tuyển` instead of another submit button, and found the correct
  company/job in the application list. Application id:
  `06ca5c81-b500-4be8-a23b-bffcdc46a1bf`.
- Employer browser on Aug 30: first company create reproduced ST-038; valid
  classification then created company `78d79f9c-cfe7-421d-8a88-4112c59a3b5a`;
  created/edited/published job `edf06f4c-76cb-48fc-bdcf-9d2ecba89ebd` with an
  Employer-only navigation shell. Aug 31 API invalid-classification recheck is
  HTTP 400 with both Vietnamese field errors, rather than 500.
- Employer application review/status update/candidate notification round-trip
  remains pending. The browser tool explicitly rejected the Employer login;
  direct user confirmation has been requested. No alternate login method was
  used to circumvent that rejection. The existing Candidate session remains
  usable for CV checks. Do not label the complete Employer journey PASS.
- CV: exact `Nguyễn Đình Tuấn Tú` and two-line experience text survive save,
  reload and preview. Native Telex/VNI, precise caret movement and clipboard
  keyboard behavior are NOT certified: browser automation anchors selection at
  the contenteditable SPAN and cannot establish native IME behavior reliably.
- Responsive browser after ST-040: widths 1920,1440,1024,768,430,375 produced
  document widths 1905,1425,1009,753,415,360 respectively; no global overflow.
  Heights tested: 1080,900,768,1024,932,667. Mobile and tablet screenshots inspected;
  A4 canvas retains its separate horizontal scrolling surface.
- Prior PDF/24-template/security evidence below remains historical evidence,
  not a claim that every scenario was rerun on Aug 31. English PDF, fresh 3+
  page rendered PDF, all restart/concurrency combinations and the full six-account
  live matrix remain incomplete for this continuation.
- Real AI on Aug 31: English chat 200/104 s in Vietnamese; explanation 200/118 s;
  interview 504/182 s; CAREER_ROADMAP 504/183 s; LEARNING_ROADMAP 200/107 s;
  SKILL_ROADMAP 200/69 s; CERTIFICATE_RECOMMENDATION 200/92 s.
  The already-started bounded API batch is still running at this checkpoint;
  portfolio/job-search/resume-improvement are not yet counted as verified.
  Remaining assistant cases are
  recorded progressively in `runtime-logs/st-qa/resumed-ai-features.log`.
  HTTP success alone is not semantic PASS. Explanation still proposed adding
  Cassandra/HBase to the CV without source evidence: ST-033/ST-035 stay open.
- Git: branch main, HEAD and local origin/main both
  `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`; ahead 0 / behind 0; dirty working
  tree, 87 changed/untracked files at this checkpoint. No fetch or push performed.
  Exact current paths are listed in the continuation file inventory below.

### Newly confirmed issues and resolution

- **ST-038 / HIGH / Employer onboarding — FIXED.** Reproduce: create company
  with name but leave type/size at `Chưa xác định`. Expected field validation;
  actual PostgreSQL NOT NULL violation and HTTP 500. Root cause: optional form
  and request DTO disagreed with required database columns. Fix: @NotNull on
  create DTO, Zod validation and Vietnamese field alerts in form. Partial update
  semantics preserved. Integration test covers missing both/each field; schema
  regression covers invalid/valid classification; Docker HTTP 400 verified.
- **ST-039 / MEDIUM / Explanation retrieval — FIXED.** Reproduce: completed task,
  then GET saved explanation returns 503. Expected visible error plus read retry;
  actual silent missing result because query error was not rendered. Added a
  failing-before-fix frontend test, then ErrorNotice for non-404 retrieval errors.
  Retry invokes GET only; test asserts no new generation POST. Runtime connection
  interruption motivated this check; the controlled 503 itself is a unit test.
- **ST-040 / MEDIUM / CV tablet toolbar — FIXED.** At 1024x768, document width
  reached 1094px and toolbar actions extended to x=1075px. Root cause: toolbar
  wrapping depended on a 1000px viewport breakpoint, ignoring application sidebar
  width. Fix: allow toolbar/actions to wrap based on available space, with bounded
  action width. Real Docker/browser regression: document width now 1009px; all
  six requested sizes pass overflow measurement. No global overflow hiding added.
- **ST-041 / HIGH / Long AI generation — OPEN.** Real Qwen CPU interview and
  career roadmap calls exceeded the existing 180s provider deadline (504 with
  retryable AI_PROVIDER_002). Runtime showed Ollama 632% CPU and 2.285 GiB RAM;
  Maven and image builds were also active, so contention is a confounder, not a
  proven application-only cause. These tasks still use synchronous generation
  and larger contexts/output contracts. Workaround: retain saved results and
  retry individually after load subsides; no automatic retry flood or blind
  timeout increase. Compact/async Explanation is verified, but has not been
  expanded to every AI operation. Further bounded-context/runtime work needed.

ST-036 is implemented by compact four-field advice plus deterministic evidence
composition, additive V8 migration and backward-compatible durable task routes.
ST-037 uses match/task row locks, shared sync/async active claims, payload cleanup,
and stale task expiry; integration tests verify deduplication, ownership, once-only
processing and interrupted RUNNING expiry. Restart/expiry matrix is not fully
runtime-certified. ST-033 has source-exact name restoration and nullable schema
regressions, but overall factual fidelity remains open. ST-035 has distinct task
instructions and token bounds; final semantic/runtime acceptance remains open.
ST-027 footer destinations/unavailable labels and Candidate sidebar were improved;
legacy job enum labels and non-AI shell strings remain, so it is only partial.

### Continuation file inventory

Exact paths relative to `Projects/RecruitmentSystem`, including preserved earlier
changes and untracked source/tests. Runtime fixtures/logs are ignored, not staged.

```text
.gitignore
backend/ai-service/src/main/java/com/recruitment/ai/assistant/CandidateAssistantTask.java
backend/ai-service/src/main/java/com/recruitment/ai/assistant/VietnameseGenerationPolicy.java
backend/ai-service/src/main/java/com/recruitment/ai/assistant/VietnameseResponsePolicy.java
backend/ai-service/src/main/java/com/recruitment/ai/config/OllamaProperties.java
backend/ai-service/src/main/java/com/recruitment/ai/config/OpenAiProperties.java
backend/ai-service/src/main/java/com/recruitment/ai/controller/ExplanationInterviewController.java
backend/ai-service/src/main/java/com/recruitment/ai/entity/AiTask.java
backend/ai-service/src/main/java/com/recruitment/ai/exception/ErrorCode.java
backend/ai-service/src/main/java/com/recruitment/ai/explanation/CompactExplanation.java
backend/ai-service/src/main/java/com/recruitment/ai/matching/engine/RuleBasedMatchingEngine.java
backend/ai-service/src/main/java/com/recruitment/ai/matching/rule/JobRequirementsParser.java
backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/CertificateScorer.java
backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/ExperienceScorer.java
backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/SkillScorer.java
backend/ai-service/src/main/java/com/recruitment/ai/matching/util/MatchingText.java
backend/ai-service/src/main/java/com/recruitment/ai/provider/OllamaStructuredGenerationProvider.java
backend/ai-service/src/main/java/com/recruitment/ai/repository/AiTaskRepository.java
backend/ai-service/src/main/java/com/recruitment/ai/repository/JobMatchResultRepository.java
backend/ai-service/src/main/java/com/recruitment/ai/repository/ResumeDocumentRepository.java
backend/ai-service/src/main/java/com/recruitment/ai/service/ExplanationInterviewService.java
backend/ai-service/src/main/java/com/recruitment/ai/service/analysis/ResumeAnalysisJsonValidator.java
backend/ai-service/src/main/java/com/recruitment/ai/service/impl/AssistantServiceImpl.java
backend/ai-service/src/main/java/com/recruitment/ai/service/impl/ExplanationInterviewServiceImpl.java
backend/ai-service/src/main/java/com/recruitment/ai/service/impl/ExplanationTaskWorker.java
backend/ai-service/src/main/java/com/recruitment/ai/service/impl/ResumeServiceImpl.java
backend/ai-service/src/main/resources/db/migration/V8__durable_explanation_tasks.sql
backend/ai-service/src/test/java/com/recruitment/ai/assistant/CandidateAssistantTaskTest.java
backend/ai-service/src/test/java/com/recruitment/ai/assistant/VietnameseGenerationPolicyTest.java
backend/ai-service/src/test/java/com/recruitment/ai/assistant/VietnameseResponsePolicyTest.java
backend/ai-service/src/test/java/com/recruitment/ai/explanation/CompactExplanationTest.java
backend/ai-service/src/test/java/com/recruitment/ai/integration/MatchingIntegrationTest.java
backend/ai-service/src/test/java/com/recruitment/ai/integration/ResumePipelineIntegrationTest.java
backend/ai-service/src/test/java/com/recruitment/ai/matching/MatchingScorersTest.java
backend/ai-service/src/test/java/com/recruitment/ai/provider/OllamaStructuredGenerationProviderTest.java
backend/ai-service/src/test/java/com/recruitment/ai/service/analysis/ResumeAnalysisJsonValidatorTest.java
backend/ai-service/src/test/resources/application-test.yml
backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayCorsConfig.java
backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayHttpClientConfig.java
backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayRouteConfig.java
backend/api-gateway/src/test/java/com/recruitment/gateway/GatewayIntegrationTest.java
backend/company-service/src/main/java/com/recruitment/company/dto/request/CreateCompanyRequest.java
backend/company-service/src/test/java/com/recruitment/company/integration/CompanyAuthorizationIntegrationTest.java
backend/user-service/src/main/java/com/recruitment/user/dto/cv/CvTemplateCatalog.java
backend/user-service/src/main/java/com/recruitment/user/dto/request/CreateCvFromProfileRequest.java
backend/user-service/src/main/java/com/recruitment/user/dto/request/SaveCandidateCvRequest.java
backend/user-service/src/main/java/com/recruitment/user/service/CandidateCvService.java
backend/user-service/src/main/java/com/recruitment/user/service/CvPdfService.java
backend/user-service/src/test/java/com/recruitment/user/integration/CandidateCvIntegrationTest.java
backend/user-service/src/test/java/com/recruitment/user/service/CvPdfServiceTest.java
docs/SYSTEM_HARDENING_REAL_USER_QA.md
frontend/web-app/package-lock.json
frontend/web-app/package.json
frontend/web-app/src/app/providers/AppErrorBoundary.test.tsx
frontend/web-app/src/app/providers/AppErrorBoundary.tsx
frontend/web-app/src/app/providers/AppProviders.test.tsx
frontend/web-app/src/app/providers/AppProviders.tsx
frontend/web-app/src/components/navigation/Footer.test.tsx
frontend/web-app/src/components/navigation/Footer.tsx
frontend/web-app/src/components/navigation/UserMenu.tsx
frontend/web-app/src/components/navigation/footer.css
frontend/web-app/src/features/ai-career/AiCareerPage.test.tsx
frontend/web-app/src/features/ai-career/AiCareerPage.tsx
frontend/web-app/src/features/ai-career/ai-career.api.ts
frontend/web-app/src/features/ai-career/ai-career.labels.ts
frontend/web-app/src/features/candidate/components/CandidateHeader.tsx
frontend/web-app/src/features/candidate/components/CandidateSidebar.tsx
frontend/web-app/src/features/cv-builder/CvEditorPage.test.tsx
frontend/web-app/src/features/cv-builder/CvEditorPage.tsx
frontend/web-app/src/features/cv-builder/CvTemplatePreviewPage.tsx
frontend/web-app/src/features/cv-builder/CvTemplatesPage.tsx
frontend/web-app/src/features/cv-builder/components/CvPreview.test.tsx
frontend/web-app/src/features/cv-builder/components/CvPreview.tsx
frontend/web-app/src/features/cv-builder/components/EditableText.test.tsx
frontend/web-app/src/features/cv-builder/components/EditableText.tsx
frontend/web-app/src/features/cv-builder/cv-builder.css
frontend/web-app/src/features/cv-builder/cv.draft.ts
frontend/web-app/src/features/cv-builder/cv.presets.ts
frontend/web-app/src/features/cv-builder/cv.templates.test.ts
frontend/web-app/src/features/cv-builder/cv.templates.ts
frontend/web-app/src/features/cv-builder/cv.types.ts
frontend/web-app/src/features/employer/components/EmployerCompanyForm.tsx
frontend/web-app/src/features/employer/employer-company.schemas.test.ts
frontend/web-app/src/features/employer/employer-company.schemas.ts
frontend/web-app/src/lib/api/client.ts
frontend/web-app/src/main.tsx
frontend/web-app/src/styles/global.css
```

## Resolution overview

### Continuation findings (before fixes)

- **ST-036 / HIGH / AI Explanation:** POST explanation for the existing 92-point
  QA match fails twice at 180 seconds with AI_PROVIDER_002. Expected: useful,
  bounded generation without tying up the browser connection. Trace: Axios 400s,
  Gateway AI route 390s, Java HttpRequest 180s, Ollama CPU inference; no browser
  abort. Root cause: full duplicated context plus 19 unbounded output groups and
  maxOutputTokens=0. Plan: compact generated advice, compose immutable matching
  facts server-side, preserve old response contract, add durable 202 task flow.
- **ST-037 / HIGH / Explanation concurrency/recovery:** code inspection of
  createTask shows no match lock or active-task check; two submissions can run
  inference twice and race the unique persisted explanation. RUNNING tasks have
  no periodic expiry. Expected: one active claim, safe retries and terminal reason
  after interrupted execution. Plan: shared match-row claim for sync/async,
  DB-backed queue, bounded stale-task expiry, persistence guarded by task state.

41 confirmed defects: **0 critical, 22 high, 17 medium, 2 low**.
36 implemented fixes; 5 open/partial issues (ST-026, ST-027, ST-033, ST-035, ST-041).
Final verification is still in progress; do not interpret this as a release certificate.

| Area | Fixed IDs | Evidence |
| --- | --- | --- |
| Inline editing / history | 001, 014 | Focused undo/redo, Unicode paste, caret/selection/composition tests; browser typing |
| CV persistence / recovery | 003, 004, 008, 018, 021, 031 | Delayed first save, PDF serialization, rejected-save no loop, draft recovery, canonical signatures, outage retry, pending profile import |
| Sections / mobile / fonts | 007, 013, 019, 025, 028, 030 | Browser section CRUD, skill duplicate/reorder/delete, 6 viewports, local canvas scroll, modal visual retest |
| PDF | 005, 006, 020 | PDFBox multipage rendering, selectable Vietnamese, null/Unicode handling, column-position test and real downloaded PDF inspection |
| AI scoring / presentation | 009, 010, 011, 012, 016, 029, 032 | Regression tests, Vietnamese/mixed/English fixtures; English reanalysis on final image 200 / 167s, Vietnamese summary, matching recognizes 5 years |
| Runtime / security | 002, 015, 017, 022, 024 | CORS 401, live duplicate analysis 409, >120s analysis success, replacement-container DNS recovery, owner-scoped query cache |
| Test harness | 023 | Thread-safe bounded wait retains all credential-redaction assertions |
| Deployment recovery | 034 | Real lazy-route load during a bounded frontend outage shows Vietnamese recovery; explicit reload after restart opens preview; unit test preserves draft |

### Deferred issues

- **ST-026 (medium):** zone-less legacy timestamps need an explicit timezone/data
  migration decision, not a blanket client-side UTC assumption.
- **ST-027 (low):** legacy footer placeholders and non-AI shell localization;
  primary candidate/CV/AI navigation works. No unimplemented destination invented.
- **ST-033 (medium):** small-model extraction fidelity: the Vietnamese PDF name
  `Nguyễn Văn An` was shortened to `Nguyễn Văn`, and one role was represented as
  two experience lines, inflating the document-quality entry count. Matching no
  longer invents years from those counts. A fully source-anchored extraction
  schema is outside this compatibility-preserving hardening; users must review
  extracted facts. No claim of hallucination-free or perfect extraction is made.
- **ST-035 (medium):** task-specific assistant quality remains uneven. Live
  certificate, portfolio, skill and job-search advice repeated matching keywords
  instead of consistently delivering task-specific practical recommendations.
  The shared prompt supplies only the task enum and a common response schema;
  its validator checks structure/language, not task relevance. A task-specific
  prompt/evaluation pass is needed; HTTP 200 is not counted as semantic quality PASS.

ST-033 reproduction: upload the generated ST Browser Journey PDF, analyze with
Qwen2.5:3B, expand structured facts, compare against the CV preview/PDF. Root
cause: generative flat arrays lack source spans and entity grouping. A grounded
schema and evaluation corpus are the follow-up, not guessed name reconstruction.

## Scope and verification notes

- The 24 original presets reuse CvContent + CvDesignConfig, four layouts, eight
  palettes, six font selections, three densities and three section arrangements.
  Six categories have gallery filtering, search, actual previews and apply actions.
  Existing saved designConfig remains authoritative; applying a preset changes
  design only and preserves content/custom/hidden sections. Undo restores design.
  All 24 IDs pass backend profile-create/get/PDF and frontend content-preservation tests.
- Candidate QA account completed UI registration/login, profile update, creation
  from profile, Unicode editing, save/refresh, preview, PDF download/upload,
  AI PDF analysis, job detail, application submission/tracking, notifications.
  The application is explicitly labelled synthetic QA and uses an existing QA job.
- Browser CV persistence survived logout/login and replacement of user-service;
  gateway recovered after IP changed from 172.18.0.15 to 172.18.0.4 without a gateway restart.
- Owner CV read/update/PDF 200; foreign candidate 404; employer/admin 403;
  unauthenticated 401. AI admin read is allowed by its existing contract.
  Temporary admin fixture was revoked, refresh tokens removed and account disabled.
- Native Windows Telex/VNI was not available for a reliable end-to-end IME test.
  Unicode browser typing and DOM composition tests passed; these are not equivalent.
- PDF fonts fall back to available compatible sans/serif faces; exact six-face
  browser/PDF parity is not guaranteed. Unsupported emoji can render as `?`.
- Recovery uses owner-scoped sessionStorage, not durable cross-tab/device drafts.
  Closing the tab loses its local journal; interrupted first-create before its
  acknowledgement still lacks a backend idempotency key. No multi-tab merge claimed.
- Sparse job requirements retain existing no-penalty scoring policy, so an
  underspecified job can score highly without enough evidence to interpret it.
- A frontend timing test hit its default wait under concurrent heavy builds;
  running the unchanged assertions with one worker passed. A reactor run collided
  with a local recompile of the same ai-service target and produced missing-bean
  errors; that run is invalid and must be replaced by a clean sequential reactor.
- After enabling resume-language enforcement, the matching integration provider
  fixture's English project sentence was translated to Vietnamese with the same
  technology facts. All score, ownership, persistence and status assertions are
  unchanged; dedicated regressions now require English prose to be rewritten or rejected.
- Generated QA files/logs are ignored under runtime-logs/st-qa; no credentials,
  tokens or private resume data are included in the commit.

## Historical verification checkpoint — before continuation changes

1. **Total defects:** 35, recorded with reproduction/root cause before fixes.
2. **Severity:** critical 0, high 18, medium 15, low 2.
3. **Implemented:** 31 fixes listed above, including all confirmed high defects.
4. **Deferred:** ST-026 timezone contract, ST-027 legacy shell/footer,
   ST-033 factual extraction quality, ST-035 task-specific advice quality.
5. **Main causes:** focused DOM ignored external history; first-save response
   rehydrated stale state; SPA navigation cancelled debounce; CORS ran after JWT;
   missing DB analysis claim; mismatched timeout budgets; private cache survived
   account changes; PDF continuation text inherited banner color.
6. **Regressions:** focused history/composition/paste, delayed autosave and import,
   draft recovery, failed-save bounded retries, selection/context isolation,
   account cache isolation, all 24 preset content preservation, PDF Unicode/
   pagination/columns, analysis concurrency, language rewrite/rejection, explicit
   years, negative certificate evidence, skill alias/dedup and CORS failures.
7. **Desktop browser:** 1920x1080, 1440x900, 1366x768; editing, section CRUD,
   save/refresh/logout/login, template apply/undo, preview/download and candidate
   journey exercised. Foreign account sees empty CV list after owner logout/login
   in the same SPA; direct foreign editor URL denies access without CV content.
8. **Mobile/tablet:** 390x844, 375x667, 768x1024; no global CV overflow, intentional
   local A4 canvas scroll. Mobile template modal title/close visible; AI cards,
   profile form and primary navigation inspected. Native Telex/VNI remains unverified.
9. **AI runtime:** TXT Vietnamese/sparse/long English, PDF and mixed DOCX analyzed
   with real Qwen2.5:3B. Final English reanalysis 200 in 167s, match 93/100 and
   five explicit years; mixed DOCX match 92, Vietnamese match 78, sparse match 25.
   Concurrent same-resume request returns 409. English chat, interview and all
   seven candidate assistant task types returned 200 with Vietnamese-oriented
   prose; semantic quality is NOT uniformly PASS (ST-033/ST-035). Explanation
   timed out with 504 at 180s both initially and on the single deliberate retry
   after other builds/generations ended. This runtime feature is NOT verified
   successful on the local model; error handling is bounded, without retry loops.
10. **CV Builder:** Unicode content retained without observed reversal; focused
    undo/redo, section controls, draft recovery and service-restart persistence
    checked. Real downloaded Developer PDF visually inspected with selectable
    Vietnamese, header and populated sidebar. Multipage/hidden/custom sections
    also covered by rendering and tests; exact browser/PDF font parity not promised.
11. **Templates:** 24 original presets / six categories / shared renderer;
    content independent from design, no proprietary copied assets, old IDs retained.
12. **Tests:** root `mvn clean test` PASS (16:13:58), root `mvn clean verify` PASS
    (16:17:43), each 146 tests, zero failures/errors/skips. Module counts:
    auth 19, user 19, company 3, recruitment 14, application 9, notification 6,
    AI 76. Gateway is outside root reactor: separate clean verify 17/17 PASS.
    Frontend 44/44 PASS, lint PASS, production build PASS. No JVM stop needed.
13. **Docker/security:** affected user/AI/Gateway/frontend images rebuilt;
    config validation PASS, all 14 containers healthy after final AI deployment
    and frontend outage recovery. CV owner read/update/PDF 200, foreign 404,
    employer/admin 403, anonymous 401 (admin fixture tested separately and revoked).
14. **Git checkpoint:** 61 changed files below; diff reviewed and diff-check PASS.
    HEAD/origin/main both `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78` after fetch,
    ahead/behind 0/0, working tree intentionally dirty. No commit/push yet:
    final authenticated browser/recommendation check awaits explicit QA-login
    approval after the browser tool rejected switching back to the QA owner.
    Explanation runtime success also remains unproven (two provider timeouts).
15. **Limitations:** see deferred issues and scope notes. Qwen latency can exceed
    180s per generation; language guard is heuristic, not a guarantee of perfect
    localization/factuality. New extraction still uses Vietnamese absence prose
    in contact fields and can split duties into projects/skills (ST-033).
    Build warns about a >500kB main chunk; Compose warns that the existing Ollama
    volume was created under the earlier project name. Synthetic QA records are
    retained for reproduction; only the temporary admin privilege was revoked.

### Exact changed files (relative to Projects/RecruitmentSystem)

- `.gitignore`
- `backend/ai-service/src/main/java/com/recruitment/ai/assistant/VietnameseGenerationPolicy.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/assistant/VietnameseResponsePolicy.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/config/OllamaProperties.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/config/OpenAiProperties.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/exception/ErrorCode.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/engine/RuleBasedMatchingEngine.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/rule/JobRequirementsParser.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/CertificateScorer.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/ExperienceScorer.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/SkillScorer.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/util/MatchingText.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/repository/AiTaskRepository.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/repository/ResumeDocumentRepository.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/analysis/ResumeAnalysisJsonValidator.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/impl/ResumeServiceImpl.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/assistant/VietnameseGenerationPolicyTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/assistant/VietnameseResponsePolicyTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/integration/MatchingIntegrationTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/integration/ResumePipelineIntegrationTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/matching/MatchingScorersTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/provider/OllamaStructuredGenerationProviderTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/service/analysis/ResumeAnalysisJsonValidatorTest.java`
- `backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayCorsConfig.java`
- `backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayHttpClientConfig.java`
- `backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayRouteConfig.java`
- `backend/api-gateway/src/test/java/com/recruitment/gateway/GatewayIntegrationTest.java`
- `backend/user-service/src/main/java/com/recruitment/user/dto/cv/CvTemplateCatalog.java`
- `backend/user-service/src/main/java/com/recruitment/user/dto/request/CreateCvFromProfileRequest.java`
- `backend/user-service/src/main/java/com/recruitment/user/dto/request/SaveCandidateCvRequest.java`
- `backend/user-service/src/main/java/com/recruitment/user/service/CandidateCvService.java`
- `backend/user-service/src/main/java/com/recruitment/user/service/CvPdfService.java`
- `backend/user-service/src/test/java/com/recruitment/user/integration/CandidateCvIntegrationTest.java`
- `backend/user-service/src/test/java/com/recruitment/user/service/CvPdfServiceTest.java`
- `docs/SYSTEM_HARDENING_REAL_USER_QA.md`
- `frontend/web-app/package-lock.json`
- `frontend/web-app/package.json`
- `frontend/web-app/src/app/providers/AppErrorBoundary.test.tsx`
- `frontend/web-app/src/app/providers/AppErrorBoundary.tsx`
- `frontend/web-app/src/app/providers/AppProviders.test.tsx`
- `frontend/web-app/src/app/providers/AppProviders.tsx`
- `frontend/web-app/src/features/ai-career/ai-career.labels.ts`
- `frontend/web-app/src/features/ai-career/AiCareerPage.test.tsx`
- `frontend/web-app/src/features/ai-career/AiCareerPage.tsx`
- `frontend/web-app/src/features/cv-builder/components/CvPreview.test.tsx`
- `frontend/web-app/src/features/cv-builder/components/CvPreview.tsx`
- `frontend/web-app/src/features/cv-builder/components/EditableText.test.tsx`
- `frontend/web-app/src/features/cv-builder/components/EditableText.tsx`
- `frontend/web-app/src/features/cv-builder/cv-builder.css`
- `frontend/web-app/src/features/cv-builder/cv.draft.ts`
- `frontend/web-app/src/features/cv-builder/cv.presets.ts`
- `frontend/web-app/src/features/cv-builder/cv.templates.test.ts`
- `frontend/web-app/src/features/cv-builder/cv.templates.ts`
- `frontend/web-app/src/features/cv-builder/cv.types.ts`
- `frontend/web-app/src/features/cv-builder/CvEditorPage.test.tsx`
- `frontend/web-app/src/features/cv-builder/CvEditorPage.tsx`
- `frontend/web-app/src/features/cv-builder/CvTemplatePreviewPage.tsx`
- `frontend/web-app/src/features/cv-builder/CvTemplatesPage.tsx`
- `frontend/web-app/src/lib/api/client.ts`
- `frontend/web-app/src/main.tsx`
- `frontend/web-app/src/styles/global.css`

## ST-1 bug register (recorded before implementation)

### ST-001 — Focused inline undo leaves stale visible text
- MODULE: CV Builder / inline editing
- STEPS TO REPRODUCE: create a blank CV; type `Nguyễn Văn An`, append
  ` kiểm tra`; press Ctrl+Z without leaving the name field; open Preview.
- EXPECTED: the editable DOM, editor state, preview and saved content agree.
- ACTUAL: the field still displays `Nguyễn Văn An kiểm tra`; Preview displays
  the empty-name placeholder. Redo restores the text. Unicode typing itself
  retained correct character order in this test.
- SEVERITY: HIGH
- ROOT CAUSE: global history restores React state, but EditableText ignores
  all value changes while focused, including genuine external restores.
- FIX PLAN: distinguish local input echoes from external history changes;
  synchronize only external changes, preserve ordinary typing/caret and IME;
  add focused undo/redo and composition regression coverage.
- STATUS: confirmed in browser; open.

### ST-002 — Expired-token responses are unreadable by the browser
- MODULE: Gateway / session recovery / CV persistence
- STEPS TO REPRODUCE: open CV Builder with an expired access token; edit and
  save. Independently GET `/api/v1/cvs` with an invalid bearer and Origin
  `http://localhost:5173`.
- EXPECTED: 401 with configured CORS headers, enabling the refresh interceptor.
- ACTUAL: 401 lacks Access-Control-Allow-Origin; UI remains apparently signed
  in but list/save fail. No refresh request follows the protected 401s.
- SEVERITY: HIGH
- ROOT CAUSE: JWT WebFilter order -200 returns before unordered CorsWebFilter.
- FIX PLAN: explicitly order CORS before JWT; test missing/expired/invalid
  tokens and disallowed origins without weakening authentication.
- STATUS: confirmed by browser, gateway logs and HTTP headers; open.

### ST-003 — Brand-new unsaved CV claims to be saved
- MODULE: CV Builder / save status
- STEPS TO REPRODUCE: open `/cv/new?template=classic&source=blank`.
- EXPECTED: indicate that a new draft has not yet been persisted.
- ACTUAL: toolbar says `Đã lưu` before any CV exists on the server.
- SEVERITY: MEDIUM
- ROOT CAUSE: initial blank snapshot is also initialized as savedSignature.
- FIX PLAN: distinguish persisted identity from unchanged blank draft.
- STATUS: confirmed in browser; open.

## Additional baseline reproductions

### ST-009 — Unsupported experience inference / Vietnamese years (HIGH)
- MODULE: AI deterministic matching.
- STEPS: score two experience entries without durations; score one entry saying
  `3 năm phát triển Java` against five years required.
- EXPECTED: unknown duration is explicitly unknown; Vietnamese three years is
  recognized as three years, not one entry/year.
- ACTUAL: two entries earn eight experience points (invented two years);
  Vietnamese three years earns four rather than twelve points.
- ROOT CAUSE: entry-count fallback and English-only years regex.
- FIX PLAN: remove inference; recognize explicit Vietnamese/English duration,
  label unknown evidence; preserve conservative scoring.
- STATUS: three new matching regressions fail on baseline as expected.

### ST-010 — Alias/duplicate skills produce incorrect gaps and counts (MEDIUM)
- MODULE: AI matching and skill presentation.
- STEPS: match REST API against REST; duplicate Java in requirements; inspect
  existing analyzed CV in browser.
- EXPECTED: equivalent skills match once; each displayed skill appears once.
- ACTUAL: REST listed missing despite REST API evidence; repeated skills in UI;
  duplicate required/preferred terms bias the score.
- ROOT CAUSE: exact-only equality, no cross-category de-duplication.
- FIX PLAN: explicit narrow alias mapping shared by score/gaps; normalize unique
  sets and remove required/preferred overlap; deduplicate persisted/display skills.
- STATUS: browser evidence plus failing scorer test before fix.

### ST-011 — Stale AI results remain after changing CV (HIGH)
- MODULE: AI Career selection/concurrency.
- STEPS: open a CV with a saved match, upload/select a new unanalysed CV.
- EXPECTED: hide results and actions from the previous CV.
- ACTUAL: old active match remains selected although new CV is current.
- ROOT CAUSE: selection paths reset different subsets of state; callbacks do not
  correlate responses with the currently selected CV/match.
- FIX PLAN: derive visible results by current IDs, guard asynchronous callbacks;
  test delayed responses and selection via upload/dropdown/list.
- STATUS: browser state and code path confirmed; open.

### ST-012 — Disabled AI query looks permanently loading / raw task label (MEDIUM)
- MODULE: AI UI.
- STEPS: select unanalysed CV and view recommendations/history.
- EXPECTED: prerequisite hint, translated history labels.
- ACTUAL: disabled recommendations query displays loading; `CANDIDATE ASSISTANT`
  is shown as a heading.
- ROOT CAUSE: isPending used instead of isLoading; missing enum label.
- FIX PLAN: explicit disabled state and centralized translated safe labels.
- STATUS: browser/code confirmed; open.

### Confirmed by added executable regressions

#### ST-004 — First-create response overwrites newer edits (HIGH)
- MODULE: CV autosave.
- STEPS: delay create response, type into another field while pending, release
  response, wait for next autosave.
- EXPECTED: latest content remains and is updated using the returned CV ID.
- ACTUAL: first server snapshot replaces newer content; no follow-up update.
- ROOT CAUSE: route hydration runs on the create response without marking the
  editor as already loaded; PDF also uses an independent save path.
- FIX PLAN: seed cache/identity without rehydrating live state; serialize save
  paths, disable conflicting actions and retain dirty state for later edits.
- STATUS: failing component regression reproduced before fix.

#### ST-005 — Invisible text on PDF continuation pages (HIGH)
- MODULE: PDF export.
- STEPS: export 30 Vietnamese experience entries in header layout, render page 2.
- EXPECTED: readable text from the normal top margin.
- ACTUAL: page 2 starts with white text on white background; rendered top text
  region contains zero dark pixels although text extraction succeeds.
- ROOT CAUSE: white text is selected by Y coordinate on all pages, while the
  colored banner is only drawn on page 1.
- FIX PLAN: make text color conditional on an actual banner; render-test pages.
- STATUS: reproduced by PDF renderer and visually inspected; open.

#### ST-006 — Sparse/Unicode CV can fail PDF export (HIGH)
- MODULE: PDF export.
- STEPS: optional contact fields null; paste Unicode/control characters.
- EXPECTED: sparse CV exports; supported Unicode preserved; unsupported glyphs
  handled without a server exception.
- ACTUAL: null optional fields throw at List.of; unsupported font glyphs are
  also passed directly to PDFBox without validation.
- ROOT CAUSE: null-intolerant contact list and no text/font sanitation.
- FIX PLAN: normalize optional text, NFC, whitespace/control handling and a
  visible fallback for missing font glyphs; test long unbroken text wrapping.
- STATUS: null contact failure reproduced in executable test; glyph case pending.

#### ST-007 — Legacy hidden custom section cannot be shown (MEDIUM)
- MODULE: CV sections.
- STEPS: load custom section visible=false, open Layout.
- EXPECTED: Show action reveals the section and preserves its items.
- ACTUAL: no Show action; updating design visibility alone cannot reveal it.
- ROOT CAUSE: two existing visibility representations are read inconsistently.
- FIX PLAN: derive effective visibility from both; Show updates both atomically.
- STATUS: failing component regression reproduced before fix.

ST-002 update: added test failed before fix; 16/16 Gateway tests pass after
ordering CORS before JWT. Docker rebuilt; the original expired browser session
successfully refreshed and saved QA CV `c97c99f0-26e2-4d7f-afb3-2153559e45c0`.

## Evidence / limitations so far

### ST-034 — Stale browser bundle renders a blank page after deployment (HIGH)
- MODULE: frontend route loading / recovery.
- STEPS: keep a signed-in tab open, replace frontend image, navigate to a lazy CV route.
- EXPECTED: readable recovery action if the old chunk is no longer available.
- ACTUAL: empty DOM, console `Failed to fetch dynamically imported module` for
  an old CvTemplatesPage asset.
- ROOT CAUSE: Suspense handles pending promises but no error boundary catches
  rejected imports/render errors.
- FIX PLAN: app-level Vietnamese recovery screen with explicit reload; do not
  silently reload or clear owner-scoped drafts; test thrown import/render error.

### ST-031 — Typing can race automatic profile import (HIGH)
- MODULE: CV create-from-profile.
- STEPS: delay from-profile response; the blank editor remains editable while
  import runs; type before the response navigates to the new saved CV.
- EXPECTED: no editable blank document until the requested snapshot is ready.
- ACTUAL: imported snapshot can replace text entered during the pending request.
- ROOT CAUSE: only write buttons are disabled, not the initial profile-import surface.
- FIX PLAN: explicit loading state for automatic import; retain an error/retry
  path and allow blank editing after an import failure. Test the pending state.

### ST-032 — Resume analysis bypasses Vietnamese response enforcement (HIGH)
- MODULE: AI resume generation.
- STEPS: analyze long English TXT; inspect summary and optional contact fields.
- EXPECTED: Vietnamese prose; missing contacts absent, explicit summary duration usable.
- ACTUAL: successful result contains an English summary and `Not provided in CV`;
  five years in summary is ignored when experience contains dates only.
- ROOT CAUSE: resume calls provider directly with prompt-only language instruction;
  placeholder normalization is too narrow; duration scorer reads only experience.
- FIX PLAN: one bounded language rewrite with original facts; fail safely rather
  than inventing a fallback resume; clean explicit missing-value placeholders and
  recognize explicit duration in summary without summing jobs or inferring dates.

Escape/blur investigation was **not a defect**: existing code already restores
DOM before blur. A passing regression test was added; it is not counted as a bug.

### ST-029 — Negative certificate evidence earns positive credit (HIGH)
- MODULE: AI extraction / matching.
- STEPS: analyze mixed DOCX with `No AWS experience. No professional certificates.`;
  model puts the Vietnamese negative sentence in certificates; score a job requiring certification.
- EXPECTED: absence is not evidence of a certificate.
- ACTUAL: non-empty array is treated as certification present.
- ROOT CAUSE: validator keeps absence prose and CertificateScorer checks only array size.
- FIX PLAN: conservative explicit-absence normalization plus scorer defense for
  legacy stored results; regression must retain genuine certificates/NoSQL terms.

### ST-030 — Mobile navigation header overlaps template dialog (MEDIUM)
- MODULE: CV responsive modal.
- STEPS: open template picker at 390x844.
- EXPECTED: dialog stays above navigation with fully visible title/close action.
- ACTUAL: fixed app header overlays the top of the dialog.
- ROOT CAUSE: modal stacking layer is below the shared header and dialog height
  has no viewport-bound outer scroll.
- FIX PLAN: raise the modal layer and bound the entire dialog to dynamic viewport.

### ST-028 — Unavailable sans-serif fonts fall back to browser serif (MEDIUM)
- MODULE: CV template typography.
- STEPS: preview Developer/Roboto or Data Professional/Open Sans without those
  fonts installed; inspect the visible serif glyphs and inline font-family.
- EXPECTED: a readable sans-serif fallback consistent with the chosen family.
- ACTUAL: only one unbundled font name is specified, so browser default is serif.
- ROOT CAUSE: missing CSS fallback stack; only Inter/Manrope are externally loaded.
- FIX PLAN: explicit Arial/sans-serif or Georgia/serif fallback, no new external
  font dependency. Exact unavailable font faces remain a documented limitation.

### ST-025 — Skills omit duplicate and item-reorder controls (MEDIUM)
- MODULE: CV repeatable items.
- STEPS: add two skills; look for duplicate and move actions available on experience.
- EXPECTED: the requested repeatable-item operations also work for skills.
- ACTUAL: skill renderer exposes only edit/delete.
- ROOT CAUSE: special chip renderer bypasses the shared ItemControls.
- FIX PLAN: reuse shared controls, retain content/history handling, test skill operations.

### ST-026 — Application timestamps lack a timezone (MEDIUM, open)
- MODULE: candidate application tracking.
- STEPS: apply at 10:48 Asia/Bangkok in Docker; inspect list/detail timestamps.
- EXPECTED: an explicitly zoned instant displayed in the user's timezone.
- ACTUAL: UI shows 03:48, the Docker UTC wall-clock value.
- ROOT CAUSE: backend LocalDateTime serializes without an offset; browser treats
  the zone-less value as local. Existing non-Docker servers may use another zone.
- FIX PLAN: migrate event timestamps to an explicit instant/offset contract.
  Deferred: guessing UTC for every legacy zone-less value would corrupt display
  of records created by non-UTC deployments. Dates and application state persist.

### ST-027 — Public footer links are placeholders / partial non-AI localization (LOW, open)
- MODULE: shared navigation and candidate shell.
- STEPS: inspect footer CV/AI links and candidate application headings.
- ACTUAL: several footer links point to `/`; shell still contains Career workspace,
  Candidate Portal and Planned. Main CV/AI navigation remains functional.
- ROOT CAUSE: legacy scaffold footer and untranslated shared-shell strings.
- FIX PLAN: product-wide navigation/localization follow-up; do not invent routes
  for unimplemented support, policy and cover-letter destinations in this phase.

### ST-024 — Private query cache survives account changes (HIGH)
- MODULE: frontend authentication/query isolation.
- STEPS: cache a candidate profile/CV, log out, sign in as another candidate in
  the same SPA, revisit a query whose key does not contain the account ID.
- EXPECTED: no prior-account cached content, including delayed responses.
- ACTUAL: AppProviders owns one process-wide QueryClient and AuthProvider never
  clears/replaces it; CV/profile keys can be reused across identities.
- ROOT CAUSE: cache lifetime is app-scoped rather than authenticated-owner-scoped.
- FIX PLAN: create a separate QueryClient for each identity transition; cancel
  and clear retired clients, test delayed writes to an old cache cannot appear
  in the new session. Do not rely solely on eventual server authorization.
- NOTE: the two notifications seen after registration are global broadcasts,
  not evidence of private notification leakage; their query keys are scoped.

### ST-023 — Log-redaction test races asynchronous completion (LOW)
- MODULE: Gateway regression harness.
- STEPS: run gateway suite while Docker/reactor builds run concurrently.
- EXPECTED: wait for the request's log and assert no credentials occur.
- ACTUAL: one run sees an empty appender before doFinally writes the log.
- ROOT CAUSE: response completion and asynchronous logging are not synchronous;
  test reads a non-thread-safe list immediately after receiving HTTP 200.
- FIX PLAN: thread-safe capture and bounded wait for that request's event;
  retain all token/Authorization redaction assertions unchanged.

### ST-021 — Temporary CV outage is labelled as missing/forbidden (MEDIUM)
- MODULE: CV initial load/error recovery.
- STEPS: open CV during user-service restart.
- EXPECTED: temporary-service message and retry without claiming data deletion.
- ACTUAL: `Không tìm thấy CV hoặc bạn không có quyền truy cập.` for HTTP 503.
- ROOT CAUSE: every query error has the same hardcoded render with no retry.
- FIX PLAN: use normalized status/message and explicit refetch action.

### ST-022 — Gateway fails after upstream container replacement (HIGH)
- MODULE: Docker/Gateway recovery.
- STEPS: replace user-service container, wait for health, open saved CV.
- EXPECTED: Gateway resolves the replacement service and recovers.
- ACTUAL: repeated 503 at 03:22 and 03:23 UTC while an independent request from
  the Gateway container to user-service:8082/actuator/health returns UP.
- ROOT CAUSE: evidence is consistent with cached Docker DNS addresses in the
  long-lived Netty client; no bounded DNS cache is configured. Confirm by
  repeating replacement with bounded resolver TTL and without Gateway restart.
- FIX PLAN: cap positive/negative resolver cache TTL; do not retry mutating
  requests automatically; verify restart recovery in runtime.

### ST-018 — Saved CV stays dirty after custom sections (HIGH)
- MODULE: CV persistence.
- STEPS: add custom sections, save and export, observe toolbar after completion.
- EXPECTED: successful unchanged snapshot becomes saved; no false recovery draft.
- ACTUAL: toolbar remains `Có thay đổi chưa lưu`; navigation warns again.
- ROOT CAUSE: JSON.stringify compares insertion order of object keys; custom
  sections and visibility maps have different key order after normalization.
- FIX PLAN: canonical object-key ordering for signatures, preserve array order;
  regression with semantically equal, reordered server response.

### ST-019 — Mobile editor text is too small / zoom reflows document (MEDIUM)
- MODULE: CV responsive canvas.
- STEPS: open populated CV at 390x844 and adjust zoom.
- EXPECTED: stable document layout, readable edit zoom, locally scrollable canvas.
- ACTUAL: contact/body font computed around 8.8px before transform; zoom also
  changes document width, so layout changes rather than simply scaling.
- ROOT CAUSE: responsive font shrinking combined with inverse width/transform.
- FIX PLAN: fixed A4 editing width, one zoom scale, scroll contained to stage.

### ST-020 — PDF sidebar does not contain sidebar sections (MEDIUM)
- MODULE: PDF design fidelity.
- STEPS: choose sidebar-left with education/skills; download and render PDF.
- EXPECTED: sidebar sections remain in their column as in preview.
- ACTUAL: empty colored gutter; every section is in the main column.
- ROOT CAUSE: PDF writer only changes margin/background for sidebar layouts.
- FIX PLAN: real independent column flows and continuation-page rendering;
  assert text positions and inspect exported browser PDF.

### ST-014 — Custom heading case is changed while typing (MEDIUM)
- MODULE: CV inline editing.
- STEPS: rename custom section to `Ngoại ngữ`.
- EXPECTED: preserve the exact input in state and persistence.
- ACTUAL: accessible labels become `NGOẠI NGỮ` after input.
- ROOT CAUSE: innerText includes inherited CSS text-transform: uppercase.
- FIX PLAN: disable text-transform on editable nodes, preserve display-only
  heading styles outside editing; browser retest exact input after refresh.

### ST-015 — Concurrent analysis has no atomic claim (HIGH)
- MODULE: AI resume analysis.
- STEPS: submit analyze for the same resume while the first provider call runs.
- EXPECTED: one active analysis; second request receives a clear conflict.
- ACTUAL: each request creates a RUNNING task and calls the provider; competing
  result writes can violate the unique resume-analysis constraint.
- ROOT CAUSE: createTask has no subject-level lock or active-task check.
- FIX PLAN: executable concurrent regression, short database row-lock claim,
  bounded stale-task recovery; no transaction held across provider generation.
- STATUS: code-confirmed race; executable reproduction pending.

### ST-016 — Language guard accepts mostly-English mixed output (MEDIUM)
- MODULE: AI language policy.
- STEPS: return a Vietnamese greeting plus long English advice, or multiline
  Vietnamese prose without common stop words.
- EXPECTED: reject English-dominant prose, accept Vietnamese multiline text.
- ACTUAL: two Vietnamese stop words bypass the English count; regex dot does
  not span newlines when detecting Vietnamese accents.
- ROOT CAUSE: aggregate stop-word threshold and matches without DOTALL.
- FIX PLAN: regression cases, per-prose-field validation, retain original
  factual context on the single language retry.

### ST-017 — AI timeout budgets disagree (HIGH)
- MODULE: synchronous AI calls through frontend/Gateway.
- STEPS: provider needs over 120 seconds (configured limit 180 seconds), or a
  language rewrite requires a second generation.
- EXPECTED: bounded provider completion/failure arrives before proxy/client timeout.
- ACTUAL: client and Gateway stop at 120 seconds while provider can keep running.
- ROOT CAUSE: independent budgets, no allowance for one permitted rewrite.
- FIX PLAN: explicit bounded AI-route budget shared with client documentation;
  preserve short timeouts for non-AI routes and cap provider calls.
- STATUS: configuration mismatch confirmed; slow-provider regression pending.

### ST-013 — Add-section buttons are covered by reorder controls (HIGH)
- MODULE: CV section management.
- STEPS: click `Thêm Kinh nghiệm` on the CV canvas at 1440x900.
- EXPECTED: append an experience item and expose its editable fields.
- ACTUAL: section moves down instead; repeated clicks on add buttons perform
  reorder/hide operations. DOM hit-test at the add-button center resolves to
  `Di chuyển section xuống`.
- ROOT CAUSE: both controls are absolutely positioned at the same right/bottom
  coordinates; the reorder toolbar has higher z-index and captures clicks.
- FIX PLAN: put editing controls in non-overlapping normal-flow rows, reserve
  touch targets; verify actual hit-testing and browser add/edit/remove actions.
- STATUS: browser interaction, screenshot and elementFromPoint confirmed.

### ST-008 — Immediate in-app navigation silently loses edits (HIGH)
- MODULE: CV autosave/recovery.
- STEPS: in saved QA CV, type `QA nội dung trước khi rời trang` into Headline;
  immediately click `CV của tôi`; reopen the same CV.
- EXPECTED: recover the pending edit or block navigation with clear warning.
- ACTUAL: headline is empty again, no warning or recoverable draft.
- ROOT CAUSE: 900ms timer is cancelled on unmount; beforeunload does not protect
  router navigation and there is no local recovery journal.
- FIX PLAN: tab-local, owner-scoped recovery journal written synchronously
  before paint, explicit restore/discard UI, preserve drafts on failed saves;
  do not silently overwrite a newer server snapshot.
- STATUS: reproduced in browser before implementation.

- Browser entered Vietnamese Unicode successfully; this is NOT proof of a
  native Telex/VNI IME composition session.
