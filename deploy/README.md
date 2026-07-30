# Deployment

## Runtime

- Backend runtime target: Java 8.
- Frontend build target: Node.js 18+.
- Database: MySQL 5.7.42.
- The deployment package must carry a Java runtime so target users do not need to install Java manually.

## Java Runtime Layout

Place an approved JRE 8 distribution here before packaging or before starting from an unpacked release:

```text
runtime/jre8/
```

The startup scripts use:

```text
runtime/jre8/bin/java
runtime\jre8\bin\java.exe
```

The runtime directory is not committed to Git. Add the correct platform runtime during release packaging.

## Package Layout

```text
dist/
  app/jiaoxuepeiyu-backend.jar
  database/init/*.sql
  logs/
  runtime/jre8/
  uploads/
  web/
  start-backend.sh
  stop-backend.sh
  start-backend.bat
  stop-backend.bat
```

## Database Configuration

The backend reads database settings from environment variables:

```bash
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_DATABASE=jiaoxuepeiyu
MYSQL_USER=root
MYSQL_PASSWORD=change-me
```

Do not write production passwords into the repository.
