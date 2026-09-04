# Final P2 Git inventory — 2026-09-04 (authoritative)

Pre-stage inventory: **106 modified tracked files, 42 untracked files (40 default status entries), 0 staged**. Expanded `git status --untracked-files=all` contains 148 individual intended project paths: 104 source/QA-script paths, 35 tests, 3 migrations, 3 configurations and 3 documents. No review-unknown or generated path was found. Full classification is retained in ignored evidence at `runtime-logs/st-qa/final-p2-20260904/git-path-classification.json`.

New P2 artifacts intended for release are `docs/MANUAL_WINDOWS_IME_QA.md` and `frontend/web-app/src/features/admin/admin.labels.test.ts`. Existing reports are updated in place. Route lazy loading modifies `AppRouter.tsx`; timestamp regression extends `ApplicationAuthorizationIntegrationTest.java`; localization reuses `admin.labels.ts` and the shared application status presenter across Candidate, Employer and Admin surfaces.

Excluded from staging by ignore rules: `runtime-logs/st-qa/**`, all Maven `target/**`, frontend `node_modules/**`, build `dist/**`, cached sessions and temporary bootstrap helpers. The final frontend image was inspected and contains none of the localhost QA helper markers. Synthetic QA database content and Docker volumes remain intact.

---
# Final release inventory — 2026-09-04 (authoritative)

Release status: **RELEASE READY** with physical native IME explicitly **NOT VERIFIED**. Final working tree: **104 modified + 38 untracked, 0 staged**. The incoming 96 modified + 36 untracked checkpoint and all earlier changes remain preserved. `HEAD` and `origin/main` remain `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`; no commit or push occurred.

Final code additions to the retained working tree include:

| Path | Classification | Action |
|---|---|---|
| `frontend/web-app/src/app/router/AppRouter.tsx` | SOURCE CODE | KEEP; adds protected Employer notification route |
| `frontend/web-app/src/features/notifications/NotificationPage.tsx` | SOURCE CODE | KEEP; role-aware copy, cache scope and application deep links |
| `frontend/web-app/src/features/employer/components/EmployerHeader.tsx` | SOURCE CODE | KEEP; notification entry point |
| `frontend/web-app/src/features/employer/components/EmployerSidebar.tsx` | SOURCE CODE | KEEP; notification navigation |
| `frontend/web-app/src/features/employer/employer-portal.css` | SOURCE CODE | KEEP; compact notification action and 768 px overflow fix |
| `backend/application-service/.../ApplicationResponse.java` | SOURCE CODE | KEEP; computed latest `updatedAtInstant` |
| `backend/application-service/.../ApplicationSummaryResponse.java` | SOURCE CODE | KEEP; computed latest `updatedAtInstant` |
| `backend/application-service/.../ApplicationStatusHistoryRepository.java` | SOURCE CODE | KEEP; batch status-history lookup |
| `backend/application-service/.../ApplicationServiceImpl.java` | SOURCE CODE | KEEP; populates latest explicit instant |
| `frontend/web-app/src/features/admin/admin.labels.ts` | SOURCE CODE | KEEP; centralized notification/application/company labels |

Ignored final evidence retained under `runtime-logs/st-qa/final-release-20260904/` includes the real-provider JSONL, 120-check authenticated responsive evidence, Employer notification breakpoint evidence, timezone/API/UI evidence, localization and credential scans, backend/gateway/frontend regression logs, Docker build log and 14/14 health snapshot. Browser-session bootstrap/helper files remain ignored QA artifacts and contain no embedded credential. The helper process was stopped; port 8765 is not part of the final stack. The temporary bootstrap was removed from the running frontend by recreating it from the final image.

Synthetic QA data intentionally retained for reproducibility includes terminal HIRED/REJECTED applications, the ST-026 status-transition application, seven resume fixtures, the Unicode CV editor record `89eef2ad-53c0-4bc0-a3a1-87192d954438`, QA jobs/catalog/template records and notifications. No real account/data was used or changed; no Docker volume or QA artifact was deleted.

