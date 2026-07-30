#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$APP_HOME/app.pid"

if [ ! -f "$PID_FILE" ]; then
  echo "Backend is not running."
  exit 0
fi

PID="$(cat "$PID_FILE")"
if kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  rm -f "$PID_FILE"
  echo "Backend stopped, pid=$PID"
else
  rm -f "$PID_FILE"
  echo "Stale pid file removed."
fi
