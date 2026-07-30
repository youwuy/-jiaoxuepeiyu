# Admin Education Config Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management-side backend APIs for organization, academic year, semester, major, and class configuration.

**Architecture:** Follow the existing Spring Boot 2.7/MyBatis feature package style with controller, service, port, repository, and model classes. Keep business rules in services and SQL access in annotation-based MyBatis mappers.

**Tech Stack:** Java 8, Spring Boot 2.7.18, MyBatis, MySQL 5.7-compatible SQL, JUnit 5 service tests.

## Global Constraints

- Backend branch is `chen/backend`.
- Backend must stay compatible with JDK 1.8.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Do not hardcode secrets or credentials.
- Keep changes focused to management-side backend foundation APIs.

---

### Task 1: Admin Organization API

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/org/model/AdminOrg.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/org/model/AdminOrgCommand.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/org/port/AdminOrgRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/org/AdminOrgService.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/org/repository/AdminOrgMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/org/repository/MyBatisAdminOrgRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/org/controller/AdminOrgController.java`
- Test: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/org/AdminOrgServiceTests.java`

**Interfaces:**
- Produces: `AdminOrgService#getTree()`, `create(AdminOrgCommand)`, `update(Long, AdminOrgCommand)`, `enable(Long)`, `disable(Long)`.
- Produces: `/api/admin/org/tree`, `/api/admin/org`, `/api/admin/org/{orgId}`, `/api/admin/org/{orgId}/enable`, `/api/admin/org/{orgId}/disable`.

- [x] **Step 1: Write service tests**

Create tests for sorted organization tree creation, blank organization name rejection, and status updates.

- [x] **Step 2: Run focused test command**

Run: `mvn -pl backend "-Dtest=AdminOrgServiceTests" test`
Expected in a Java/Maven environment: failing compilation before implementation, passing after implementation.

- [x] **Step 3: Implement the service and repository port**

Add model classes, service validation, tree building, and persistence interface.

- [x] **Step 4: Implement MyBatis mapper and REST controller**

Map `sys_org.status` to boolean `enabled`, and return data through `ApiResponse`.

### Task 2: Admin Education Configuration API

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminAcademicYear.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminAcademicYearCommand.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminSemester.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminMajor.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminMajorCommand.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminClass.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/model/AdminClassCommand.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/port/AdminEducationConfigRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/AdminEducationConfigService.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/repository/AdminEducationConfigMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/repository/MyBatisAdminEducationConfigRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/config/controller/AdminEducationConfigController.java`
- Test: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/config/AdminEducationConfigServiceTests.java`

**Interfaces:**
- Produces: `AdminEducationConfigService#listAcademicYears()`, `createAcademicYear(AdminAcademicYearCommand)`, `setCurrentSemester(Long)`, `listMajors()`, `createMajor(AdminMajorCommand)`, `enableMajor(Long)`, `disableMajor(Long)`, `listClasses(Long)`, `createClass(AdminClassCommand)`.
- Produces: `/api/admin/academic-years`, `/api/admin/semesters/{semesterId}/current`, `/api/admin/majors`, `/api/admin/classes`.

- [x] **Step 1: Write service tests**

Create tests for automatic semester creation, single current semester update order, validation failures, and major status updates.

- [x] **Step 2: Run focused test command**

Run: `mvn -pl backend "-Dtest=AdminEducationConfigServiceTests" test`
Expected in a Java/Maven environment: failing compilation before implementation, passing after implementation.

- [x] **Step 3: Implement service rules**

Create two semesters named `FIRST` and `SECOND` after academic year creation. Clear all current semester flags before marking the requested semester current. Trim names at service boundaries.

- [x] **Step 4: Implement MyBatis mapper and REST controller**

Use existing `edu_academic_year`, `edu_semester`, `edu_major`, and `edu_class` tables without new schema changes.

### Task 3: Verification and Git

**Files:**
- Modify: `MEMORY.md`
- Commit: all files touched by this checkpoint

**Interfaces:**
- Consumes: Task 1 and Task 2 APIs.
- Produces: pushed commit on `origin/chen/backend`.

- [x] **Step 1: Run static whitespace check**

Run: `git diff --check`
Expected: no output and exit code 0.

- [x] **Step 2: Scan for unsupported Java syntax and risky text**

Run repository scans for Java 9+ factory methods, records, sealed classes, `var`, `eval`, placeholders, and leaked credential patterns.

- [x] **Step 3: Attempt Java and Maven checks**

Run: `java -version`
Run: `mvn -version`
Expected in this workspace: commands may fail because Java and Maven are not installed.

- [ ] **Step 4: Commit and push**

Run: `git add ...`
Run: `git commit -m "feat: add admin organization and education config APIs"`
Run: `git push origin chen/backend`
