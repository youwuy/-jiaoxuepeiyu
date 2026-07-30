# Online Presence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline task execution with test-first steps. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add heartbeat and online-status APIs so management pages can show realtime online users, IP addresses, and offline state.

**Architecture:** Reuse `sys_user.last_heartbeat_time` and `sys_user.last_login_ip` as the presence source. `OnlinePresenceService` normalizes heartbeat/offline behavior using configured timeouts, `AuthController.logout` marks the user offline after token invalidation, and admin APIs calculate online status from a moving cutoff.

**Tech Stack:** Java 8, Spring Boot 2.7, MyBatis annotation mappers, MySQL 5.7.42.0.

## Global Constraints

- Backend must remain Java 8 compatible.
- Do not create a separate online table unless `sys_user` cannot support the requirement.
- Do not log or expose tokens, passwords, or secrets.
- Heartbeat interval is `30` seconds and offline timeout is `120` seconds by default.
- Keep APIs visible in OpenAPI for Vue and testing teams.

---

### Task 1: Service Contract And Tests

**Files:**
- Create: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/online/OnlinePresenceServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/online/OnlinePresenceService.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/online/model/*`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/online/port/OnlinePresenceRepository.java`

**Interfaces:**
- Produces: `OnlineHeartbeatResult heartbeat(Long userId, String ipAddress)`
- Produces: `void markOffline(Long userId)`
- Produces: `OnlinePresenceDashboard listOnlineUsers(OnlineUserQuery query)`

- [x] Write tests for heartbeat metadata, cutoff-based online summary, and logout offline behavior.
- [x] Attempt focused tests; local Maven is unavailable because `mvn` is not installed in PATH.
- [x] Implement minimal service and models.

### Task 2: Persistence And Controllers

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/online/repository/OnlinePresenceMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/online/repository/MyBatisOnlinePresenceRepository.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/online/controller/OnlinePresenceController.java`
- Modify: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/auth/controller/AuthController.java`
- Create: `database/init/018_online_presence_indexes.sql`

**Interfaces:**
- Produces: `POST /api/online/heartbeat`
- Produces: `POST /api/online/offline`
- Produces: `GET /api/admin/online/users`

- [x] Implement mapper methods over `sys_user`.
- [x] Require authenticated identity for heartbeat and offline calls.
- [x] Mark authenticated user offline when `/api/auth/logout` succeeds.
- [x] Add index for online status queries.

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
- `mvn test` when Maven is available

- [x] Document online presence request/response contracts.
- [x] Record architecture note in `MEMORY.md`.
- [x] Run available verification commands; Maven is unavailable because `mvn` is not installed in PATH.
- [ ] Commit, push `chen/backend`, merge to `main`, and push `main`.
