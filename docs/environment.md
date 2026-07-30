# 开发环境说明

## 版本要求

- JDK：1.8。
- Node.js：18 以上。
- 数据库：MySQL 5.7.42。
- 前端框架：Vue 3，后续如需改 Vue 2.x 需单独调整工程。
- 后端框架：Spring Boot 2.7.18，兼容 Java 8。

## 首次启动

### 1. 启动 MySQL

```bash
docker compose up -d mysql
```

如不用 Docker，可自行安装 MySQL 5.7.42 后执行：

```bash
mysql -uroot -p < database/init/001_schema.sql
```

### 2. 启动后台

```bash
./scripts/dev-backend.sh
```

后台默认端口：`8080`。

健康检查：

```bash
curl http://localhost:8080/api/health
```

### 3. 启动前端

```bash
./scripts/dev-frontend.sh
```

前端默认端口：`5173`。

## 分支使用

占工：

```bash
git checkout zhan/frontend-test
```

陈工：

```bash
git checkout chen/backend
```

## Static Backend Verification

When the backend service is not started locally, run these checks from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-backend-static.ps1
powershell -ExecutionPolicy Bypass -File scripts\verify-deploy-layout.ps1
```

These checks verify the Java 8 build settings, OpenAPI dependency, MySQL target metadata, deployment runtime layout, bootstrap admin configuration, and the rule that database init scripts must not seed default users.
