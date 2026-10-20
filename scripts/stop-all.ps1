$ErrorActionPreference = "Continue"

$Ports = @(
    8761,
    1245,
    1111,
    1251,
    1241,
    1235,
    1236,
    1237,
    1239,
    1240,
    1260,
    8080
)

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "        APNAEVENT STOP MANAGER          " -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

foreach ($Port in $Ports) {

    $Connections = Get-NetTCPConnection `
        -LocalPort $Port `
        -State Listen `
        -ErrorAction SilentlyContinue

    foreach ($Connection in $Connections) {

        $ProcessId = $Connection.OwningProcess

        if ($ProcessId -and $ProcessId -ne 0) {

            $Process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue

            if ($Process) {
                Write-Host "Stopping PID $ProcessId on port $Port ($($Process.ProcessName))..." -ForegroundColor Cyan
                Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
            }
        }
    }
}

Start-Sleep -Seconds 2

Write-Host ""
Write-Host "ApnaEvent service processes stopped." -ForegroundColor Green
Write-Host ""
