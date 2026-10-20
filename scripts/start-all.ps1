$ErrorActionPreference = "Continue"

$Root = Split-Path -Parent $PSScriptRoot
$LogDir = Join-Path $Root "logs"

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Services = @(
    @{ Name="EurekaServer";       Port=8761; Path="EurekaServer" },
    @{ Name="Config-server";      Port=1245; Path="Config-server" },
    @{ Name="AdminServer";        Port=1111; Path="AdminServer" },
    @{ Name="Auth-Service";       Port=1251; Path="Auth-Service" },
    @{ Name="Customer-Service";   Port=1241; Path="Customer-Service" },
    @{ Name="Event-Service";      Port=1235; Path="Event-Service" },
    @{ Name="Venue-Service";      Port=1236; Path="Venue-Service" },
    @{ Name="Booking-Service";    Port=1237; Path="Booking-Service" },
    @{ Name="Paymnt-Service";     Port=1239; Path="Paymnt-Service" },
    @{ Name="Notification-Service"; Port=1240; Path="Notification-Service" },
    @{ Name="RA-AI-Service";      Port=1260; Path="RA-AI-Service" },
    @{ Name="ApiGateway";         Port=8080; Path="ApiGateway" }
)

function Test-Port {
    param([int]$Port)

    try {
        $connection = Test-NetConnection `
            -ComputerName "localhost" `
            -Port $Port `
            -WarningAction SilentlyContinue

        return $connection.TcpTestSucceeded
    }
    catch {
        return $false
    }
}

function Wait-ForPort {
    param(
        [int]$Port,
        [string]$Name,
        [int]$TimeoutSeconds = 90
    )

    Write-Host "Waiting for $Name on port $Port..." -ForegroundColor Yellow

    $start = Get-Date

    while (((Get-Date) - $start).TotalSeconds -lt $TimeoutSeconds) {

        if (Test-Port $Port) {
            Write-Host "$Name is listening on port $Port." -ForegroundColor Green
            return $true
        }

        Start-Sleep -Seconds 2
    }

    Write-Host "WARNING: $Name did not become available within $TimeoutSeconds seconds." -ForegroundColor Red
    return $false
}

function Start-ServiceProject {
    param(
        [string]$Name,
        [string]$RelativePath
    )

    $ProjectPath = Join-Path $Root $RelativePath
    $LogFile = Join-Path $LogDir "$Name.log"

    Write-Host ""
    Write-Host "Starting $Name..." -ForegroundColor Cyan

    if (-not (Test-Path (Join-Path $ProjectPath "pom.xml"))) {
        Write-Host "ERROR: pom.xml not found for $Name" -ForegroundColor Red
        return
    }

    $Command = "cd `"$ProjectPath`"; mvn spring-boot:run *> `"$LogFile`""

    Start-Process `
        -FilePath "powershell.exe" `
        -ArgumentList "-NoExit", "-Command", $Command `
        -WorkingDirectory $ProjectPath | Out-Null

    Write-Host "$Name process started." -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       APNAEVENT STARTUP MANAGER        " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Infrastructure
Start-ServiceProject "EurekaServer" "EurekaServer"
Wait-ForPort 8761 "EurekaServer" 90 | Out-Null

Start-ServiceProject "Config-server" "Config-server"
Wait-ForPort 1245 "Config-server" 90 | Out-Null

Start-ServiceProject "AdminServer" "AdminServer"
Wait-ForPort 1111 "AdminServer" 90 | Out-Null

# Core services
Start-ServiceProject "Auth-Service" "Auth-Service"
Start-ServiceProject "Customer-Service" "Customer-Service"
Start-ServiceProject "Event-Service" "Event-Service"
Start-ServiceProject "Venue-Service" "Venue-Service"

Wait-ForPort 1251 "Auth-Service" 90 | Out-Null
Wait-ForPort 1241 "Customer-Service" 90 | Out-Null
Wait-ForPort 1235 "Event-Service" 90 | Out-Null
Wait-ForPort 1236 "Venue-Service" 90 | Out-Null

# Dependent services
Start-ServiceProject "Booking-Service" "Booking-Service"
Start-ServiceProject "Paymnt-Service" "Paymnt-Service"
Start-ServiceProject "Notification-Service" "Notification-Service"
Start-ServiceProject "RA-AI-Service" "RA-AI-Service"

Wait-ForPort 1237 "Booking-Service" 90 | Out-Null
Wait-ForPort 1239 "Paymnt-Service" 90 | Out-Null
Wait-ForPort 1240 "Notification-Service" 90 | Out-Null
Wait-ForPort 1260 "RA-AI-Service" 90 | Out-Null

# Gateway last
Start-ServiceProject "ApiGateway" "ApiGateway"
Wait-ForPort 8080 "ApiGateway" 90 | Out-Null

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "       APNAEVENT STARTUP COMPLETE       " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Gateway:       http://localhost:8080" -ForegroundColor White
Write-Host "Eureka:        http://localhost:8761" -ForegroundColor White
Write-Host "Admin Server:  http://localhost:1111" -ForegroundColor White

Write-Host ""
Write-Host "Service logs:" -ForegroundColor Yellow
Write-Host $LogDir -ForegroundColor Gray

Write-Host ""
Write-Host "IMPORTANT: Kafka localhost:9092 must be running for Kafka-dependent features." -ForegroundColor Yellow
Write-Host ""
