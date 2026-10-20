$Services = @(
    @{ Name="EurekaServer"; Port=8761 },
    @{ Name="Config-server"; Port=1245 },
    @{ Name="AdminServer"; Port=1111 },
    @{ Name="Auth-Service"; Port=1251 },
    @{ Name="Customer-Service"; Port=1241 },
    @{ Name="Event-Service"; Port=1235 },
    @{ Name="Venue-Service"; Port=1236 },
    @{ Name="Booking-Service"; Port=1237 },
    @{ Name="Paymnt-Service"; Port=1239 },
    @{ Name="Notification-Service"; Port=1240 },
    @{ Name="RA-AI-Service"; Port=1260 },
    @{ Name="ApiGateway"; Port=8080 }
)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       APNAEVENT HEALTH CHECK           " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($Service in $Services) {

    $Result = Test-NetConnection `
        -ComputerName "localhost" `
        -Port $Service.Port `
        -WarningAction SilentlyContinue

    if ($Result.TcpTestSucceeded) {
        Write-Host ("{0,-25} : UP   : {1}" -f $Service.Name, $Service.Port) -ForegroundColor Green
    }
    else {
        Write-Host ("{0,-25} : DOWN : {1}" -f $Service.Name, $Service.Port) -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Kafka check:" -ForegroundColor Yellow

$Kafka = Test-NetConnection `
    -ComputerName "localhost" `
    -Port 9092 `
    -WarningAction SilentlyContinue

if ($Kafka.TcpTestSucceeded) {
    Write-Host "Kafka                     : UP   : 9092" -ForegroundColor Green
}
else {
    Write-Host "Kafka                     : DOWN : 9092" -ForegroundColor Red
}

Write-Host ""
