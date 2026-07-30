# Admin Semester Score Import Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline task execution with test-first steps. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management-side offline semester score import preview and submission APIs.

**Architecture:** Extend the existing admin semester score module. Import endpoints accept already parsed JSON rows instead of binary Excel, validate student/semester/score/weight data, calculate comprehensive score in the service, and upsert `score_semester_summary` by the existing `student_id + semester_id` unique key.

**Tech Stack:** Java 8, Spring Boot 2.7, MyBatis annotation mappers, MySQL 5.7.42.0.

## Global Constraints

- Backend must remain Java 8 compatible.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Do not parse binary Excel in this module; frontend/import adapters submit already parsed rows.
- Comprehensive score is calculated by backend `ScoreCalculator`; clients do not submit trusted final totals.
- Keep APIs visible in OpenAPI for Vue and testing teams.

---

### Task 1: Service Models And Tests

**Files:**
- Modify: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/score/AdminSemesterScoreServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/model/AdminSemesterScoreImportCommand.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/model/AdminSemesterScoreImportRow.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/model/AdminSemesterScoreImportPreview.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/model/AdminSemesterScoreImportResult.java`

**Interfaces:**
- Produces: `AdminSemesterScoreImportPreview previewImport(AdminSemesterScoreImportCommand command)`
- Produces: `AdminSemesterScoreImportResult importScores(AdminSemesterScoreImportCommand command)`

- [x] Write tests for preview validation, duplicate student/semester rows, missing students, and successful upsert with calculated comprehensive score.
- [x] Attempt focused tests; record local Maven limitation if unavailable.
- [x] Implement minimal models and service methods.

### Task 2: Repository And Controller

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/port/AdminSemesterScoreRepository.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/repository/AdminSemesterScoreMapper.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/repository/MyBatisAdminSemesterScoreRepository.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/controller/AdminSemesterScoreController.java`

**Interfaces:**
- Produces: `POST /api/admin/scores/semester/import/preview`
- Produces: `POST /api/admin/scores/semester/import`

- [x] Add repository lookup for enabled student numbers.
- [x] Add repository upsert using `ON DUPLICATE KEY UPDATE`.
- [x] Add controller endpoints with Swagger annotations.

### Task 3: Documentation And Verification

**Files:**
- Modify: `docs/admin-api-contract.md`
- Modify: `MEMORY.md`

**Verification:**
- `git diff --check`
- Java 8 syntax scan for unsupported syntax
- sensitive keyword scan for accidental secrets
- `codegraph.cmd sync`
- `mvn -Dtest=AdminSemesterScoreServiceTests test` when Maven is available

- [x] Document import preview/import contracts.
- [x] Record architecture note in `MEMORY.md`.
- [x] Run available verification commands.
