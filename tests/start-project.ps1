$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)

Write-Host '1. Checking Java...' -ForegroundColor Cyan
java -version

Write-Host '2. Checking Maven...' -ForegroundColor Cyan
mvn -version

Write-Host '3. Checking MySQL service...' -ForegroundColor Cyan
$mysql = Get-Service -Name 'MySQL80' -ErrorAction SilentlyContinue

if ($null -eq $mysql) {
    throw 'MySQL80 service was not found.'
}

if ($mysql.Status -ne 'Running') {
    Write-Host 'MySQL80 is not running. Starting it...' -ForegroundColor Yellow
    Start-Service -Name 'MySQL80'
}

Write-Host 'MySQL is running.' -ForegroundColor Green

Write-Host '4. Checking required environment variables...' -ForegroundColor Cyan

$required = @(
    'JWT_SECRET',
    'AUTH_PASSWORD',
    'DB_PASSWORD'
)

foreach ($name in $required) {
    $value = [Environment]::GetEnvironmentVariable($name)

    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Required environment variable '$name' is not set."
    }
}

Write-Host 'Required environment variables are available.' -ForegroundColor Green

Write-Host '5. Building all services...' -ForegroundColor Cyan
& "$PSScriptRoot\..\build-all.ps1"

if ($LASTEXITCODE -ne 0) {
    throw 'Build failed.'
}

Write-Host '6. Running smoke tests...' -ForegroundColor Cyan
& "$PSScriptRoot\smoke-test.ps1"

if ($LASTEXITCODE -ne 0) {
    throw 'Smoke tests failed.'
}

Write-Host ''
Write-Host 'Platform validation completed successfully.' -ForegroundColor Green
Write-Host 'Gateway: http://localhost:8080/' -ForegroundColor Green
Write-Host 'Login:   http://localhost:8080/auth-ui/login.html' -ForegroundColor Green
Write-Host 'RA is available on the project HTML pages.' -ForegroundColor Green
