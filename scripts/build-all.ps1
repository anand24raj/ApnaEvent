$ErrorActionPreference = 'Stop'

$services = @(
  'Config-server', 'EurekaServer', 'Auth-Service', 'Customer-Service',
  'Venue-Service', 'Event-Service', 'Paymnt-Service', 'Booking-Service',
  'Notification-Service', 'RA-AI-Service', 'AdminServer', 'ApiGateway'
)

foreach ($service in $services) {
    Write-Host "===== Building $service =====" -ForegroundColor Cyan
    Push-Location (Join-Path $PSScriptRoot "..\$service")
    try {
        mvn clean package
        if ($LASTEXITCODE -ne 0) { throw "Build failed for $service" }
    } finally {
        Pop-Location
    }
}

Write-Host "All services built successfully." -ForegroundColor Green
