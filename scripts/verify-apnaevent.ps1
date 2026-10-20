$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$services = @('EurekaServer','Config-server','Auth-Service','Customer-Service','Venue-Service','Event-Service','Booking-Service','Paymnt-Service','Notification-Service','RA-AI-Service','ApiGateway','AdminServer')
Write-Host "ApnaEvent build verification" -ForegroundColor Cyan
foreach ($service in $services) {
    $dir = Join-Path $root $service
    $wrapper = Join-Path $dir 'mvnw.cmd'
    if (-not (Test-Path $wrapper)) { throw "$service : mvnw.cmd not found; verification cannot be reported as successful." }
    Write-Host "`n=== $service ===" -ForegroundColor Yellow
    Push-Location $dir
    try {
        & $wrapper -q test
        if ($LASTEXITCODE -ne 0) { throw "$service test failed" }
        Write-Host "$service : tests passed" -ForegroundColor Green
    } finally { Pop-Location }
}
Write-Host "`nAll requested service test checks passed." -ForegroundColor Green
