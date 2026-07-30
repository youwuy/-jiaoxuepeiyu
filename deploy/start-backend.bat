@echo off
setlocal

set APP_HOME=%~dp0
set JAVA_BIN=%APP_HOME%runtime\jre8\bin\java.exe
set APP_JAR=%APP_HOME%app\jiaoxuepeiyu-backend.jar

if not exist "%JAVA_BIN%" (
  echo Bundled Java runtime not found: %JAVA_BIN%
  echo Place JRE 8 under runtime\jre8 before starting the backend.
  exit /b 1
)

if not exist "%APP_JAR%" (
  echo Backend jar not found: %APP_JAR%
  exit /b 1
)

start "jiaoxuepeiyu-backend" "%JAVA_BIN%" -jar "%APP_JAR%"
echo Backend start command issued.
