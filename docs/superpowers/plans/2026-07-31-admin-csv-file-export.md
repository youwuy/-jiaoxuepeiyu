# Admin CSV File Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Excel-compatible CSV file downloads for existing admin export row APIs without breaking the current JSON export contracts.

**Architecture:** Keep existing `/export` endpoints returning JSON rows for frontend-controlled export. Add `/export/file` endpoints that stream UTF-8 BOM CSV bytes with `Content-Disposition` attachments. Use a small dependency-free utility so Java 8 compatibility and deployment simplicity are preserved.

**Tech Stack:** Java 8, Spring Boot MVC, MyBatis-backed existing services, JUnit 5.

## Global Constraints

- Java source and target compatibility must stay at `1.8`.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Backend APIs serve Vue management and student clients.
- Deployment package must include a Java runtime/library layout so target users do not need to install Java manually.
- No plaintext passwords or secrets in API responses, logs, docs, or memory.

---

### Task 1: CSV Export Utility

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/common/export/CsvExporter.java`
- Create: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/common/export/CsvExporterTests.java`

**Interfaces:**
- Produces: `byte[] toCsvBytes(List<String> headers, List<List<String>> rows)`

- [x] Write tests for UTF-8 BOM, comma/quote/newline escaping, null-to-empty, and CRLF row endings.
- [x] Implement the dependency-free CSV utility.

### Task 2: Account Export Files

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/account/controller/AdminAccountController.java`
- Test: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/common/export/CsvExporterTests.java`

**Interfaces:**
- Produces:
  - `GET /api/admin/accounts/teachers/export/file`
  - `GET /api/admin/accounts/students/export/file`

- [x] Stream account export rows as CSV attachment.
- [x] Keep masked phone and ID card fields in file exports.
- [x] Preserve existing JSON `/export` endpoints.

### Task 3: Score And Archive Export Files

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/AdminSemesterScoreService.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/score/controller/AdminSemesterScoreController.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/archive/controller/AdminTrainingArchiveController.java`

**Interfaces:**
- Produces:
  - `GET /api/admin/scores/semester/export/file`
  - `GET /api/admin/archives/export/file`
  - `List<AdminSemesterScore> exportScores(AdminSemesterScoreQuery query)`

- [x] Export semester scores as filtered CSV rows instead of ranking-only data.
- [x] Export training archive rows as CSV attachment.
- [x] Preserve existing JSON `/export` endpoints.

### Task 4: Docs, Memory, Verification, Git

**Files:**
- Modify: `docs/admin-api-contract.md`
- Modify: `MEMORY.md`

**Verification:**
- [ ] Attempt focused Maven tests and record if Maven is unavailable.
  - Blocked locally: `mvn` is not installed or available in PATH.
- [x] Run `git diff --check`.
- [x] Run Java 8 syntax scan on touched Java files.
- [x] Run sensitive value scan on touched files.
- [x] Run `codegraph.cmd sync`.

**Git:**
- [ ] Commit as `feat: add admin csv file export APIs`.
- [ ] Push `chen/backend`.
- [ ] Merge/sync to `main` and push after successful branch push.
