#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIST="$ROOT/deploy/dist"

mkdir -p "$DIST/app" "$DIST/web" "$DIST/runtime"

cp "$ROOT/backend/target/jiaoxuepeiyu-backend-0.1.0.jar" "$DIST/app/jiaoxuepeiyu-backend.jar"
cp -R "$ROOT/frontend/dist/." "$DIST/web/"
cp "$ROOT/deploy/start-backend.sh" "$DIST/start-backend.sh"
cp -R "$ROOT/database" "$DIST/database"

cat > "$DIST/README.txt" <<'EOF'
Web教辅系统部署包

1. 将 JRE 8 放入 runtime/jre8。
2. 初始化 MySQL 5.7.42，执行 database/init 下 SQL。
3. 配置 MYSQL_HOST、MYSQL_PORT、MYSQL_DATABASE、MYSQL_USER、MYSQL_PASSWORD。
4. 执行 ./start-backend.sh 启动后台。
5. web 目录为前端静态资源，可由 Nginx 部署。
EOF

echo "部署包已生成：$DIST"
