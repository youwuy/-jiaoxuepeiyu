# Health Smoke Check Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline execution with TDD. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `/api/health` a documented deployment smoke-check endpoint.

**Architecture:** Keep the existing controller route and replace the anonymous response map with a typed `HealthStatus` DTO. Return service status, service name, Java runtime version, MySQL target version, and server time without touching database state.

**Tech Stack:** Java 8, Spring Boot 2.7.18, springdoc-openapi.

## Global Constraints

- Keep Java source compatible with JDK 1.8.
- Keep the endpoint public and side-effect free.
- Do not expose secrets, database usernames, hostnames, or passwords.

---

### Task 1: Typed Health Response

- [x] Add a controller unit test for health smoke-check fields.
- [x] Add `HealthStatus`.
- [x] Update `HealthController` to return `ApiResponse<HealthStatus>`.

### Task 2: Docs And Memory

- [x] Document `GET /api/health` in the API contract and deployment README.
- [x] Update MEMORY.

### Task 3: Verification And Commit

- [x] Run available static checks and codegraph sync.
- [ ] Commit as `chore: document health smoke check`.
