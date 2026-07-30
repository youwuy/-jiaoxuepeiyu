# Admin Device Efficiency Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline execution with TDD. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management-side device efficiency APIs for real-time usage, cumulative usage, monthly trends, utilization, and heat ranking.

**Architecture:** Add an `admin/device` module with Controller -> Service -> Repository port -> MyBatis mapper, following existing admin modules. Persist device inventory and usage rollups in `device`, `device_usage_event`, and `device_usage_daily_summary`; keep classroom relationships through existing `training_room`. The service normalizes query windows and returns one aggregate report for dashboard pages plus focused endpoints for each chart/table.

**Tech Stack:** Java 8, Spring Boot 2.7.18, MyBatis annotation mappers, MySQL 5.7.42.0, springdoc-openapi.

## Global Constraints

- Keep Java source compatible with JDK 1.8.
- Use `/api/admin/devices/efficiency/**` and the common `ApiResponse` shape.
- Use MySQL 5.7-compatible DDL and date functions.
- Do not hardcode secrets.
- Push only after a major module is complete; merge/sync to `main` only after the push succeeds.

---

### Task 1: Service Tests And Contract

**Files:**
- Create: `backend/src/test/java/com/qizhifu/jiaoxuepeiyu/admin/device/AdminDeviceEfficiencyServiceTests.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/device/model/*`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/device/port/AdminDeviceEfficiencyRepository.java`

- [ ] Write tests for default date normalization, invalid date range rejection, aggregate report assembly, and null summary defaults.
- [ ] Attempt focused test run and record missing Maven if unavailable.

### Task 2: Service Implementation

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/device/AdminDeviceEfficiencyService.java`

- [ ] Normalize query date range to the current month when omitted.
- [ ] Cap query windows to 366 days to protect dashboard queries.
- [ ] Return summary, real-time states, monthly trends, and heat ranking in one report.

### Task 3: Persistence And SQL

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/device/repository/AdminDeviceEfficiencyMapper.java`
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/device/repository/MyBatisAdminDeviceEfficiencyRepository.java`
- Create: `database/init/015_admin_device_efficiency.sql`

- [ ] Add device inventory, usage event, and daily summary tables.
- [ ] Implement summary, real-time state, trend, and ranking queries.

### Task 4: REST API And Docs

**Files:**
- Create: `backend/src/main/java/com/qizhifu/jiaoxuepeiyu/admin/device/controller/AdminDeviceEfficiencyController.java`
- Modify: `docs/admin-api-contract.md`
- Modify: `MEMORY.md`

- [ ] Add Swagger-documented dashboard, summary, real-time, monthly-trend, and heat-ranking endpoints.
- [ ] Document query parameters and response behavior.
- [ ] Record long-term data source decisions in project memory.

### Task 5: Verification And Commit

- [ ] Run available static checks.
- [ ] Sync codegraph.
- [ ] Commit as `feat: add admin device efficiency APIs`.
