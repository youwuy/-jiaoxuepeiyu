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

function Require-Content {
    param(
        [string]$Path,
        [string]$Pattern,
        [string]$Message
    )
    $content = Get-Content -LiteralPath $Path -Raw
    if ($content -notmatch $Pattern) {
        throw $Message
    }
}

$packageSh = Join-RepoPath "deploy" "package.sh"
$packageBat = Join-RepoPath "deploy" "package.bat"
$startBat = Join-RepoPath "deploy" "start-backend.bat"
$startSh = Join-RepoPath "deploy" "start-backend.sh"
$configExample = Join-RepoPath "deploy" "config" "application.yml.example"

Require-File $packageSh "deploy/package.sh is required."
Require-File $packageBat "deploy/package.bat is required for Windows release packaging."
Require-File $startBat "deploy/start-backend.bat is required."
Require-File $startSh "deploy/start-backend.sh is required."
Require-File $configExample "deploy/config/application.yml.example is required."

Require-Content $packageSh "runtime/jre8" "deploy/package.sh must copy a bundled JRE 8 runtime."
Require-Content $packageSh "JRE8_HOME" "deploy/package.sh must support JRE8_HOME."
Require-Content $packageSh "database" "deploy/package.sh must include database scripts."

Require-Content $packageBat "runtime\\jre8" "deploy/package.bat must copy a bundled JRE 8 runtime."
Require-Content $packageBat "JRE8_HOME" "deploy/package.bat must support JRE8_HOME."
Require-Content $packageBat "database" "deploy/package.bat must include database scripts."
Require-Content $packageBat "Backend jar not found" "deploy/package.bat must fail when the backend jar is missing."
Require-Content $packageBat "JRE 8 runtime source not found" "deploy/package.bat must fail when the runtime source is missing."
Require-Content $packageBat "Users do not need to install Java" "deploy/package.bat README output must state Java is bundled."

Require-Content $startBat "runtime\\jre8\\bin\\java.exe" "Windows startup must use the bundled Java runtime."
Require-Content $startSh "runtime/jre8/bin/java" "Unix startup must use the bundled Java runtime."
Require-Content $configExample "APP_ACCOUNT_INITIAL_PASSWORD" "Deployment config must expose the initial password environment variable."

Write-Host "Deployment layout verification passed."
