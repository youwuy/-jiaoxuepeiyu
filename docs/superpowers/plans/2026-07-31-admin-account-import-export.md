# Admin Account Import Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline task execution with test-first steps. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add teacher/student batch import preview, batch import submission, and export-ready account APIs for management user administration.

**Architecture:** Extend the existing admin account module instead of creating a parallel account path. Import submission reuses the same `createTeacher` and `createStudent` validation and initial-password hashing path as single-account creation; export returns masked, export-ready rows from the existing account list query.

**Tech Stack:** Java 8, Spring Boot 2.7, MyBatis annotation mappers, MySQL 5.7.42.0.

## Global Constraints

- Backend must remain Java 8 compatible.
- Do not return plaintext initial passwords from import APIs.
- Do not parse binary Excel in this module; frontend/import adapters submit already parsed rows, matching current question and paper import preview style.
- Keep teacher and student import behavior consistent with single create APIs.
- Keep APIs visible in OpenAPI for Vue and testing teams.

---

### Task 1: Service Contract And Tests

**Files:**
- Modify: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/account/AdminAccountServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/model/AdminAccountImportCommand.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/model/AdminAccountImportRow.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/model/AdminAccountImportPreview.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/model/AdminAccountImportResult.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/model/AdminAccountExportRow.java`

**Interfaces:**
- Produces: `AdminAccountImportPreview previewImport(String userType, AdminAccountImportCommand command)`
- Produces: `AdminAccountImportResult importAccounts(String userType, AdminAccountImportCommand command)`
- Produces: `List<AdminAccountExportRow> exportAccounts(String userType, AdminAccountQuery query)`

- [x] Write tests for invalid preview rows, duplicate rows, successful import through existing create path, and export masking.
- [x] Attempt focused tests; local Maven is unavailable because `mvn` is not installed in PATH.
- [x] Implement minimal service and models.

### Task 2: Repository And Controller

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/AdminAccountService.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/port/AdminAccountRepository.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/repository/AdminAccountMapper.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/repository/MyBatisAdminAccountRepository.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/controller/AdminAccountController.java`

**Interfaces:**
- Produces: `POST /api/admin/accounts/teachers/import/preview`
- Produces: `POST /api/admin/accounts/teachers/import`
- Produces: `GET /api/admin/accounts/teachers/export`
- Produces: `POST /api/admin/accounts/students/import/preview`
- Produces: `POST /api/admin/accounts/students/import`
- Produces: `GET /api/admin/accounts/students/export`

- [x] Add repository method to detect existing usernames.
- [x] Add controller endpoints with Swagger annotations.
- [x] Keep import submission transactional.

### Task 3: Documentation And Verification

**Files:**
- Modify: `docs/admin-api-contract.md`
- Modify: `MEMORY.md`

**Verification:**
- `git diff --check`
- Java 8 syntax scan for unsupported syntax
- sensitive keyword scan for accidental secrets
- `codegraph.cmd sync`
- `mvn test` when Maven is available

- [x] Document import/export contracts.
- [x] Record architecture note in `MEMORY.md`.
- [x] Run available verification commands.
- [ ] Commit, push `chen/backend`, merge to `main`, and push `main`.
