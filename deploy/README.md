# 部署说明

## 运行环境

- 后端运行目标：Java 8。
- 前端构建环境：Node 18+。
- 数据库：MySQL 5.7.42。
- 部署包需要携带 Java 运行库，客户服务器不需要额外安装 Java。

## Java 运行库携带方式

正式交付时将 JRE 8 放入：

```text
deploy/runtime/jre8/
```

启动脚本默认使用：

```text
deploy/runtime/jre8/bin/java
```

该目录不提交到 Git，避免把平台相关二进制文件放入源码仓库。交付部署包时由打包流程或人工放入对应服务器平台的 JRE 8。

## 本地开发启动

```bash
docker compose up -d mysql
./scripts/dev-backend.sh
./scripts/dev-frontend.sh
```

访问：

- 前端：http://localhost:5173
- 后端健康检查：http://localhost:8080/api/health

## 生产部署配置

后端通过环境变量读取数据库配置：

```bash
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=jiaoxuepeiyu
MYSQL_USER=root
MYSQL_PASSWORD=root123456
```

生产环境密码需由部署方单独配置，不写入公开仓库。
