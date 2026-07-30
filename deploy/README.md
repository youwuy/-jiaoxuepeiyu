# Deployment

## Runtime

- Backend runtime target: Java 8.
- Frontend build target: Node.js 18+.
- Database: MySQL 5.7.42.
- The deployment package carries a Java runtime under `runtime/jre8`, so target users do not need to install Java manually.

## Java Runtime Layout

Release packaging requires an approved JRE 8 distribution. Use either path:

```text
deploy/runtime/jre8/
```

or set:

```bash
JRE8_HOME=/path/to/jre8
```

`deploy/package.sh` copies that runtime into:

```text
deploy/dist/runtime/jre8/
```

The runtime directory is not committed to Git. Add the correct platform runtime on the release machine before packaging. The generated `dist` package must contain the Java executable for its target platform:

```text
runtime/jre8/bin/java       # Linux/macOS package
runtime\jre8\bin\java.exe   # Windows package
```

## Package Layout

```text
dist/
  app/jiaoxuepeiyu-backend.jar
  database/init/*.sql
  logs/
  runtime/jre8/
  uploads/
  web/
  config/application.yml
  start-backend.sh
  stop-backend.sh
  start-backend.bat
  stop-backend.bat
```

## Database Configuration

The generated package contains `config/application.yml`. It reads database settings from environment variables:

Set `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, and `APP_ACCOUNT_INITIAL_PASSWORD` in the deployment environment.

Do not write production passwords into the repository.

## File Storage

Uploaded files are stored locally and served from the backend process.

Set these environment variables when the default package paths are not appropriate:

```bash
APP_FILE_UPLOAD_ROOT=/path/to/persistent/uploads
APP_FILE_PUBLIC_PREFIX=/uploads
APP_FILE_MAX_SIZE=200MB
APP_FILE_MAX_SIZE_BYTES=209715200
```

Keep `APP_FILE_UPLOAD_ROOT` on persistent storage so uploaded courseware, covers, previews, and attachments survive service upgrades.

## Packaging

Build the backend first:

```bash
scripts/build-backend.sh
```

Build the frontend before full web release packaging:

```bash
scripts/build-frontend.sh
```

Create the deployment package:

```bash
JRE8_HOME=/path/to/jre8 deploy/package.sh
```

Windows release machines can use the native batch packager:

```bat
set JRE8_HOME=C:\path\to\jre8
deploy\package.bat
```

If `JRE8_HOME` is not set, both packagers read the runtime from `deploy/runtime/jre8`.

The command fails when the backend jar or JRE 8 runtime is missing. This is intentional: the final package must be runnable without asking users to install Java.

Before release, run the static deployment layout check:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-deploy-layout.ps1
```

## Start And Stop

Linux/macOS:

```bash
./start-backend.sh
./stop-backend.sh
```

Windows:

```bat
start-backend.bat
stop-backend.bat
```

Logs are written to `logs/backend.log` and `logs/backend-error.log`.

## Smoke Check

After startup, verify the backend:

```text
GET http://127.0.0.1:8080/api/health
```

The response contains `status`, `service`, `javaVersion`, `databaseVersionTarget`, and `time`.
