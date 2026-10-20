$ErrorActionPreference = "Stop"
$scriptPath = Join-Path $PSScriptRoot "scripts\build-all.ps1"
if (-not (Test-Path $scriptPath)) {
    throw "Build script not found: $scriptPath"
}
& $scriptPath
if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) {
    throw "ApnaEvent build failed. Review the service output above."
}
