$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

function Join-RepoPath {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Parts)
    $path = $root
    foreach ($part in $Parts) {
        $path = Join-Path $path $part
    }
    return $path
}

function Require-File {
    param(
        [string]$Path,
        [string]$Message
    )
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw $Message
    }
}

function Read-Text {
    param([string]$Path)
    return Get-Content -LiteralPath $Path -Raw
}

function Require-Content {
    param(
        [string]$Path,
        [string]$Pattern,
        [string]$Message
    )
    $content = Read-Text $Path
    if ($content -notmatch $Pattern) {
        throw $Message
    }
}

function Reject-Content {
    param(
        [string]$Path,
        [string]$Pattern,
        [string]$Message
    )
    $content = Read-Text $Path
    if ($content -match $Pattern) {
        throw $Message
    }
}

$pom = Join-RepoPath "backend" "pom.xml"
$appConfig = Join-RepoPath "backend" "src" "main" "resources" "application.yml"
$deployConfig = Join-RepoPath "deploy" "config" "application.yml.example"
$deployReadme = Join-RepoPath "deploy" "README.md"
$ciSmoke = Join-RepoPath "scripts" "ci-backend-smoke.sh"
$healthController = Join-RepoPath "backend" "src" "main" "java" "com" "qizhifu" "jiaoxuepeiyu" "controller" "HealthController.java"
$bootstrapService = Join-RepoPath "backend" "src" "main" "java" "com" "qizhifu" "jiaoxuepeiyu" "bootstrap" "BootstrapAdminService.java"
$bootstrapInitializer = Join-RepoPath "backend" "src" "main" "java" "com" "qizhifu" "jiaoxuepeiyu" "bootstrap" "BootstrapAdminInitializer.java"
$databaseInit = Join-RepoPath "database" "init"

Require-File $pom "backend/pom.xml is required."
Require-File $appConfig "backend application.yml is required."
Require-File $deployConfig "deploy/config/application.yml.example is required."
Require-File $deployReadme "deploy/README.md is required."
Require-File $ciSmoke "scripts/ci-backend-smoke.sh is required."
Require-File $healthController "HealthController is required."
Require-File $bootstrapService "BootstrapAdminService is required."
Require-File $bootstrapInitializer "BootstrapAdminInitializer is required."

Require-Content $pom "<java.version>1\.8</java.version>" "Backend Maven java.version must stay at 1.8."
Require-Content $pom "<source>1\.8</source>" "Maven compiler source must stay at 1.8."
Require-Content $pom "<target>1\.8</target>" "Maven compiler target must stay at 1.8."
Require-Content $pom "<artifactId>springdoc-openapi-ui</artifactId>" "Backend must keep springdoc OpenAPI UI dependency."
Require-Content $pom "<artifactId>mysql-connector-java</artifactId>" "Backend must keep MySQL JDBC runtime dependency."

Require-Content $healthController "MySQL 5\.7\.42\.0" "Health endpoint must report the requested MySQL 5.7.42.0 target."
Require-Content $appConfig "APP_ACCOUNT_INITIAL_PASSWORD" "Application config must expose account initial password via environment."
Require-Content $appConfig "APP_BOOTSTRAP_ADMIN_USERNAME" "Application config must expose bootstrap admin username via environment."
Require-Content $appConfig "APP_BOOTSTRAP_ADMIN_PASSWORD" "Application config must expose bootstrap admin password via environment."
Require-Content $deployConfig "APP_ACCOUNT_INITIAL_PASSWORD" "Deployment config must expose account initial password via environment."
Require-Content $deployConfig "APP_BOOTSTRAP_ADMIN_USERNAME" "Deployment config must expose bootstrap admin username via environment."
Require-Content $deployConfig "APP_BOOTSTRAP_ADMIN_PASSWORD" "Deployment config must expose bootstrap admin password via environment."
Require-Content $deployReadme "First Admin Bootstrap" "Deployment README must document first admin bootstrap."
Require-Content $deployReadme 'remove `APP_BOOTSTRAP_ADMIN_USERNAME` and `APP_BOOTSTRAP_ADMIN_PASSWORD`' "Deployment README must tell operators to remove bootstrap credentials after first startup."
Require-Content $ciSmoke "/api/health" "CI smoke test must verify the health endpoint."
Require-Content $ciSmoke "/api/auth/admin/login" "CI smoke test must verify admin login."
Require-Content $ciSmoke "/api/auth/current" "CI smoke test must verify current-user token lookup."
Require-Content $ciSmoke "/api/auth/logout" "CI smoke test must verify logout."
Require-Content $ciSmoke "jiaoxuepeiyu-backend-0\.1\.0\.jar" "CI smoke test must start the packaged backend jar."
Require-Content $ciSmoke "MySQL 5\.7\.42\.0" "CI smoke test must assert the requested MySQL target."
Require-Content $ciSmoke "APP_BOOTSTRAP_ADMIN_USERNAME" "CI smoke test must exercise bootstrap admin configuration."
Require-Content $ciSmoke "trap cleanup EXIT" "CI smoke test must stop the backend process after verification."
Reject-Content $ciSmoke 'APP_BOOTSTRAP_ADMIN_PASSWORD="\$\{APP_BOOTSTRAP_ADMIN_PASSWORD:-' "CI smoke test must not default the bootstrap admin password."
Reject-Content $ciSmoke 'APP_ACCOUNT_INITIAL_PASSWORD="\$\{APP_ACCOUNT_INITIAL_PASSWORD:-' "CI smoke test must not default the account initial password."

Require-Content $bootstrapInitializer "ApplicationRunner" "Bootstrap admin must run through Spring startup wiring."
Require-Content $bootstrapService "SKIPPED_ADMIN_EXISTS" "Bootstrap admin must skip when an admin already exists."
Require-Content $bootstrapService "passwordHasher\.hash\(password\)" "Bootstrap admin must hash the configured password before storing it."
Reject-Content $bootstrapService "System\.out|Logger|log\." "Bootstrap admin must not log bootstrap credential handling."

if (-not (Test-Path -LiteralPath $databaseInit -PathType Container)) {
    throw "database/init directory is required."
}

$sqlFiles = Get-ChildItem -LiteralPath $databaseInit -Filter "*.sql" -File | Sort-Object Name
if ($sqlFiles.Count -eq 0) {
    throw "database/init must contain ordered SQL files."
}

foreach ($file in $sqlFiles) {
    Reject-Content $file.FullName 'INSERT\s+INTO\s+`?sys_user`?' "Database init scripts must not seed default users or plaintext passwords."
}

Write-Host "Backend static verification passed."
