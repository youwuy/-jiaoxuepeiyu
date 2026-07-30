@echo off
setlocal

set APP_HOME=%~dp0
set PID_FILE=%APP_HOME%app.pid

if not exist "%PID_FILE%" (
  echo Backend is not running.
  exit /b 0
)

set /p BACKEND_PID=<"%PID_FILE%"
tasklist /FI "PID eq %BACKEND_PID%" | find "%BACKEND_PID%" >nul
if errorlevel 1 (
  del "%PID_FILE%"
  echo Stale pid file removed.
  exit /b 0
)

taskkill /PID %BACKEND_PID% /T /F
del "%PID_FILE%"
echo Backend stopped, pid=%BACKEND_PID%
