$ErrorActionPreference = 'Stop'
$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { 'http://localhost:8080' }

function Check-Http($url) {
    $r = Invoke-WebRequest -Uri $url -UseBasicParsing
    if ($r.StatusCode -lt 200 -or $r.StatusCode -ge 300) { throw "HTTP check failed: $url -> $($r.StatusCode)" }
    Write-Host "PASS $url" -ForegroundColor Green
}

Write-Host "Checking Gateway health..." -ForegroundColor Cyan
Check-Http "$BaseUrl/actuator/health"

Write-Host "Checking public Event/Venue APIs..." -ForegroundColor Cyan
Check-Http "$BaseUrl/event/view-all"
Check-Http "$BaseUrl/venue/view-all"

Write-Host "Checking Auth/JWT..." -ForegroundColor Cyan
if ([string]::IsNullOrWhiteSpace($env:AUTH_USERNAME) -or [string]::IsNullOrWhiteSpace($env:AUTH_PASSWORD)) {
    throw "Set AUTH_USERNAME and AUTH_PASSWORD in this PowerShell session before running the smoke test. Do not use a hard-coded default password."
}
$login = Invoke-RestMethod -Uri "$BaseUrl/auth/token" -Method Post -ContentType 'application/json' -Body (@{
    username = $env:AUTH_USERNAME
    password = $env:AUTH_PASSWORD
} | ConvertTo-Json)
if (-not $login.token) { throw 'JWT token was not returned.' }
if ($login.role -ne 'ADMIN') { throw 'Smoke test credentials must belong to an ADMIN account because the test checks administrative endpoints.' }
$headers = @{ Authorization = "Bearer $($login.token)" }
Write-Host 'PASS JWT token returned' -ForegroundColor Green

Write-Host "Checking protected APIs..." -ForegroundColor Cyan
$r = Invoke-RestMethod -Uri "$BaseUrl/customer/view-all" -Headers $headers
$r = Invoke-RestMethod -Uri "$BaseUrl/booking/view-all" -Headers $headers
$r = Invoke-RestMethod -Uri "$BaseUrl/paymnt/stats/revenue" -Headers $headers
Write-Host 'PASS protected APIs' -ForegroundColor Green

Write-Host "Checking infrastructure and browser UI routes..." -ForegroundColor Cyan
Check-Http "http://localhost:8761/"
Check-Http "http://localhost:1111/"
$r = Invoke-RestMethod -Uri "$BaseUrl/ai/suggestions" -Headers $headers
Write-Host "PASS RA AI suggestions" -ForegroundColor Green
$uiPaths = @(
    '/auth-ui/login.html',
    '/auth-ui/register.html',
    '/customer-ui/index.html',
    '/venue-ui/index.html',
    '/event-ui/index.html',
    '/booking-ui/index.html',
    '/payment-ui/index.html',
    '/notification-ui/index.html'
)
foreach ($path in $uiPaths) { Check-Http "$BaseUrl$path" }

Write-Host "All smoke checks passed." -ForegroundColor Green
