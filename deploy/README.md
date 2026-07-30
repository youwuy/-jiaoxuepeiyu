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

The command fails when the backend jar or JRE 8 runtime is missing. This is intentional: the final package must be runnable without asking users to install Java.

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
