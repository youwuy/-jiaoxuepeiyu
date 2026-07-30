@echo off
setlocal

set APP_HOME=%~dp0
set JAVA_BIN=%APP_HOME%runtime\jre8\bin\java.exe
set APP_JAR=%APP_HOME%app\jiaoxuepeiyu-backend.jar
set CONFIG_FILE=%APP_HOME%config\application.yml
set LOG_DIR=%APP_HOME%logs
set PID_FILE=%APP_HOME%app.pid

if not exist "%JAVA_BIN%" (
  echo Bundled Java runtime not found: %JAVA_BIN%
  exit /b 1
)

if not exist "%APP_JAR%" (
  echo Backend jar not found: %APP_JAR%
  exit /b 1
)

if not exist "%CONFIG_FILE%" (
  echo Backend config not found: %CONFIG_FILE%
  exit /b 1
)

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

for /f %%p in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "$p=Start-Process -FilePath '%JAVA_BIN%' -ArgumentList @('-jar','%APP_JAR%','--spring.config.additional-location=file:%CONFIG_FILE%') -RedirectStandardOutput '%LOG_DIR%\backend.log' -RedirectStandardError '%LOG_DIR%\backend-error.log' -PassThru; $p.Id"') do set BACKEND_PID=%%p

if "%BACKEND_PID%"=="" (
  echo Backend failed to start.
  exit /b 1
)

echo %BACKEND_PID% > "%PID_FILE%"
echo Backend started, pid=%BACKEND_PID%
