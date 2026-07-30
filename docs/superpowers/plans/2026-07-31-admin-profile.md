# Admin Profile Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline task execution with test-first steps. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management-side current-user profile view and basic identity field update APIs.

**Architecture:** Add a focused `admin/profile` module parallel to the existing student profile module. The controller resolves the current admin or teacher with `AdminContext`, the service masks sensitive identity fields in read responses, and repository methods update `sys_user` directly for the current user only.

**Tech Stack:** Java 8, Spring Boot 2.7, MyBatis annotation mappers, MySQL 5.7.42.0.

## Global Constraints

- Backend must remain Java 8 compatible.
- Do not expose raw phone or ID card values in profile read responses.
- Do not add duplicate password-changing logic; use `PUT /api/auth/password`.
- Keep APIs visible in OpenAPI for Vue and testing teams.

---

### Task 1: Service Contract And Tests

**Files:**
- Create: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/profile/AdminProfileServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/profile/model/AdminProfile.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/profile/port/AdminProfileRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/profile/AdminProfileService.java`

**Interfaces:**
- Produces: `AdminProfile getProfile(Long userId)`
- Produces: `void updatePhone(Long userId, String phone)`
- Produces: `void updateIdCard(Long userId, String idCard)`

- [x] Write tests for masked profile response, invalid phone rejection, valid phone update, invalid ID card rejection, and valid ID card update.
- [x] Attempt focused tests; record local Maven limitation if unavailable.
- [x] Implement minimal model, port, and service.

### Task 2: Persistence And Controller

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/profile/repository/AdminProfileMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/profile/repository/MyBatisAdminProfileRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/profile/controller/AdminProfileController.java`

**Interfaces:**
- Produces: `GET /api/admin/profile`
- Produces: `PUT /api/admin/profile/phone`
- Produces: `PUT /api/admin/profile/id-card`

- [x] Add MyBatis queries restricted to `admin` and `teacher` user types.
- [x] Add controller request bodies with validation annotations.
- [x] Add Swagger annotations.

### Task 3: Documentation And Verification

**Files:**
- Modify: `docs/admin-api-contract.md`
- Modify: `MEMORY.md`

**Verification:**
- `git diff --check`
- Java 8 syntax scan for unsupported syntax
- sensitive keyword scan for accidental secrets
- `codegraph.cmd sync`
- `mvn -Dtest=AdminProfileServiceTests test` when Maven is available

- [x] Document admin profile contracts.
- [x] Record the profile architecture note in `MEMORY.md`.
- [x] Run available verification commands.
