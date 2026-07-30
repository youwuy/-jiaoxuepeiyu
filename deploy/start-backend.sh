#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
JAVA_BIN="$APP_HOME/runtime/jre8/bin/java"
APP_JAR="$APP_HOME/app/jiaoxuepeiyu-backend.jar"
PID_FILE="$APP_HOME/app.pid"

if [ ! -x "$JAVA_BIN" ]; then
  echo "Bundled Java runtime not found: $JAVA_BIN"
  echo "Place JRE 8 under runtime/jre8 before starting the backend."
  exit 1
fi

if [ ! -f "$APP_JAR" ]; then
  echo "Backend jar not found: $APP_JAR"
  exit 1
fi

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "Backend is already running, pid=$(cat "$PID_FILE")"
  exit 0
fi

nohup "$JAVA_BIN" -jar "$APP_JAR" > "$APP_HOME/backend.log" 2>&1 &
echo "$!" > "$PID_FILE"
echo "Backend started, pid=$(cat "$PID_FILE")"
