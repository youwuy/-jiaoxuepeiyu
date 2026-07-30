# Backend Implementation Plan

> Required branch: `chen/backend`.

## Goal

Implement the complete Java 8 backend for the education support platform described by the management-side and student-side requirement PDFs.

## Architecture

Use the existing Spring Boot 2.7.18 backend as a Java 8 modular monolith. Modules communicate through service interfaces and persist to MySQL 5.7.42 compatible tables.

## Global Constraints

- Java source and target compatibility must stay at `1.8`.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Backend APIs serve Vue management and student clients.
- Deployment package must include a Java runtime/library layout so target users do not need to install Java manually.
- No plaintext passwords or secrets in API responses, logs, docs, or memory.

## Tasks

### Task 1: Backend Foundation

- Add common API response and page response models.
- Add global exception mapping.
- Add reusable validators for phone, ID card, password, and required text.
- Add pure domain calculators for course progress and comprehensive score.
- Add initial tests for validators and calculators.

### Task 2: Auth And IAM

- Implement user, role, permission, and session tables.
- Implement admin/student login, logout, one-active-session rule, 24-hour token expiry, password hashing, disabled-account rejection, and current-user lookup.
- Implement role permission and data scope models.
- Add tests for login identity matching, disabled accounts, duplicate login invalidation, and password policy.

### Task 3: Organization And Configuration

- Implement organization tree management.
- Implement academic year/semester, major, class, classroom, NVR metadata, score weight, and score grade rule APIs.
- Enforce one current semester and year-to-two-semesters creation.
- Add tests for uniqueness, enable/disable visibility, current semester rule, and score weight logs.

### Task 4: Resources

- Implement personal resource upload metadata, file storage abstraction, search/filter, edit, batch edit, delete checks, public application, audit, rejection, and public resource version replacement.
- Generate student messages after public resource approval.
- Add tests for file validation, audit transitions, version replacement, and notification creation.

### Task 5: Question Bank And Papers

- Implement theory question CRUD, enable/disable, Excel import preview, validation, and operation logs.
- Implement theory paper manual assembly, automatic assembly, import preview, score editing, and validation.
- Add tests for question scoring metadata, paper validation, automatic assembly counts, and import failures.

### Task 6: Courses And Assignments

- Implement course lifecycle, chapter/content tree, courseware progress, assignment rules, publish/cancel publish, copy, statistics, and operation logs.
- Implement theory and training assignment attempts, answer saving, submission, report, retry, auto-scoring, manual review, deadline lock, and final score.
- Add tests for publication visibility, progress calculation, completion rules, deadline locking, and teacher review.

### Task 7: Training And Rooms

- Implement training course lifecycle, participant binding, training question selection, monitor snapshots, room creation/join/leave/dissolve/start, role claims, countdown state, and room archive.
- Use transactions and row locks for concurrent room state transitions.
- Add tests for one-active-room-per-student, room capacity, role claim conflicts, owner dissolve, and start validation.

### Task 8: Archives, Scores, Devices, And Messages

- Implement immutable training attempt archive, step records, recording metadata, score sheet queries/export, comprehensive score snapshots, device usage aggregation, and student message read state.
- Add tests for final score policy, score formula, message unread count, and archive detail fallbacks.

### Task 9: Deployment

- Update database init scripts.
- Add Windows start/stop scripts.
- Update package script to include `runtime/jre8`, backend jar, config examples, database scripts, logs, and upload directories.
- Verify build and package on Java 8.

## Verification Gates

- `mvn test` in `backend/`.
- `mvn package` in `backend/`.
- MySQL 5.7 schema import.
- API smoke checks for `/api/health`, login, current user, and one representative API per module.
- Package layout check under `deploy/dist` or equivalent output directory.
