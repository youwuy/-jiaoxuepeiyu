#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIST="$ROOT/deploy/dist"

mkdir -p "$DIST/app" "$DIST/web" "$DIST/runtime/jre8" "$DIST/logs" "$DIST/uploads"

cp "$ROOT/backend/target/jiaoxuepeiyu-backend-0.1.0.jar" "$DIST/app/jiaoxuepeiyu-backend.jar"
cp -R "$ROOT/frontend/dist/." "$DIST/web/"
cp "$ROOT/deploy/start-backend.sh" "$DIST/start-backend.sh"
cp "$ROOT/deploy/stop-backend.sh" "$DIST/stop-backend.sh"
cp "$ROOT/deploy/start-backend.bat" "$DIST/start-backend.bat"
cp "$ROOT/deploy/stop-backend.bat" "$DIST/stop-backend.bat"
cp -R "$ROOT/database" "$DIST/database"

cat > "$DIST/README.txt" <<'EOF'
Web education support platform deployment package

1. Place JRE 8 files under runtime/jre8.
2. Initialize MySQL 5.7.42 with SQL files under database/init in filename order.
3. Configure MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, and MYSQL_PASSWORD.
4. Run start-backend.sh on Linux/macOS or start-backend.bat on Windows.
5. The web directory contains frontend static files for Nginx or another web server.
EOF

echo "Deployment package generated: $DIST"
