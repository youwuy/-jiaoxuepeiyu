# Backend Completion Audit

Date: 2026-07-31
Branch: `chen/backend`
Last audited baseline commit: `894f21a feat: add admin job role config APIs`

## Scope

This audit tracks evidence toward the requested backend deliverable:

- Java backend compatible with JDK 1.8.
- MySQL 5.7.42.0 compatible schema and data access.
- Student-side backend APIs.
- Management-side backend APIs.
- Swagger / OpenAPI interface documentation.
- Deployment package layout with bundled Java runtime support.
- Code pushed to `chen/backend` and synchronized to `main` after major modules.

## Proven By Current Repository State

These items have code-level evidence in the repository:

- Common API envelope, paging response, exception handling, validation helpers, score/progress calculators, and health endpoint.
- Admin and student login APIs, bearer token sessions, current user lookup, logout, and authenticated password change.
- Student APIs for courses, courseware progress, public resources, messages, profile, assignments, semester scores, training center rooms, role claim/release, training attempt submission, score sheets, and training archives.
- UE training APIs for launch metadata, live status callback, and immutable result callback.
- Admin APIs for organization tree, education configuration, classroom/NVR metadata, score weights, score grade rules, account management, resource management, question bank, paper management, course management, courseware learning windows, assignment completion rules, assignment review, training management, training monitor, device efficiency, semester scores, training archives, profile, permission/menu management, and IAM role management.
- Swagger annotations are present on controller methods, and `springdoc-openapi-ui` exposes runtime docs at `/v3/api-docs` and `/swagger-ui.html` after backend startup.
- Deployment scripts exist for Linux/macOS and Windows, and packaging scripts require `JRE8_HOME` or `deploy/runtime/jre8` so generated packages include `runtime/jre8`.
- Admin file export endpoints now provide Excel-compatible UTF-8 BOM CSV downloads at `/export/file` while preserving JSON export endpoints.
- Admin account management now supports backend-level fuzzy `jobTitle` filtering for teacher/student paged lists, JSON exports, and CSV exports.
- The latest completed backend module has been pushed to `origin/chen/backend` and fast-forwarded to `origin/main`.
- First-admin bootstrap code creates an optional environment-configured admin at startup only when no admin exists, stores only the hashed password, and documents the one-time deployment variables.
- Static backend and deployment verification scripts can check Java 8 compiler settings, OpenAPI dependency presence, MySQL target metadata, bundled JRE packaging rules, first-admin bootstrap configuration, and the absence of seeded `sys_user` rows in SQL init scripts without starting the service.
- GitHub CI now has a packaged-backend smoke script that starts the jar, verifies `/api/health`, logs in through the bootstrap admin, checks `/api/auth/current`, and logs out against MySQL 5.7.42.
- Student training attempt APIs now expose `/api/student/training-attempts` and `/api/student/training-attempts/{attemptId}/score-sheet` while reusing the UE result callback and archive score-sheet pipeline.
- Student training room APIs now include role release for waiting rooms, constrained to the current student's own claimed role.
- Admin teaching course APIs now support PDF-specified teaching-time overlap filtering, unpublished status filtering, pending-review counts in course lists, 20-character course/chapter validation, courseware learning windows, assignment answer windows, assignment publish modes, and submit/pass-score completion rules.
- Admin teaching course APIs now support up to 3 levels of nested chapter trees, persist `course_chapter.parent_chapter_id`, return nested chapter `children` in admin course detail/list payloads, and keep student course detail responses compatible by returning nested content in path order.
- Admin organization management extends the existing `/api/admin/org` module instead of adding duplicate organization APIs, and now covers PDF page 44-47 behavior for fuzzy search, enabled-only parent picker trees, 20-character trimmed names, sibling-name uniqueness, audit fields, drag sort persistence, immutable parent editing, selected-node enable, and selected-node-plus-descendants disable.

## Not Yet Proven

These items are not complete until stronger evidence is collected:

- `mvn test` has not run successfully in this workspace because `mvn` is not installed.
- `mvn package` has not run successfully in this workspace because Maven and a JDK are not available on `PATH`.
- `BootstrapAdminServiceTests` has not run successfully in this workspace because `mvn` is not installed.
- `scripts/ci-backend-smoke.sh` has not been shell-syntax checked locally because `bash` is not installed in this workspace; GitHub CI runs it on Ubuntu.
- GitHub CI is configured to run backend static checks, deployment layout checks, MySQL 5.7.42 schema import, `mvn test`, package checks, and packaged-jar smoke checks; the previous workflow runs failed before jobs were created, so the workflow expression syntax needs re-verification after the next push.
- The Spring Boot service has not been started locally, so runtime Swagger pages, `/api/health`, login, current user, and representative module smoke tests have not been verified.
- MySQL 5.7.42.0 schema import has not been tested against a real MySQL 5.7.42.0 instance.
- A final deployment package has not been generated because no JRE 8 source directory is available in this workspace.
- Static published OpenAPI JSON under `docs/api/openapi.json` may be stale after recent controller additions; runtime Swagger will be correct after startup, but the static docs need regeneration during release verification.
- The requirement PDFs still need a final requirement-by-requirement audit against implemented endpoints, database tables, docs, and runtime behavior.

## Delivery Risks

- There are no committed default users or plaintext passwords in `database/init`, by design. First deployment should use the optional one-time bootstrap admin environment variables, then remove them after first successful startup. Student login works only after an admin creates or resets a student account with `APP_ACCOUNT_INITIAL_PASSWORD` / `app.account.initial-password` configured.
- Binary `.xlsx` parsing and generation are intentionally not implemented in the backend. Current import APIs accept parsed JSON rows, and current file exports generate Excel-compatible CSV files.
- Camera/NVR passwords are stored as configured metadata. Release deployment should ensure database access and logs do not expose these values.
- Some plan checklist files still contain unchecked verification or commit steps; treat the repository code and this audit as the current source of truth, then close stale checklist items when their evidence is verified.

## Next Backend Priorities

1. Run full Java verification on a machine with JDK 8 and Maven: `mvn test` and `mvn package` in `backend/`.
2. Verify MySQL 5.7.42.0 import using all `database/init/*.sql` files in order.
3. Start the backend and run smoke checks for `/api/health`, admin login, student login, `/api/auth/current`, and one representative endpoint per completed module.
4. Regenerate and republish static OpenAPI artifacts after the backend starts successfully.
5. Build a real deployment package with `JRE8_HOME` or `deploy/runtime/jre8`, then run `scripts/verify-deploy-layout.ps1`.
6. Complete the PDF requirement-by-requirement audit and implement any missing behavior found by that audit.
