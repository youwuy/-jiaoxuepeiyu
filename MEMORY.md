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
- 2026-07-30: Backend integrates `springdoc-openapi-ui` for online API docs at `/v3/api-docs` and `/swagger-ui.html`; APIs now prefer `Authorization: Bearer <token>` and keep `X-User-Id` as a temporary compatibility fallback.
- 2026-07-30: Published OpenAPI docs are available at `https://youwuy.github.io/-jiaoxuepeiyu/` with schema JSON at `https://youwuy.github.io/-jiaoxuepeiyu/openapi.json`; frontend API integration should use this contract and only wire APIs into UI screens that already exist.
- 2026-07-30: Codegraph is initialized locally for this repository; `.codegraph/` is ignored because it contains machine-local index data.
- 2026-07-30: Admin resource management is being implemented as metadata-first resource records with version snapshots; approved public applications sync into `res_public_resource` so existing student public-resource APIs can read them.
- 2026-07-30: User clarified Git workflow preference: do not push every small change; push `chen/backend` after completing a major backend module, then merge/sync to `main` after that module-level push.
- 2026-07-30: Admin exam management keeps question bank records mutable but stores immutable paper question snapshots in `exam_paper_question`; disabled questions remain available to historical papers and are only excluded from future assembly.
- 2026-07-30: Admin course management uses `course_teacher` and `course_class` for multi-teacher/multi-class bindings while maintaining `course.class_id` as the first class for legacy student-course compatibility; student course and assignment queries read through `course_class` with a `course.class_id` fallback.
- 2026-07-30: Admin assignment review uses `assignment_attempt` as the review state source; reviewed attempts store reviewer metadata and per-question scores/comments in `assignment_answer`, then refresh course progress after review.
- 2026-07-30: Frontend work branch is `zhan/frontend-test`; after major frontend modules are completed, push that branch and merge/sync to `main`.
- 2026-07-30: Calicat student login source is design file `2077697084758810624`, login canvas `5743e413-27b0-4a99-9047-cd1a00e44a64`; actual frame IDs are `c96fafb2-b43e-4a55-8922-7f8b583f59f0` for `登录页-学号登录` and `50a840b5-34cc-4449-87a1-43eb76123929` for `登录页-手机号登录`.
- 2026-07-30: Student login UI must match the Calicat 1200 x 800 frames: student-ID state keeps left brand content hidden with only the blue gradient/circles, while phone-login state shows the left brand panel and feature list.
- 2026-07-30: Admin training management stores management metadata on `training_course`, class bindings in `training_class`, and team roles in existing `training_role`; publishing rebuilds `training_participant` from enabled students in bound classes so existing student training APIs can see newly published trainings.
- 2026-07-30: Admin training monitor APIs read camera/student/progress/score state from `training_monitor_snapshot`; later UE/device callback work should update this snapshot table instead of changing the management query contract.
- 2026-07-30: Admin device efficiency APIs use `device` for inventory/realtime status, `device_usage_event` for active/latest usage sessions, and `device_usage_daily_summary` for dashboard totals, monthly trends, utilization, and heat ranking; future device/UE callbacks should write events and rollups instead of changing the API contract.
- 2026-07-30: Admin semester score APIs read `score_semester_summary` and calculate missing `comprehensive_score` values from stored component scores/weights at query time; admin training archive APIs read immutable `training_attempt` and `training_attempt_step` rows without mutating historical records.
- 2026-07-30: Admin IAM role management reads permissions from `sys_permission` as a tree; stores role data scope on `sys_role.data_scope`; copies the same scope into `sys_role_permission`; and preserves `sys_user_role` rows when soft deleting roles for audit compatibility.
- 2026-07-30: Auth now supports `Authorization: Bearer <token>` for current user resolution and logout; `AdminContext` and `StudentContext` prefer token-authenticated request users and keep `X-User-Id` as a temporary compatibility fallback.
- 2026-07-30: Deployment packaging must be run on a release machine with `JRE8_HOME` or `deploy/runtime/jre8`; `deploy/package.sh` fails without a JRE 8 source and copies it into `deploy/dist/runtime/jre8` so target users do not install Java.
- 2026-07-30: `/api/health` is the public deployment smoke-check endpoint and returns service status, Java runtime version, MySQL 5.7.42.0 target, and server time.
- 2026-07-30: File upload is handled by an independent local storage module at `POST /api/files`; resource/course/assignment modules should upload file content first and then store the returned `fileUrl`, `fileName`, and `fileSize` metadata.
- 2026-07-30: UE training integration uses student-authenticated callbacks under `/api/ue/trainings`; status callbacks upsert `training_monitor_snapshot`, and result callbacks insert immutable `training_attempt` plus `training_attempt_step` rows for existing admin/student archive APIs.
- 2026-07-30: Online presence uses `sys_user.last_heartbeat_time` and `last_login_ip`; clients call `POST /api/online/heartbeat` about every 30 seconds, `POST /api/auth/logout` clears heartbeat, and admin online dashboards treat users without heartbeat for 120 seconds as offline.
- 2026-07-31: Calicat training center source is design file `2077697084758810624`, training canvas `b8638fa5-46da-4528-ac68-9a919b9368d6`; the student training list frame is `1c8a8668-498d-4265-8e54-7bb769b22194`. The user-provided `2077697084775587840` canvas is actually `课程学习`, not `实训中心`.
- 2026-07-31: Student training list UI should match the Calicat 1440 x 1285 list: 64 px student topbar, 1376 px training content width, fixed training row columns, right-aligned filter toolbar, and status labels `进行中` / `未开始` / `已结束`.
- 2026-07-31: Calicat resource learning source is design file `2077697084758810624`, resource canvas `fa78d99b-dc13-4dd5-9cc8-c2c440d586d5`; the main resource list frame is `e8e54703-ef33-49b8-9290-05e9ec9704b5` and preview modal frame is `a8278ded-b034-405c-a4e9-4fdc4648de8c`.
- 2026-07-31: Student resource learning UI should match the Calicat 1440 px resource library: 64 px student topbar, 1376 px content width, white 192 px filter panel, two rows of resource-type/major chips, 4-column 326 px resource card grid with real cover images, and bottom pagination.
- 2026-07-31: Calicat personal center source is design file `2077697084758810624`, profile canvas `373411a9-e772-42d6-9e91-5b2c3db3a7c0`; core frames are `9f177a13-d224-4b65-9f6a-63f64a202b1a` personal info, `7142d7d9-f897-4115-a1e2-b73616899568` comprehensive score, `a6ea198e-580d-4afd-9284-14b0273b4f34` messages, `ad9fb9cd-48ca-4e44-8914-f7af9853abb5` training archive list, and `4e15c586-0860-43e7-9371-8a7aff331b75` archive detail.
- 2026-07-31: Student personal center UI should match the Calicat shell with a 220 px left sidebar, breadcrumb content area, personal info field grid, score table, message cards, training archive table, and in-page archive detail view.
- 2026-07-31: Current remote API documentation for frontend integration is `http://8.130.35.250/swagger-ui/index.html`, with OpenAPI JSON available at `http://8.130.35.250/v3/api-docs`.
- 2026-07-31: Student frontend local development proxies `/api` to `http://8.130.35.250`; the existing `requestJson('/student/...')` calls resolve to backend `/api/student/...` endpoints through the Vite proxy.
- 2026-07-31: Calicat admin login source is design file `2077681918571450368`, login canvas `ffb400ed-7fdd-4662-b39a-27c3e039f70e`; frame IDs are `5d8bdf11-2533-42c8-8010-925d43496c23` for `登录页-工号登录` and `16354727-a3c5-4bfe-b96f-24450dd0bf84` for `登录页-手机号登录`.
- 2026-07-31: Admin login UI should match the Calicat 1440 x 800 dark split screen: 600 px metro carriage image, near-black right stage, 440 px dark slate login card, work-number/phone tabs, 48 px inputs, and 48 px blue submit button.
- 2026-07-31: Calicat admin teaching-course source is design file `2077681918571450368`, canvas `5f1d775a-5e0d-4d3e-bf8d-032f5b13e6f4`; the management course page uses the 240 px left sidebar, breadcrumb `教学实训 / 教学课程`, filter row, 9-column course table, and drawer/dialog patterns for course detail, statistics, and logs.
- 2026-07-31: Frontend admin course integration uses `GET /api/admin/courses`, `GET /api/admin/courses/{courseId}`, `POST /api/admin/courses/{courseId}/publish`, `POST /api/admin/courses/{courseId}/cancel-publish`, `POST /api/admin/courses/{courseId}/delete`, `POST /api/admin/courses/{courseId}/copy`, `GET /api/admin/courses/{courseId}/statistics`, and `GET /api/admin/courses/{courseId}/logs`; the page falls back to local mock data when the backend is unavailable.

## Operational Notes

- Do not store secret values in this file.
- The repository contains `backend/`, `frontend/`, `database/`, `deploy/`, `docs/`, and `scripts/`.
