# Admin Score And Archive Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline execution with TDD. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management-side semester score and training archive query APIs.

**Architecture:** Add two focused admin modules: `admin/score` reads `score_semester_summary` for lists, statistics, and ranking; `admin/archive` reads immutable `training_attempt` and `training_attempt_step` records. Both modules follow existing Controller -> Service -> Repository port -> MyBatis mapper patterns and use `PageResponse`.

**Tech Stack:** Java 8, Spring Boot 2.7.18, MyBatis annotation mappers, MySQL 5.7.42.0, springdoc-openapi.

## Global Constraints

- Keep Java source compatible with JDK 1.8.
- Use `/api/admin/scores/semester/**` and `/api/admin/archives/**`.
- Use existing score/archive fact tables; avoid duplicate persistence.
- Keep export endpoints metadata-first until binary Excel/PDF generation is wired.

---

### Task 1: Service Tests And Contracts

- [ ] Write score service tests for paging defaults, computed comprehensive score fallback, and statistics defaults.
- [ ] Write archive service tests for paging defaults and detail step loading.

### Task 2: Score Implementation

- [ ] Add score models, repository port, service, MyBatis repository/mapper, and Swagger controller.

### Task 3: Archive Implementation

- [ ] Add archive models, repository port, service, MyBatis repository/mapper, and Swagger controller.

### Task 4: Docs And Verification

- [ ] Update admin API contract and MEMORY.
- [ ] Run available static checks and codegraph sync.
- [ ] Commit as `feat: add admin score archive APIs`.
