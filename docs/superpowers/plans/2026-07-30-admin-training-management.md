# Admin Training Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline execution with TDD. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management-side training course APIs that create, edit, publish, cancel publish, soft delete, inspect logs, expose statistics, and provide a monitoring snapshot while keeping student training visibility compatible.

**Architecture:** Follow the existing admin course module shape: Controller -> Service -> Repository port -> MyBatis mapper. Store management metadata on `training_course`, class bindings in `training_class`, team roles in existing `training_role`, operation logs in `training_course_log`, and monitoring rows in `training_monitor_snapshot`. Publishing repopulates `training_participant` from bound class students so existing student APIs remain unchanged.

**Tech Stack:** Java 8, Spring Boot 2.7.18, MyBatis annotation mappers, MySQL 5.7.42.0, springdoc-openapi.

## Global Constraints

- Keep Java source compatible with JDK 1.8.
- Use `/api/admin/trainings/**` and the common `ApiResponse` / `PageResponse` shape.
- Keep temporary identity header `X-User-Id` through `AdminContext.requireAdminId`.
- Use MySQL 5.7-compatible DDL and avoid hardcoded secrets.
- Push only after this major module is complete, then merge/sync to `main`.

---

### Task 1: Service Contract And Tests

**Files:**
- Create: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/training/AdminTrainingServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/model/*`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/port/AdminTrainingRepository.java`

**Interfaces:**
- Produces `AdminTrainingService#createTraining`, `#updateTraining`, `#publishTraining`, `#cancelPublishTraining`, `#deleteTraining`, `#getStatistics`, `#getMonitorSnapshot`, and `#listTrainingLogs`.

- [ ] Write tests for draft creation normalization, validation errors, publish side effects, and statistics defaults.
- [ ] Run the focused test command and confirm failures before production code exists.
- [ ] Add model and repository interfaces only as needed to compile the tests.

### Task 2: Service Implementation

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/AdminTrainingService.java`

**Interfaces:**
- Consumes `AdminTrainingRepository`.
- Produces validated service behavior for the controller and mapper-backed repository.

- [ ] Implement validation for name, term, major, class ids, open time, `PRACTICE/EXAM`, `SINGLE/TEAM`, `MANUAL/AUTO`, paper requirement, team size, and role count.
- [ ] Implement lifecycle methods and append operation logs.
- [ ] Run the focused service tests until green.

### Task 3: Persistence And SQL

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/repository/AdminTrainingMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/repository/MyBatisAdminTrainingRepository.java`
- Create: `database/init/014_admin_training_management.sql`

**Interfaces:**
- Consumes normalized `AdminTrainingCommand`.
- Produces MyBatis-backed storage compatible with existing student training queries.

- [ ] Add DDL for training metadata, `training_class`, `training_course_log`, and `training_monitor_snapshot`.
- [ ] Implement list/detail CRUD, role replacement, class replacement, participant sync, notifications, logs, statistics, and monitor snapshot queries.
- [ ] Keep `training_participant` populated from enabled students in bound classes at publish time.

### Task 4: REST API And Docs

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/controller/AdminTrainingController.java`
- Modify: `docs/admin-api-contract.md`
- Modify: `MEMORY.md`

**Interfaces:**
- Produces Swagger-documented endpoints under `/api/admin/trainings`.

- [ ] Add list/detail/create/update/publish/cancel-publish/delete/statistics/monitor/log APIs.
- [ ] Document request bodies, query params, statuses, and behavior in the admin API contract.
- [ ] Record long-term module decisions in `MEMORY.md`.

### Task 5: Verification And Module Commit

**Files:**
- Verify all touched files.

- [ ] Run focused Maven tests if Maven is available.
- [ ] Run `git diff --check`.
- [ ] Run Java 8 compatibility scans for common non-Java-8 APIs and syntax.
- [ ] Sync codegraph if the local command is available.
- [ ] Commit as `feat: add admin training management APIs`.
- [ ] Push `chen/backend`, fast-forward/merge to `main`, push `main`, and return to `chen/backend`.
