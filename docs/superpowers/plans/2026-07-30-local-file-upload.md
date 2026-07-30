# Local File Upload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline task execution with test-first steps. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a backend file upload API that stores files locally and returns metadata usable by resource create/update APIs.

**Architecture:** Keep upload storage independent from admin resource management. `POST /api/files` accepts multipart input, `FileStorageService` validates and stores the file under a configurable root, and Spring MVC serves stored files under `/uploads/**`.

**Tech Stack:** Java 8, Spring Boot 2.7, Spring MVC multipart support, JUnit 5, MySQL 5.7.42.0 compatible backend.

## Global Constraints

- Backend must remain Java 8 compatible.
- Do not hardcode secrets, credentials, or machine-local absolute upload paths.
- Use existing `ApiResponse` response envelope and `BusinessException` error handling.
- Keep resource management metadata-first: upload files first, then pass returned metadata to resource APIs.
- Deployment must allow operators to configure the upload directory.

---

### Task 1: Storage Service Contract

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/file/FileStorageService.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/file/model/StoredFile.java`
- Test: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/file/FileStorageServiceTests.java`

**Interfaces:**
- Produces: `StoredFile store(MultipartFile file, String category)`
- Produces: sanitized categories containing only lower-case letters, digits, `_`, and `-`
- Produces: public URLs in the form `/uploads/{category}/{storedFileName}`

- [x] Write tests for successful storage, unsafe filename normalization, unsafe category rejection, and empty file rejection.
- [x] Attempt focused tests before implementation; local verification is blocked because `mvn` is not installed in PATH.
- [x] Implement minimal storage service and response model.
- [ ] Run focused tests again on a machine with Maven and confirm they pass; this workstation has no `mvn` command.

### Task 2: HTTP Upload API And Static Access

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/file/controller/FileUploadController.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/config/WebConfig.java`
- Modify: `backend/src/main/resources/application.yml`
- Modify: `deploy/config/application.yml.example`

**Interfaces:**
- Consumes: `FileStorageService.store(MultipartFile file, String category)`
- Produces: `POST /api/files`
- Produces: static resource mapping `/uploads/**`

- [x] Add controller method returning `ApiResponse<StoredFile>`.
- [x] Add Swagger/OpenAPI annotations for frontend and tester visibility.
- [x] Add `app.file.upload-root` and `app.file.public-prefix` configuration.
- [x] Add MVC resource handler for uploaded files.

### Task 3: Documentation And Verification

**Files:**
- Modify: `docs/admin-api-contract.md`
- Modify: `deploy/README.md`
- Modify: `MEMORY.md`

**Verification:**
- `git diff --check`
- Java 8 syntax scan for unsupported syntax
- sensitive keyword scan for accidental secrets
- `mvn test` when Maven is available
- `codegraph.cmd sync`

- [x] Document upload API contract and deployment storage configuration.
- [x] Record long-term architecture note in `MEMORY.md`.
- [x] Run available verification commands.
- [ ] Commit the completed module.
