@echo off
setlocal EnableExtensions

set ROOT=%~dp0..
for %%I in ("%ROOT%") do set ROOT=%%~fI
set DIST=%ROOT%\deploy\dist
set BACKEND_JAR=%ROOT%\backend\target\jiaoxuepeiyu-backend-0.1.0.jar
set FRONTEND_DIST=%ROOT%\frontend\dist

if defined JRE8_HOME (
  set RUNTIME_SOURCE=%JRE8_HOME%
) else (
  set RUNTIME_SOURCE=%ROOT%\deploy\runtime\jre8
)

if not exist "%BACKEND_JAR%" (
  echo Backend jar not found: %BACKEND_JAR%
  echo Run scripts\build-backend.sh or build the backend jar before packaging.
  exit /b 1
)

if not exist "%RUNTIME_SOURCE%\" (
  echo JRE 8 runtime source not found: %RUNTIME_SOURCE%
  echo Set JRE8_HOME to an approved JRE 8 directory, or place it under deploy\runtime\jre8.
  exit /b 1
)

if not exist "%RUNTIME_SOURCE%\bin\java.exe" if not exist "%RUNTIME_SOURCE%\bin\java" (
  echo JRE 8 runtime source does not contain bin\java.exe or bin\java: %RUNTIME_SOURCE%
  exit /b 1
)

if exist "%DIST%\" rmdir /s /q "%DIST%"
mkdir "%DIST%\app" "%DIST%\config" "%DIST%\web" "%DIST%\runtime\jre8" "%DIST%\logs" "%DIST%\uploads"
if errorlevel 1 exit /b 1

copy /y "%BACKEND_JAR%" "%DIST%\app\jiaoxuepeiyu-backend.jar" >nul
xcopy "%RUNTIME_SOURCE%\*" "%DIST%\runtime\jre8\" /E /I /Y >nul

if exist "%FRONTEND_DIST%\" (
  xcopy "%FRONTEND_DIST%\*" "%DIST%\web\" /E /I /Y >nul
) else (
  > "%DIST%\web\README.txt" echo Frontend static files were not packaged because frontend\dist was not found.
  >> "%DIST%\web\README.txt" echo Build the Vue frontend before release packaging when a full web bundle is required.
)

copy /y "%ROOT%\deploy\config\application.yml.example" "%DIST%\config\application.yml" >nul
copy /y "%ROOT%\deploy\start-backend.sh" "%DIST%\start-backend.sh" >nul
copy /y "%ROOT%\deploy\stop-backend.sh" "%DIST%\stop-backend.sh" >nul
copy /y "%ROOT%\deploy\start-backend.bat" "%DIST%\start-backend.bat" >nul
copy /y "%ROOT%\deploy\stop-backend.bat" "%DIST%\stop-backend.bat" >nul
xcopy "%ROOT%\database" "%DIST%\database\" /E /I /Y >nul

> "%DIST%\README.txt" echo Web education support platform deployment package
>> "%DIST%\README.txt" echo.
>> "%DIST%\README.txt" echo 1. The Java runtime is bundled under runtime\jre8. Users do not need to install Java.
>> "%DIST%\README.txt" echo 2. Initialize MySQL 5.7.42 with SQL files under database\init in filename order.
>> "%DIST%\README.txt" echo 3. Edit config\application.yml or set MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, and APP_ACCOUNT_INITIAL_PASSWORD.
>> "%DIST%\README.txt" echo 4. Run start-backend.bat on Windows or start-backend.sh on Linux/macOS.
>> "%DIST%\README.txt" echo 5. Runtime logs are written under logs\.
>> "%DIST%\README.txt" echo 6. The web directory contains frontend static files for IIS, Nginx, or another web server when frontend\dist exists at packaging time.

echo Deployment package generated: %DIST%
