# Auth Change Password Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline task execution with test-first steps. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a token-authenticated password change API that works for admin, teacher, and student users.

**Architecture:** Extend the existing `auth` module instead of duplicating admin/student profile password logic. The service resolves the current user from the Bearer token, loads the password hash by user id, validates the current password and password policy, then writes only the new password hash.

**Tech Stack:** Java 8, Spring Boot 2.7, MyBatis annotation mappers, MySQL 5.7.42.0.

## Global Constraints

- Backend must remain Java 8 compatible.
- Do not return plaintext passwords or password hashes.
- Do not hardcode passwords, secrets, tokens, or private keys.
- Keep the existing student profile password endpoint as compatibility surface.
- Keep the new API visible in OpenAPI for Vue and testing teams.

---

### Task 1: Auth Service Contract And Tests

**Files:**
- Modify: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/auth/AuthServiceTests.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/auth/AuthService.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/auth/port/UserAccountRepository.java`

**Interfaces:**
- Produces: `void changePassword(String token, String currentPassword, String newPassword, String confirmPassword)`

- [x] Write tests for successful hash update, wrong current password, missing/weak password policy errors, and missing current user.
- [x] Attempt focused tests; record local Maven limitation if unavailable.
- [x] Implement the minimal service method.

### Task 2: Persistence And Controller

**Files:**
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/auth/repository/UserAccountMapper.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/auth/repository/MyBatisUserAccountRepository.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/auth/controller/AuthController.java`

**Interfaces:**
- Produces: `PUT /api/auth/password`

- [x] Add repository lookup by user id.
- [x] Add repository password hash update.
- [x] Add controller request body and Swagger annotation.

### Task 3: Documentation And Verification

**Files:**
- Modify: `docs/admin-api-contract.md`
- Modify: `docs/student-api-contract.md`
- Modify: `MEMORY.md`

**Verification:**
- `git diff --check`
- Java 8 syntax scan for unsupported syntax
- sensitive keyword scan for accidental secrets
- `codegraph.cmd sync`
- `mvn -Dtest=AuthServiceTests test` when Maven is available

- [x] Document password change contract in both admin and student API docs.
- [x] Record the auth architecture note in `MEMORY.md`.
- [x] Run available verification commands.
