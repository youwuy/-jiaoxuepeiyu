# Admin Training Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add filtered management training course exports for the training lifecycle requirement.

**Architecture:** Reuse the existing `AdminTrainingService` list query path and cap exports at the same service maximum page size used by other admin exports. Preserve a JSON export endpoint for frontend-controlled exports and add an Excel-compatible CSV file endpoint through the shared `CsvExporter`.

**Tech Stack:** Java 8, Spring Boot 2.7.x, MyBatis, MySQL 5.7.42.0, JUnit 5.

## Global Constraints

- Java source and target compatibility must stay at `1.8`.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Do not introduce Apache POI for this module; use the existing dependency-free CSV helper.
- Keep endpoints under `/api/admin/trainings`.

---

### Task 1: Service Export Rows

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/AdminTrainingService.java`
- Test: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/training/AdminTrainingServiceTests.java`

**Interfaces:**
- Consumes: `AdminTrainingRepository.findTrainings(AdminTrainingQuery query)`.
- Produces: `List<AdminTraining> exportTrainings(AdminTrainingQuery query)`.

- [x] **Step 1: Write the failing test**

Add `exportsTrainingsWithFilterAndMaximumPageSize` to verify export uses `findTrainings`, keeps filters, sets `page = 1`, sets `pageSize = 100`, and does not call `countTrainings`.

- [x] **Step 2: Run test to verify it fails**

Run: `mvn test "-Dtest=AdminTrainingServiceTests#exportsTrainingsWithFilterAndMaximumPageSize"`
Expected: FAIL because `exportTrainings` is not implemented.
Actual in this workspace: command cannot run because `mvn` is not installed.

- [x] **Step 3: Write minimal implementation**

Add `AdminTrainingService.exportTrainings` that normalizes the query, overrides paging to `1 / 100`, and returns `repository.findTrainings`.

- [ ] **Step 4: Run test to verify it passes**

Run on a Java/Maven machine: `mvn test "-Dtest=AdminTrainingServiceTests#exportsTrainingsWithFilterAndMaximumPageSize"`
Expected: PASS.

### Task 2: Controller And Contract

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/training/controller/AdminTrainingController.java`
- Modify: `docs/admin-api-contract.md`

**Interfaces:**
- Produces: `GET /api/admin/trainings/export`
- Produces: `GET /api/admin/trainings/export/file`

- [x] **Step 1: Add JSON export endpoint**

`GET /api/admin/trainings/export` returns `ApiResponse<List<AdminTraining>>` from `service.exportTrainings(query)`.

- [x] **Step 2: Add CSV export endpoint**

`GET /api/admin/trainings/export/file` returns a `text/csv;charset=UTF-8` attachment named `trainings.csv`.

- [x] **Step 3: Update API contract**

Document both endpoints and their response behavior in `docs/admin-api-contract.md`.

- [ ] **Step 4: Run verification**

Run: `mvn test "-Dtest=AdminTrainingServiceTests,CsvExporterTests"`
Expected: PASS.
Actual in this workspace: blocked until Maven is installed.
