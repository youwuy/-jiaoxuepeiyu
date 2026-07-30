#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
JAVA_BIN="$APP_HOME/runtime/jre8/bin/java"
APP_JAR="$APP_HOME/app/jiaoxuepeiyu-backend.jar"

if [ ! -x "$JAVA_BIN" ]; then
  echo "未找到内置 Java 运行库：$JAVA_BIN"
  echo "请将 JRE 8 放入 deploy/runtime/jre8 后再启动。"
  exit 1
fi

if [ ! -f "$APP_JAR" ]; then
  echo "未找到后台 Jar：$APP_JAR"
  exit 1
fi

exec "$JAVA_BIN" -jar "$APP_JAR"