The final credential scan includes ignored QA evidence and reports zero JWT, OpenAI key, private-key or plaintext QA-password literal hits. Eight historical PowerShell QA scripts now read `$env:QA_PASSWORD`; `auth_bridge.py` reads environment variables and `token_session_bridge.py` resolves an already-authorized cached localhost session without persisting tokens.

---
# Latest inventory update — 2026-09-04

Current state: **96 modified + 36 untracked, 0 staged**. Incoming 72 modified + 32 untracked remains preserved. HEAD and local `origin/main` remain `ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`. No commit or push.

New untracked review candidates added by this continuation:

| Path | Classification | Action |
|---|---|---|
| `backend/ai-service/src/main/java/com/recruitment/ai/assistant/RecruiterAnswerPolicy.java` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/test/java/com/recruitment/ai/assistant/RecruiterAnswerPolicyTest.java` | TEST CODE | KEEP for review |
| `backend/application-service/src/main/resources/db/migration/V4__add_explicit_application_instants.sql` | SOURCE CODE / MIGRATION | KEEP for review; do not backfill legacy timestamps blindly |
| `frontend/web-app/src/features/admin/admin.labels.ts` | SOURCE CODE | KEEP for review |

The continuation also modifies the existing AI validator/policies/services/tests, application entities/DTOs/service/integration tests, application display call sites, Admin/Employer localization surfaces and the two QA reports. Docker images were rebuilt from current source while persistent data volumes were retained. Generated Maven, frontend build and Testcontainers artifacts remain build output and are not staged.

---
# Final QA artifact inventory

No source, test, artifact or Git history was deleted or discarded. No staging, commit or push.

Final status after the authorized localhost continuation: **72 modified + 32 untracked status entries**.
The incoming checkpoint was 67 modified + 28 untracked; all were preserved.
The latest P0 pass additionally modifies ResumeAnalysisJsonValidator,
RecommendationServiceImpl and ResumePipelineIntegrationTest and retains the prior
JobDetailsPage/CareerCompanion changes. New grounding code/tests are listed below.
The latest continuation also modifies `AdminCatalogPage.tsx` and adds its focused
route-state regression test. HEAD and local origin/main remain
`ca6ade684f3e0cd0a6329a9e9663947ee4ae0c78`.
`git diff --check` passes. Nothing is staged; these are future review candidates,
not authorization to commit while release blockers remain.

## Untracked files

| Path | Classification | Action |
|---|---|---|
| `backend/ai-service/src/main/java/com/recruitment/ai/assistant/CandidateAnswerPolicy.java` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/test/java/com/recruitment/ai/assistant/CandidateAnswerPolicyTest.java` | TEST CODE | KEEP for review |
| `backend/ai-service/src/main/java/com/recruitment/ai/recommendation/GroundedRecommendationComposer.java` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/test/java/com/recruitment/ai/recommendation/GroundedRecommendationComposerTest.java` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/jobs/JobDetailsPage.test.tsx` | TEST CODE | KEEP for review |
| `backend/ai-service/src/main/java/com/recruitment/ai/explanation/CompactExplanation.java` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/main/java/com/recruitment/ai/interview/CompactInterview.java` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/main/java/com/recruitment/ai/service/impl/ExplanationTaskWorker.java` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/main/resources/db/migration/V8__durable_explanation_tasks.sql` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/main/resources/db/migration/V9__compact_interview_prompt.sql` | SOURCE CODE | KEEP for review |
| `backend/ai-service/src/test/java/com/recruitment/ai/assistant/CandidateAssistantTaskTest.java` | TEST CODE | KEEP for review |
| `backend/ai-service/src/test/java/com/recruitment/ai/explanation/CompactExplanationTest.java` | TEST CODE | KEEP for review |
| `backend/ai-service/src/test/java/com/recruitment/ai/interview/CompactInterviewTest.java` | TEST CODE | KEEP for review |
| `backend/ai-service/src/test/java/com/recruitment/ai/service/analysis/ResumeAnalysisJsonValidatorTest.java` | TEST CODE | KEEP for review |
| `backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayHttpClientConfig.java` | SOURCE CODE | KEEP for review |
| `backend/user-service/src/main/java/com/recruitment/user/dto/cv/CvTemplateCatalog.java` | SOURCE CODE | KEEP for review |
| `docs/FINAL_QA_ARTIFACT_INVENTORY.md` | DOCUMENTATION | KEEP for review |
| `docs/SYSTEM_HARDENING_REAL_USER_QA.md` | DOCUMENTATION | KEEP for review |
| `frontend/web-app/src/app/providers/AppErrorBoundary.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/app/providers/AppErrorBoundary.tsx` | SOURCE CODE | KEEP for review |
| `frontend/web-app/src/app/providers/AppProviders.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/components/navigation/Footer.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/admin/AdminCatalogPage.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/ai-career/AiCareerPage.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/cv-builder/CvEditorPage.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/cv-builder/components/CvPreview.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/cv-builder/components/EditableText.test.tsx` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/cv-builder/cv.draft.ts` | SOURCE CODE | KEEP for review |
| `frontend/web-app/src/features/cv-builder/cv.presets.ts` | SOURCE CODE | KEEP for review |
| `frontend/web-app/src/features/cv-builder/cv.templates.test.ts` | TEST CODE | KEEP for review |
| `frontend/web-app/src/features/employer/employer-company.schemas.test.ts` | TEST CODE | KEEP for review |
| `scripts/qa/admin_role_matrix.py` | QA SCRIPT | KEEP for review |
| `scripts/qa/final_ai_quality.py` | QA SCRIPT | KEEP for review |
| `scripts/qa/final_pdf_exports.py` | QA SCRIPT | KEEP for review |

## Ignored evidence

`runtime-logs/st-qa/` is already ignored by the root `.gitignore`. Keep locally for reproduction; do not commit its logs, generated JSON, PNG screenshots, PDF/DOCX/TXT CV fixtures, compiled helpers or old credential-bearing scripts.

The reusable scripts in `scripts/qa/` use environment variables for credentials and write only into the ignored evidence folder. Keep `.env` ignored. Do not add Docker volumes, Maven target directories or node_modules. No additional broad PDF/DOCX ignore rule is needed because it could hide legitimate documentation.

The QA run also left seven synthetic resume uploads and four synthetic export CVs
in local services for reproducibility. The original edited CV content/template
was restored. Synthetic service records were not bulk-deleted; exported storage
copies, persistence hashes and evidence helpers remain only in the ignored QA folder.

The authorized continuation additionally retained four synthetic published jobs:
two terminal-workflow jobs and two route-state regression jobs. Three synthetic
applications were created (HIRED, REJECTED, and APPLIED); consent/preferences were
saved for the approved QA Candidate. Terminal status history and notification-read
changes are intentional QA data. No account roles, credentials or Admin grants
were changed. Fresh CV Unicode text was restored to its original experience text.
Three new persisted job-recommendation rows also remain for this synthetic Candidate.
The synthetic Admin category created for the final route-state regression, the
edited QA benefit, earlier QA category/skill records and the QA notification
template were soft-deactivated after their final assertions. No notification was
sent solely to create delivery-log rows, and no production/non-QA service data was
deleted for cleanup.
A generated Python bytecode file was moved from `scripts/qa/__pycache__/` into
ignored evidence; no source was deleted.
New logs, screenshots, JSONL outputs and helper scripts are under
`runtime-logs/st-qa/authorized-final/`; keep them ignored, including the initial
failed-expectation and startup-503 evidence. Do not stage credential-bearing helpers.

## Existing modifications

All modified source, tests, migrations, frontend dependency files and `.gitignore` are retained. Review the entire diff together with the QA report before a future commit; release remains blocked as detailed in the report.

## Current modified paths to review

- `.gitignore`
- `backend/ai-service/src/main/java/com/recruitment/ai/assistant/CandidateAssistantTask.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/assistant/VietnameseGenerationPolicy.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/assistant/VietnameseResponsePolicy.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/config/OllamaProperties.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/config/OpenAiProperties.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/controller/ExplanationInterviewController.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/entity/AiTask.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/exception/ErrorCode.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/engine/RuleBasedMatchingEngine.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/rule/JobRequirementsParser.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/CertificateScorer.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/ExperienceScorer.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/scorer/SkillScorer.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/matching/util/MatchingText.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/provider/OllamaStructuredGenerationProvider.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/repository/AiTaskRepository.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/repository/JobMatchResultRepository.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/repository/ResumeDocumentRepository.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/ExplanationInterviewService.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/analysis/ResumeAnalysisJsonValidator.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/impl/AssistantServiceImpl.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/impl/CareerCompanionServiceImpl.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/impl/ExplanationInterviewServiceImpl.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/impl/RecommendationServiceImpl.java`
- `backend/ai-service/src/main/java/com/recruitment/ai/service/impl/ResumeServiceImpl.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/assistant/VietnameseGenerationPolicyTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/assistant/VietnameseResponsePolicyTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/integration/MatchingIntegrationTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/integration/ResumePipelineIntegrationTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/matching/MatchingScorersTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/provider/OllamaStructuredGenerationProviderTest.java`
- `backend/ai-service/src/test/java/com/recruitment/ai/service/impl/CareerCompanionServiceImplTest.java`
- `backend/ai-service/src/test/resources/application-test.yml`
- `backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayCorsConfig.java`
- `backend/api-gateway/src/main/java/com/recruitment/gateway/config/GatewayRouteConfig.java`
- `backend/api-gateway/src/test/java/com/recruitment/gateway/GatewayIntegrationTest.java`
- `backend/company-service/src/main/java/com/recruitment/company/dto/request/CreateCompanyRequest.java`
- `backend/company-service/src/test/java/com/recruitment/company/integration/CompanyAuthorizationIntegrationTest.java`
- `backend/user-service/src/main/java/com/recruitment/user/dto/request/CreateCvFromProfileRequest.java`
- `backend/user-service/src/main/java/com/recruitment/user/dto/request/SaveCandidateCvRequest.java`
- `backend/user-service/src/main/java/com/recruitment/user/service/CandidateCvService.java`
- `backend/user-service/src/main/java/com/recruitment/user/service/CvPdfService.java`
- `backend/user-service/src/test/java/com/recruitment/user/integration/CandidateCvIntegrationTest.java`
- `backend/user-service/src/test/java/com/recruitment/user/service/CvPdfServiceTest.java`
- `frontend/web-app/package-lock.json`
- `frontend/web-app/package.json`
- `frontend/web-app/src/app/providers/AppProviders.tsx`
- `frontend/web-app/src/components/navigation/Footer.tsx`
- `frontend/web-app/src/components/navigation/UserMenu.tsx`
- `frontend/web-app/src/components/navigation/footer.css`
- `frontend/web-app/src/features/admin/AdminCatalogPage.tsx`
- `frontend/web-app/src/features/ai-career/AiCareerPage.tsx`
- `frontend/web-app/src/features/ai-career/ai-career.api.ts`
- `frontend/web-app/src/features/ai-career/ai-career.labels.ts`
- `frontend/web-app/src/features/candidate/candidate-page.css`
- `frontend/web-app/src/features/candidate/components/CandidateHeader.tsx`
- `frontend/web-app/src/features/candidate/components/CandidateSidebar.tsx`
- `frontend/web-app/src/features/cv-builder/CvEditorPage.tsx`
- `frontend/web-app/src/features/cv-builder/CvTemplatePreviewPage.tsx`
- `frontend/web-app/src/features/cv-builder/CvTemplatesPage.tsx`
- `frontend/web-app/src/features/cv-builder/components/CvPreview.tsx`
- `frontend/web-app/src/features/cv-builder/components/EditableText.tsx`
- `frontend/web-app/src/features/cv-builder/cv-builder.css`
- `frontend/web-app/src/features/cv-builder/cv.templates.ts`
- `frontend/web-app/src/features/cv-builder/cv.types.ts`
- `frontend/web-app/src/features/employer/components/EmployerCompanyForm.tsx`
- `frontend/web-app/src/features/employer/employer-company.schemas.ts`
- `frontend/web-app/src/features/jobs/JobDetailsPage.tsx`
- `frontend/web-app/src/lib/api/client.ts`
- `frontend/web-app/src/main.tsx`
- `frontend/web-app/src/styles/global.css`
