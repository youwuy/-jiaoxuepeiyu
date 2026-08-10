# Admin IAM Role Permission Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline execution with TDD. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add management-side role and permission management APIs.

**Architecture:** Add `admin/iam` with Controller -> Service -> Repository port -> MyBatis mapper. Permissions are read from `sys_permission` as a tree; roles are managed in `sys_role`, role permission bindings in `sys_role_permission`, and role operation logs in `sys_role_log`. Each selected page stores its own `data_scope` in `sys_role_permission`; `sys_role.data_scope` remains only as a legacy compatibility default.

**Tech Stack:** Java 8, Spring Boot 2.7.18, MyBatis annotation mappers, MySQL 5.7.42.0, springdoc-openapi.

## Global Constraints

- Keep Java source compatible with JDK 1.8.
- Use `/api/admin/permissions/tree` and `/api/admin/roles/**`.
- Do not hardcode secrets.
- Keep account role bindings compatible with existing `sys_user_role`.

---

### Task 1: Tests And Models

- [x] Write service tests for permission tree assembly, role create validation, data scope validation, and permission replacement normalization.

### Task 2: Service And Persistence

- [x] Add role/permission models, repository port, service, MyBatis repository/mapper, and SQL migration.

### Task 3: API And Docs

- [x] Add Swagger controller endpoints.
- [x] Update admin API contract and MEMORY.

### Task 4: Verification And Commit

- [x] Run available static checks and codegraph sync.
- [ ] Commit as `feat: add admin iam role permission APIs`.
