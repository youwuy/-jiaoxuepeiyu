# Admin Job Role Config Implementation Plan

> Historical only: superseded by the 2026-08-15 customer clarification that training roles are fixed by each training topic. The management UI and job-role configuration APIs must not be restored from this plan.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management APIs for the subway job role dictionary required by global business configuration.

**Architecture:** Extend the existing `AdminEducationConfig` module because subway job roles are global foundation data alongside academic years, majors, and classes. Store roles in `edu_job_role`, expose CRUD/status APIs under `/api/admin/job-roles`, and keep the model deliberately small: name, sort order, enabled status.

**Tech Stack:** Java 8, Spring Boot 2.7.x, MyBatis annotations, MySQL 5.7.42.0, JUnit 5.

## Global Constraints

- Java source and target compatibility must stay at `1.8`.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Do not hardcode seed job role data; deployments may create roles through APIs.
- Keep changes inside the existing admin configuration module.

---

### Task 1: Service Rules

**Files:**
- Modify: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/config/AdminEducationConfigServiceTests.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/AdminEducationConfigService.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/port/AdminEducationConfigRepository.java`

**Interfaces:**
- Produces: `List<AdminJobRole> listJobRoles()`
- Produces: `Long createJobRole(AdminJobRoleCommand command)`
- Produces: `void updateJobRole(Long jobRoleId, AdminJobRoleCommand command)`
- Produces: `void enableJobRole(Long jobRoleId)`
- Produces: `void disableJobRole(Long jobRoleId)`

- [x] **Step 1: Write the failing tests**

Add tests for trimmed role names, default sort order, blank-name rejection, update behavior, and enable/disable behavior.

- [x] **Step 2: Run tests to verify failure**

Run: `mvn test "-Dtest=AdminEducationConfigServiceTests"`
Expected: FAIL until model/service methods exist.
Actual in this workspace: command cannot run because `mvn` is not installed.

- [x] **Step 3: Implement service and port methods**

Add job role methods to `AdminEducationConfigService` and `AdminEducationConfigRepository`.

- [ ] **Step 4: Run service tests**

Run on a Java/Maven machine: `mvn test "-Dtest=AdminEducationConfigServiceTests"`
Expected: PASS.

### Task 2: Persistence And API

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminJobRole.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminJobRoleCommand.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/repository/AdminEducationConfigMapper.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/repository/MyBatisAdminEducationConfigRepository.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/controller/AdminEducationConfigController.java`
- Create: `database/init/019_admin_job_roles.sql`
- Modify: `docs/admin-api-contract.md`

**Interfaces:**
- Produces: `GET /api/admin/job-roles`
- Produces: `POST /api/admin/job-roles`
- Produces: `PUT /api/admin/job-roles/{jobRoleId}`
- Produces: `POST /api/admin/job-roles/{jobRoleId}/enable`
- Produces: `POST /api/admin/job-roles/{jobRoleId}/disable`

- [x] **Step 1: Add MySQL table**

Create `edu_job_role` with unique `role_name`, `sort_order`, `status`, and timestamps.

- [x] **Step 2: Add mapper and repository implementation**

Map list/create/update/status operations to MyBatis annotations.

- [x] **Step 3: Add controller APIs**

Expose list/create/update/enable/disable endpoints under `/api/admin/job-roles`.

- [x] **Step 4: Update API contract**

Document request fields, response fields, and behavior in `docs/admin-api-contract.md`.

- [ ] **Step 5: Run verification**

Run on a Java/Maven machine: `mvn test "-Dtest=AdminEducationConfigServiceTests"`
Expected: PASS.
