# 交付前核查清单

## 代码入口

- 前端主工程在仓库根目录，源码目录是 `src/`。
- 后端工程在 `backend/`。
- 数据库脚本在 `database/init/`，按文件名前缀顺序执行。
- 部署脚本在 `deploy/`，开发辅助脚本在 `scripts/`。

## 本地启动

```bash
docker compose up -d mysql
./scripts/dev-backend.sh
./scripts/dev-frontend.sh
```

- 前端地址：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/health`
- 接口文档：`http://localhost:8080/swagger-ui/index.html`

## 构建检查

```bash
npm run build
cd backend && mvn clean package -DskipTests
```

说明：`mvn test` 需要可连接的 MySQL 环境。没有本地 MySQL 时，可以先运行非数据库相关单元测试或使用打包命令做编译检查。

## 线上地址

- 前端：`http://luoyan.xin`
- 健康检查：`http://luoyan.xin/api/health`
- 接口文档：`http://luoyan.xin/swagger-ui/index.html`
- OpenAPI JSON：`http://luoyan.xin/v3/api-docs`

## 部署包

完整离线包需要准备 JRE 8：

```bash
JRE8_HOME=/path/to/jre8 deploy/package.sh
```

打包结果输出到 `deploy/dist/`，其中：

- `app/jiaoxuepeiyu-backend.jar` 是后端服务。
- `web/` 是前端静态文件。
- `config/application.yml` 是运行配置。
- `runtime/jre8/` 是随包 Java 运行时。

## 交付注意

- 不要提交 `node_modules/`、`dist/`、`backend/target/`、`deploy/dist/`。
- 不要把生产数据库密码、服务器密码、Token 写进仓库。
- 前端请求默认走同域 `/api`，线上域名变化时优先调整 Nginx 代理，不要在代码里写死服务器 IP。
