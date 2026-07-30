# Project Memory

## Long-Term Context

- 2026-07-30: User is acting as Chen Gong and wants the full backend implemented from management/student requirement PDFs.
- 2026-07-30: Required backend constraints are Java on JDK 1.8, MySQL 5.7.42.0, and deployment with bundled Java runtime/library support.
- 2026-07-30: Repository remote is `https://github.com/youwuy/-jiaoxuepeiyu.git`.
- 2026-07-30: Backend work branch is `chen/backend`.
- 2026-07-30: Requirement analysis documents exist in the parent working directory as `PRD.md` and `Tech-Spec.md`.
- 2026-07-30: User explicitly redirected backend implementation priority to the student-side backend first.
- 2026-07-30: First student backend checkpoint covers course list, public resources, messages/read state, and profile updates with a temporary `X-User-Id` student identity header for frontend integration.
- 2026-07-30: Second student backend checkpoint adds course detail trees, courseware progress reporting, and theory assignment detail/save/submit/report APIs; training room workflows, comprehensive scores, and archives still remain.
- 2026-07-30: Third student backend checkpoint adds semester comprehensive score queries and training archive list/detail APIs; student training listing and room workflows remain the largest student-side gap.
- 2026-07-30: Fourth student backend checkpoint adds training center list, UE app installation status, and team room create/join/leave/role-claim/start APIs with transaction-backed service rules.
- 2026-07-30: First admin backend checkpoint adds organization tree CRUD/status APIs and education configuration APIs for academic years, semesters, majors, and classes using existing foundation tables.
- 2026-07-30: Admin account management uses `APP_ACCOUNT_INITIAL_PASSWORD` / `app.account.initial-password` for teacher/student create and reset password flows; plaintext initial passwords are not returned by APIs.
- 2026-07-30: Admin account detail/list responses mask phone and ID card fields to avoid exposing raw sensitive identity data.

## Operational Notes

- Do not store secret values in this file.
- The repository contains `backend/`, `frontend/`, `database/`, `deploy/`, `docs/`, and `scripts/`.
