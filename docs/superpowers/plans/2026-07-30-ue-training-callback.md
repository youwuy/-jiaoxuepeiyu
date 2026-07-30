# UE Training Callback Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline task execution with test-first steps. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add backend APIs for the UE training program to read launch task data, report live status, and submit completed training archives.

**Architecture:** Add a focused `ue` backend module. The controller resolves the current student identity, the service validates task visibility and callback payloads, and the repository writes existing `training_monitor_snapshot`, `training_attempt`, and `training_attempt_step` tables so current admin/student query APIs immediately see callback results.

**Tech Stack:** Java 8, Spring Boot 2.7, MyBatis annotation mappers, MySQL 5.7.42.0.

## Global Constraints

- Backend must remain Java 8 compatible.
- Do not create a new archive schema when existing archive and monitor tables cover the workflow.
- Do not hardcode callback secrets, tokens, passwords, or machine-local paths.
- UE callbacks use the current student identity from `Authorization: Bearer <token>` or compatibility `X-User-Id`.
- Keep the API OpenAPI-visible for Vue, UE, and testing teams.

---

### Task 1: Service Contract And Validation

**Files:**
- Create: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/ue/UeTrainingCallbackServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/ue/UeTrainingCallbackService.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/ue/model/*`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/ue/port/UeTrainingCallbackRepository.java`

**Interfaces:**
- Produces: `TrainingLaunchTask getTask(Long studentId, Long trainingId)`
- Produces: `void reportStatus(Long studentId, Long trainingId, TrainingStatusCommand command)`
- Produces: `Long submitAttempt(Long studentId, Long trainingId, TrainingAttemptCommand command)`

- [x] Write tests for task lookup rejection, status validation/upsert, and final attempt archive creation.
- [x] Attempt focused tests; local Maven is unavailable because `mvn` is not installed in PATH.
- [x] Implement minimal service and models.

### Task 2: MyBatis Persistence And HTTP API

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/ue/repository/UeTrainingCallbackMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/ue/repository/MyBatisUeTrainingCallbackRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/ue/controller/UeTrainingCallbackController.java`

**Interfaces:**
- Produces: `GET /api/ue/trainings/{trainingId}/task`
- Produces: `POST /api/ue/trainings/{trainingId}/status`
- Produces: `POST /api/ue/trainings/{trainingId}/attempts`

- [x] Read task metadata from published trainings assigned to the current student.
- [x] Upsert monitor snapshot on status and submit callbacks.
- [x] Insert immutable attempt and ordered step records on submit.
- [x] Add Swagger/OpenAPI annotations.

### Task 3: Documentation And Verification

**Files:**
- Create: `docs/ue-api-contract.md`
- Modify: `docs/admin-api-contract.md`
- Modify: `docs/student-api-contract.md`
- Modify: `MEMORY.md`

**Verification:**
- `git diff --check`
- Java 8 syntax scan for unsupported syntax
- sensitive keyword scan for accidental secrets
- `codegraph.cmd sync`
- `mvn test` when Maven is available

- [x] Document callback request/response contracts.
- [x] Record architecture note in `MEMORY.md`.
- [x] Run available verification commands.
- [ ] Commit the completed module.
