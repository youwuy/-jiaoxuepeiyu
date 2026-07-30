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
- 2026-07-30: Published OpenAPI docs are available at `https://youwuy.github.io/-jiaoxuepeiyu/` with schema JSON at `https://youwuy.github.io/-jiaoxuepeiyu/openapi.json`; frontend API integration should use this contract and only wire APIs into UI screens that already exist.
- 2026-07-30: Codegraph is initialized locally for this repository; `.codegraph/` is ignored because it contains machine-local index data.
- 2026-07-30: Admin resource management is being implemented as metadata-first resource records with version snapshots; approved public applications sync into `res_public_resource` so existing student public-resource APIs can read them.
- 2026-07-30: User clarified Git workflow preference: do not push every small change; push `chen/backend` after completing a major backend module, then merge/sync to `main` after that module-level push.
- 2026-07-30: Admin exam management keeps question bank records mutable but stores immutable paper question snapshots in `exam_paper_question`; disabled questions remain available to historical papers and are only excluded from future assembly.
- 2026-07-30: Admin course management uses `course_teacher` and `course_class` for multi-teacher/multi-class bindings while maintaining `course.class_id` as the first class for legacy student-course compatibility; student course and assignment queries read through `course_class` with a `course.class_id` fallback.
- 2026-07-30: Frontend work branch is `zhan/frontend-test`; after major frontend modules are completed, push that branch and merge/sync to `main`.
- 2026-07-30: Calicat student login source is design file `2077697084758810624`, login canvas `5743e413-27b0-4a99-9047-cd1a00e44a64`; actual frame IDs are `c96fafb2-b43e-4a55-8922-7f8b583f59f0` for `登录页-学号登录` and `50a840b5-34cc-4449-87a1-43eb76123929` for `登录页-手机号登录`.
- 2026-07-30: Student login UI must match the Calicat 1200 x 800 frames: student-ID state keeps left brand content hidden with only the blue gradient/circles, while phone-login state shows the left brand panel and feature list.

## Operational Notes

- Do not store secret values in this file.
- The repository contains `backend/`, `frontend/`, `database/`, `deploy/`, `docs/`, and `scripts/`.
