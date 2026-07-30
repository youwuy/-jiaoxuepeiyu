# Web教辅系统

项目启动说明见 [docs/project-division.md](docs/project-division.md)。
开发环境说明见 [docs/environment.md](docs/environment.md)。

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
