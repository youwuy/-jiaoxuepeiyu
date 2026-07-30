#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIST="$ROOT/deploy/dist"
BACKEND_JAR="$ROOT/backend/target/jiaoxuepeiyu-backend-0.1.0.jar"
FRONTEND_DIST="$ROOT/frontend/dist"
RUNTIME_SOURCE="${JRE8_HOME:-$ROOT/deploy/runtime/jre8}"

if [ ! -f "$BACKEND_JAR" ]; then
  echo "Backend jar not found: $BACKEND_JAR"
  echo "Run scripts/build-backend.sh before packaging."
  exit 1
fi

if [ ! -d "$RUNTIME_SOURCE" ]; then
  echo "JRE 8 runtime source not found: $RUNTIME_SOURCE"
  echo "Set JRE8_HOME to an approved JRE 8 directory, or place it under deploy/runtime/jre8."
  exit 1
fi

if [ ! -x "$RUNTIME_SOURCE/bin/java" ] && [ ! -f "$RUNTIME_SOURCE/bin/java.exe" ]; then
  echo "JRE 8 runtime source does not contain bin/java or bin/java.exe: $RUNTIME_SOURCE"
  exit 1
fi

rm -rf "$DIST"
mkdir -p "$DIST/app" "$DIST/config" "$DIST/web" "$DIST/runtime/jre8" "$DIST/logs" "$DIST/uploads"

cp "$BACKEND_JAR" "$DIST/app/jiaoxuepeiyu-backend.jar"
cp -R "$RUNTIME_SOURCE/." "$DIST/runtime/jre8/"

if [ -d "$FRONTEND_DIST" ]; then
  cp -R "$FRONTEND_DIST/." "$DIST/web/"
else
  cat > "$DIST/web/README.txt" <<'EOF'
Frontend static files were not packaged because frontend/dist was not found.
Build the Vue frontend before release packaging when a full web bundle is required.
EOF
fi

cp "$ROOT/deploy/config/application.yml.example" "$DIST/config/application.yml"
cp "$ROOT/deploy/start-backend.sh" "$DIST/start-backend.sh"
cp "$ROOT/deploy/stop-backend.sh" "$DIST/stop-backend.sh"
cp "$ROOT/deploy/start-backend.bat" "$DIST/start-backend.bat"
cp "$ROOT/deploy/stop-backend.bat" "$DIST/stop-backend.bat"
cp -R "$ROOT/database" "$DIST/database"
chmod +x "$DIST/start-backend.sh" "$DIST/stop-backend.sh"

cat > "$DIST/README.txt" <<'EOF'
Web education support platform deployment package

1. The Java runtime is bundled under runtime/jre8. Users do not need to install Java.
2. Initialize MySQL 5.7.42 with SQL files under database/init in filename order.
3. Edit config/application.yml or set MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, and APP_ACCOUNT_INITIAL_PASSWORD.
4. Run start-backend.sh on Linux/macOS or start-backend.bat on Windows.
5. Runtime logs are written under logs/.
6. The web directory contains frontend static files for Nginx or another web server when frontend/dist exists at packaging time.
EOF

echo "Deployment package generated: $DIST"
