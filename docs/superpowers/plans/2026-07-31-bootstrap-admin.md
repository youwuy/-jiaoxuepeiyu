# Bootstrap Admin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow first deployment to create an initial management administrator without committing plaintext credentials.

**Architecture:** Add a small startup bootstrap module that reads optional environment-backed properties, creates one `admin` user only when no admin exists, and stores only a hashed password. Existing deployments with an admin account are left untouched.

**Tech Stack:** Java 8, Spring Boot 2.7.x `ApplicationRunner`, MyBatis annotations, MySQL 5.7.42.0, JUnit 5.

## Global Constraints

- Java source and target compatibility must stay at `1.8`.
- Do not hardcode or log plaintext passwords.
- Do not reset or modify existing admin users.
- Bootstrap creation requires both username and password to be explicitly configured.

---

### Task 1: Bootstrap Rules

**Files:**
- Create: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/bootstrap/BootstrapAdminServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/bootstrap/BootstrapAdminService.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/bootstrap/BootstrapAdminCommand.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/bootstrap/BootstrapAdminResult.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/bootstrap/BootstrapAdminRepository.java`

**Interfaces:**
- Produces: `BootstrapAdminResult initialize(BootstrapAdminCommand command)`.

- [x] **Step 1: Write failing tests**

Cover not-configured skip, partial-config error, existing-admin skip, weak-password error, and hashed admin creation.

- [x] **Step 2: Run tests to verify failure**

Run: `mvn test "-Dtest=BootstrapAdminServiceTests"`
Expected: FAIL until bootstrap classes exist.
Actual in this workspace: command cannot run because `mvn` is not installed.

- [x] **Step 3: Implement service rules**

Add command/result/repository abstractions and service validation.

- [x] **Step 4: Run service tests**

Run on a Java/Maven machine: `mvn test "-Dtest=BootstrapAdminServiceTests"`
Expected: PASS.
Actual in this workspace: command cannot run because `mvn` is not installed.

### Task 2: Startup Wiring And Docs

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/bootstrap/BootstrapAdminInitializer.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/bootstrap/repository/BootstrapAdminMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/bootstrap/repository/MyBatisBootstrapAdminRepository.java`
- Modify: `backend/src/main/resources/application.yml`
- Modify: `deploy/config/application.yml.example`
- Modify: `deploy/README.md`
- Modify: `docs/admin-api-contract.md`

**Interfaces:**
- Consumes: `APP_BOOTSTRAP_ADMIN_USERNAME`
- Consumes: `APP_BOOTSTRAP_ADMIN_PASSWORD`
- Consumes: `APP_BOOTSTRAP_ADMIN_REAL_NAME`
- Consumes: `APP_BOOTSTRAP_ADMIN_PHONE`

- [x] **Step 1: Add MyBatis repository**

Count existing admin users, check username collisions, and insert the initial admin row.

- [x] **Step 2: Add startup runner**

Read environment-backed properties and call the bootstrap service at application startup.

- [x] **Step 3: Update config examples and deployment docs**

Document that bootstrap credentials are environment variables and should be removed after first successful startup.

- [x] **Step 4: Run verification**

Run on a Java/Maven machine: `mvn test "-Dtest=BootstrapAdminServiceTests"` and `mvn package`.
Expected: PASS.
Actual in this workspace: `git diff --check` passed; Maven commands cannot run because `mvn` is not installed.
