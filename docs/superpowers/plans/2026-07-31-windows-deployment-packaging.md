# Windows Deployment Packaging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Windows-native deployment package path that bundles the backend jar, config, database scripts, start/stop scripts, upload/log directories, and an approved JRE 8 runtime.

**Architecture:** Keep `deploy/package.sh` as the Linux/macOS packager and add `deploy/package.bat` with the same package contract for Windows release machines. Add a lightweight PowerShell verification script so deployment invariants can be checked without Maven or Java installed.

**Tech Stack:** Windows batch, PowerShell static verification, Java 8 backend artifact layout, MySQL 5.7.42.0 SQL assets.

## Global Constraints

- Java source and target compatibility must stay at `1.8`.
- Database SQL must stay compatible with MySQL 5.7.42.0.
- Backend APIs serve Vue management and student clients.
- Deployment package must include a Java runtime/library layout so target users do not need to install Java manually.
- No plaintext passwords or secrets in API responses, logs, docs, or memory.

---

### Task 1: Deployment Layout Verifier

**Files:**
- Create: `scripts/verify-deploy-layout.ps1`

**Interfaces:**
- Produces: a static verification command that fails when required deployment scripts or runtime-copy checks are missing.

- [x] Write the verifier before `deploy/package.bat` exists so it fails on the missing Windows packager.
- [x] Verify the failure is caused by `deploy/package.bat` not existing.

### Task 2: Windows Packager

**Files:**
- Create: `deploy/package.bat`

**Interfaces:**
- Consumes:
  - `backend\target\jiaoxuepeiyu-backend-0.1.0.jar`
  - `%JRE8_HOME%` or `deploy\runtime\jre8`
  - `deploy\config\application.yml.example`
  - `database\`
  - optional `frontend\dist`
- Produces:
  - `deploy\dist\app\jiaoxuepeiyu-backend.jar`
  - `deploy\dist\runtime\jre8\`
  - `deploy\dist\config\application.yml`
  - `deploy\dist\database\`
  - `deploy\dist\logs\`
  - `deploy\dist\uploads\`
  - `deploy\dist\web\`
  - `deploy\dist\start-backend.bat`
  - `deploy\dist\stop-backend.bat`
  - `deploy\dist\start-backend.sh`
  - `deploy\dist\stop-backend.sh`

- [x] Fail when the backend jar is missing.
- [x] Fail when no JRE 8 source exists.
- [x] Fail when the JRE source does not contain `bin\java.exe` or `bin\java`.
- [x] Copy runtime, app jar, config, database scripts, start/stop scripts, logs, uploads, and optional frontend assets.
- [x] Write a package README that explicitly says Java is bundled and users do not install Java manually.

### Task 3: Documentation And Memory

**Files:**
- Modify: `deploy/README.md`
- Modify: `README.md`
- Modify: `MEMORY.md`

**Interfaces:**
- Produces: operator-facing deployment instructions for Linux/macOS and Windows package generation.

- [x] Document `deploy\package.bat` usage and runtime source choices.
- [x] Mention the Windows package command in root README.
- [x] Record the Windows packager decision in `MEMORY.md`.

### Task 4: Verification And Git

**Verification:**
- [x] Run `powershell -ExecutionPolicy Bypass -File scripts/verify-deploy-layout.ps1`.
- [x] Run `git diff --check`.
- [x] Run sensitive value scan on touched files.
- [x] Run `codegraph.cmd sync`.

**Git:**
- [ ] Commit as `chore: add windows deployment packaging`.
- [ ] Push `chen/backend`.
- [ ] Merge/sync to `main` and push after successful branch push.
