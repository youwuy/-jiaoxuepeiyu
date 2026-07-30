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
- 2026-07-30: Admin facility configuration stores classroom rows in `training_room` and NVR camera metadata in extended `room_camera` columns; updates replace the submitted camera list for a classroom.
- 2026-07-30: Admin score weights are append-only history rows in `edu_score_weight`; score grade rules are maintained as a full replacement set with non-overlapping ranges.
- 2026-07-30: Backend integrates `springdoc-openapi-ui` for online API docs at `/v3/api-docs` and `/swagger-ui.html`; current APIs use the temporary `X-User-Id` header until token authentication is fully wired.
- 2026-07-30: Codegraph is initialized locally for this repository; `.codegraph/` is ignored because it contains machine-local index data.
- 2026-07-30: Admin resource management is being implemented as metadata-first resource records with version snapshots; approved public applications sync into `res_public_resource` so existing student public-resource APIs can read them.
- 2026-07-30: User clarified Git workflow preference: do not push every small change; push `chen/backend` after completing a major backend module, then merge/sync to `main` after that module-level push.
- 2026-07-30: Admin exam management keeps question bank records mutable but stores immutable paper question snapshots in `exam_paper_question`; disabled questions remain available to historical papers and are only excluded from future assembly.

## Operational Notes

- Do not store secret values in this file.
- The repository contains `backend/`, `frontend/`, `database/`, `deploy/`, `docs/`, and `scripts/`.
