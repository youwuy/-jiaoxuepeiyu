# Project Instructions

## Project Context

- Project name: `-jiaoxuepeiyu`.
- Backend owner branch: `chen/backend`.
- Backend stack target: Java, JDK 1.8 compatible.
- Frontend stack target: Vue 2.2 or Vue 3.0, Node.js 18+.
- Database target: MySQL 5.7.42.0.
- Deployment package must include Java runtime/library support so target users do not need to install Java manually.

## Repository Layout

- `backend/`: Java backend service.
- `frontend/`: Vue frontend.
- `database/`: database initialization and migration assets.
- `deploy/`: deployment and packaging scripts.
- `docs/`: project documentation.
- `scripts/`: development helper scripts.

## Working Rules

- Work on `chen/backend` for backend changes.
- Read `AGENTS.md` and `MEMORY.md` before changing project files.
- Keep backend changes scoped to the requested backend system.
- Do not hardcode secrets, tokens, API keys, passwords, or private keys.
- Do not print or expose secret values.
- Prefer existing repository patterns and dependencies before adding new ones.
- Add tests before production code for new backend behavior when practical.

## Verification

- Run backend tests before reporting backend completion.
- Run backend build/package checks before pushing.
- Verify MySQL schema compatibility with MySQL 5.7.42.0.
- Verify deployment scripts include a Java runtime or clearly package an approved runtime directory.

## Git

- Do not use destructive Git commands unless explicitly requested.
- Preserve unrelated user changes.
- Push completed backend work to the remote `chen/backend` branch.
