# Deployment Runtime Package Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use inline execution with focused verification. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the deployment package enforce a bundled Java 8 runtime so target users do not need to install Java.

**Architecture:** Keep the current `deploy/dist` layout but make packaging fail unless a release machine provides an approved JRE 8 through `JRE8_HOME` or `deploy/runtime/jre8`. Copy that runtime into `dist/runtime/jre8`, include a production config template as `dist/config/application.yml`, and update startup scripts to use the bundled Java and config file.

**Tech Stack:** Bash packaging script, Windows batch start/stop scripts, Spring Boot external config, MySQL 5.7.42-compatible config.

## Global Constraints

- Do not commit the actual JRE binary runtime.
- The generated deployment package must include `runtime/jre8`.
- Do not store production passwords in the repository.
- Keep backend startup independent of a system Java installation.

---

### Task 1: Runtime-Aware Packaging

- [x] Update `deploy/package.sh` to fail when the backend jar is missing.
- [x] Update `deploy/package.sh` to fail when no JRE 8 source is available.
- [x] Copy the JRE 8 source into `deploy/dist/runtime/jre8`.
- [x] Copy database scripts, config, logs, uploads, web assets, and start/stop scripts.

### Task 2: Start And Stop Scripts

- [x] Update Linux/macOS start script to use bundled Java and `config/application.yml`.
- [x] Update Windows start script to use bundled Java, write PID, and write logs.
- [x] Update Windows stop script to stop the PID recorded by startup.

### Task 3: Documentation And Memory

- [x] Add `deploy/config/application.yml.example`.
- [x] Update `deploy/README.md`.
- [x] Update MEMORY.

### Task 4: Verification And Commit

- [x] Run available static checks.
- [ ] Commit as `chore: enforce bundled java runtime packaging`.
