# Admin Permission Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add admin APIs to maintain menu, page, and button permission nodes used by role authorization.

**Architecture:** Extend the existing IAM module and reuse `sys_permission` as the single source of truth. Permission tree reads remain unchanged for role binding; new write APIs validate hierarchy, type, code uniqueness, and delete safety before mutating rows.

**Tech Stack:** Java 8, Spring Boot 2.7.18, MyBatis annotations, MySQL 5.7.42.0, JUnit 5.

## Global Constraints

- Java source and target compatibility must stay at `1.8`.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Backend APIs serve Vue management and student clients.
- Deployment package must include a Java runtime/library layout so target users do not need to install Java manually.
- No plaintext passwords or secrets in API responses, logs, docs, or memory.

---

### Task 1: Service Contract And Tests

**Files:**
- Modify: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/iam/AdminIamServiceTests.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/iam/port/AdminIamRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/iam/model/AdminPermissionCommand.java`

**Interfaces:**
- Consumes: existing `AdminIamService`, `AdminPermission`, and `AdminIamRepository`.
- Produces:
  - `Long createPermission(AdminPermissionCommand command, Long operatorId)`
  - `void updatePermission(Long permissionId, AdminPermissionCommand command, Long operatorId)`
  - `void enablePermission(Long permissionId, Long operatorId)`
  - `void disablePermission(Long permissionId, Long operatorId)`
  - `void deletePermission(Long permissionId, Long operatorId)`

- [x] Write service tests for successful create normalization, invalid type rejection, parent self-reference rejection, duplicate code rejection, and delete safety.
- [x] Verify the new tests fail because the service/repository methods do not exist yet.

### Task 2: IAM Service Implementation

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/iam/AdminIamService.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/iam/port/AdminIamRepository.java`
- Modify: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/iam/AdminIamServiceTests.java`

**Interfaces:**
- Consumes: repository methods for permission lookup, code lookup, child count, role binding count, and row mutation.
- Produces: validated permission node write operations.

- [x] Add permission type allow-list: `MENU`, `PAGE`, `BUTTON`.
- [x] Require operator id, permission name, permission code, and permission type for writes.
- [x] Normalize whitespace and uppercase permission type.
- [x] Reject duplicate permission codes excluding the current row during update.
- [x] Reject `parentId == permissionId` during update.
- [x] Delete only when the node has no children and no role bindings.

### Task 3: MyBatis Persistence

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/iam/repository/AdminIamMapper.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/iam/repository/MyBatisAdminIamRepository.java`

**Interfaces:**
- Consumes: existing `sys_permission` table.
- Produces:
  - `findPermission(Long permissionId)`
  - `findPermissionIdByCode(String permissionCode)`
  - `createPermission(AdminPermissionCommand command)`
  - `updatePermission(Long permissionId, AdminPermissionCommand command)`
  - `updatePermissionStatus(Long permissionId, boolean visible)`
  - `deletePermission(Long permissionId)`
  - `countPermissionChildren(Long permissionId)`
  - `countPermissionRoleBindings(Long permissionId)`

- [x] Add SQL compatible with MySQL 5.7 using existing columns only.
- [x] Keep delete as a physical delete from `sys_permission` because the table has no soft-delete columns.

### Task 4: Controller And API Docs

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/iam/controller/AdminIamController.java`
- Modify: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/iam/AdminIamControllerTests.java`
- Modify: `docs/admin-api-contract.md`

**Interfaces:**
- Produces:
  - `POST /api/admin/permissions`
  - `PUT /api/admin/permissions/{permissionId}`
  - `POST /api/admin/permissions/{permissionId}/enable`
  - `POST /api/admin/permissions/{permissionId}/disable`
  - `POST /api/admin/permissions/{permissionId}/delete`

- [x] Add Swagger `@Operation` descriptions to all new endpoints.
- [x] Use `AdminContext.requireAdminId(request)` for operator identity, matching role APIs.
- [x] Document request fields, response envelope, validation rules, and delete safety.

### Task 5: Verification And Git

**Files:**
- Modify: `MEMORY.md`

**Verification:**
- [ ] Run focused IAM tests if Maven is available: `mvn test "-Dtest=AdminIamServiceTests,AdminIamControllerTests"`.
  - Blocked locally: `mvn` is not installed or available in PATH.
- [x] Run `git diff --check`.
- [x] Run Java 8 syntax scan on touched Java files.
- [x] Run sensitive value scan on touched files.
- [x] Run `codegraph.cmd sync`.

**Git:**
- [ ] Commit as `feat: add admin permission management APIs`.
- [ ] Push `chen/backend` after the module is complete.
- [ ] Merge/sync to `main` after a successful branch push when GitHub connectivity allows it.
