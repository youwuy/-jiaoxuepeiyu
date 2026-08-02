# Web教辅系统

[![CI](https://github.com/youwuy/-jiaoxuepeiyu/actions/workflows/ci.yml/badge.svg)](https://github.com/youwuy/-jiaoxuepeiyu/actions/workflows/ci.yml)

项目启动说明见 [docs/project-division.md](docs/project-division.md)。
开发环境说明见 [docs/environment.md](docs/environment.md)。
交付核查清单见 [docs/delivery-checklist.md](docs/delivery-checklist.md)。

## 目录结构

- `src/`：Vue 3 前端主工程，根目录执行 `npm run dev` / `npm run build`。
- `backend/`：Java 8 + Spring Boot 后端服务。
- `database/`：MySQL 5.7.42 初始化和迁移脚本。
- `deploy/`：离线部署包脚本和启动脚本。
- `docs/`：接口、分工、环境和交付说明。
- `scripts/`：开发、构建和校验脚本。

## 分支

- `main`：稳定主分支。
- `zhan/frontend-test`：占工负责前端开发和测试工作。
- `chen/backend`：陈工负责后端全部开发工作。

## 快速启动

```bash
docker compose up -d mysql
./scripts/dev-backend.sh
./scripts/dev-frontend.sh
```

- 前端：http://localhost:5173
- 后端：http://localhost:8080/api/health

## Deployment

Backend release packaging is documented in [deploy/README.md](deploy/README.md).

Windows release packaging:

```bat
set JRE8_HOME=C:\path\to\jre8
deploy\package.bat
```

Linux/macOS release packaging:

```bash
JRE8_HOME=/path/to/jre8 deploy/package.sh
```

Both package commands require a JRE 8 source and copy it into `deploy/dist/runtime/jre8` so target users do not install Java manually.
