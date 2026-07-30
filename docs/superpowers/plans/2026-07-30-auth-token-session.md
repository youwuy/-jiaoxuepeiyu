# Auth Token Session Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline execution with TDD. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete token-backed current-user and logout behavior so existing admin and student APIs can use login sessions instead of only the temporary identity header.

**Architecture:** Keep the existing login/session table shape and add the smallest missing auth surface. `AuthService` validates active tokens and invalidates one token on logout; `AuthenticationFilter` resolves `Authorization: Bearer <token>` and attaches an `AuthenticatedUser` to the request; existing `AdminContext` and `StudentContext` prefer authenticated request users and fall back to `X-User-Id` for frontend compatibility.

**Tech Stack:** Java 8, Spring Boot 2.7.18 servlet filter, MyBatis annotation mappers, MySQL 5.7.42.0, springdoc-openapi.

## Global Constraints

- Keep Java source compatible with JDK 1.8.
- Do not hardcode secrets or token values outside tests.
- Preserve `X-User-Id` compatibility until all frontend callers use bearer tokens.
- Store only token hashes in the database.

---

### Task 1: Service And Context Tests

- [x] Add service tests for current user lookup, expired token rejection, and logout token invalidation.
- [x] Add context tests proving admin/student contexts read authenticated request users and reject cross-portal use.
- [x] Add bearer token resolver tests.

### Task 2: Token Service And Filter

- [x] Add `BearerTokenResolver`.
- [x] Add `AuthenticatedUserContext`.
- [x] Add servlet `AuthenticationFilter`.
- [x] Add `AuthService.currentUser` and `AuthService.logout`.
- [x] Add session repository token invalidation.

### Task 3: API And Docs

- [x] Add `GET /api/auth/current`.
- [x] Add `POST /api/auth/logout`.
- [x] Update admin/student API contracts and MEMORY.

### Task 4: Verification And Commit

- [x] Run available static checks and codegraph sync.
- [ ] Commit as `feat: add token current user APIs`.
