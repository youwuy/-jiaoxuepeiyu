#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP_JAR="$ROOT/backend/target/jiaoxuepeiyu-backend-0.1.0.jar"
LOG_FILE="$ROOT/backend-smoke.log"
PID_FILE="$ROOT/backend-smoke.pid"
BASE_URL="http://127.0.0.1:8080"

if [ ! -f "$APP_JAR" ]; then
  echo "Backend jar not found: $APP_JAR" >&2
  exit 1
fi

require_env() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    echo "$name is required for backend CI smoke verification." >&2
    exit 1
  fi
}

cleanup() {
  if [ -f "$PID_FILE" ]; then
    local pid
    pid="$(cat "$PID_FILE")"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      wait "$pid" 2>/dev/null || true
    fi
  fi
}
trap cleanup EXIT

export SERVER_PORT=8080
export MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
export MYSQL_PORT="${MYSQL_PORT:-3306}"
export MYSQL_DATABASE="${MYSQL_DATABASE:-jiaoxuepeiyu}"
export MYSQL_USER="${MYSQL_USER:-root}"
export MYSQL_PASSWORD="${MYSQL_PASSWORD:-root123456}"
export APP_BOOTSTRAP_ADMIN_REAL_NAME="${APP_BOOTSTRAP_ADMIN_REAL_NAME:-CI Administrator}"

require_env APP_ACCOUNT_INITIAL_PASSWORD
require_env APP_BOOTSTRAP_ADMIN_USERNAME
require_env APP_BOOTSTRAP_ADMIN_PASSWORD

nohup java -jar "$APP_JAR" >"$LOG_FILE" 2>&1 &
echo "$!" > "$PID_FILE"

for _ in $(seq 1 60); do
  if curl -fsS "$BASE_URL/api/health" >/tmp/backend-health.json; then
    break
  fi
  sleep 2
done

if ! curl -fsS "$BASE_URL/api/health" >/tmp/backend-health.json; then
  echo "Backend health check failed. Last log lines:" >&2
  tail -n 120 "$LOG_FILE" >&2 || true
  exit 1
fi

python3 - <<'PY'
import json

with open("/tmp/backend-health.json", "r", encoding="utf-8") as fh:
    payload = json.load(fh)

if payload.get("code") != 0:
    raise SystemExit("Health response code is not 0")

data = payload.get("data") or {}
if data.get("status") != "OK":
    raise SystemExit("Health status is not OK")
if data.get("databaseVersionTarget") != "MySQL 5.7.42.0":
    raise SystemExit("Health database target is not MySQL 5.7.42.0")
PY

curl -fsS \
  -H "Content-Type: application/json" \
  -d "{\"loginType\":\"username\",\"account\":\"$APP_BOOTSTRAP_ADMIN_USERNAME\",\"password\":\"$APP_BOOTSTRAP_ADMIN_PASSWORD\"}" \
  "$BASE_URL/api/auth/admin/login" >/tmp/backend-login.json

TOKEN="$(python3 - <<'PY'
import json

with open("/tmp/backend-login.json", "r", encoding="utf-8") as fh:
    payload = json.load(fh)

if payload.get("code") != 0:
    raise SystemExit("Login response code is not 0")

token = ((payload.get("data") or {}).get("token") or "").strip()
if not token:
    raise SystemExit("Login token is missing")

print(token)
PY
)"

curl -fsS -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/auth/current" >/tmp/backend-current.json

python3 - <<'PY'
import json

with open("/tmp/backend-current.json", "r", encoding="utf-8") as fh:
    payload = json.load(fh)

if payload.get("code") != 0:
    raise SystemExit("Current-user response code is not 0")

data = payload.get("data") or {}
if data.get("userType") != "admin":
    raise SystemExit("Current user is not an admin")
PY

curl -fsS -X POST -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/auth/logout" >/tmp/backend-logout.json

echo "Backend CI smoke verification passed."
