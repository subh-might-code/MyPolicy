# MyPolicy Backend - Service Verification Script
# Run after all services have started (~2 min)

$ErrorActionPreference = "SilentlyContinue"
Write-Host "`n=== MyPolicy Backend - Service Verification ===" -ForegroundColor Cyan
Write-Host ""

$services = @(
    @{ Name = "Config";        Port = 8888; Path = "/actuator/health" },
    @{ Name = "Discovery";      Port = 8761; Path = "/" },
    @{ Name = "Customer";      Port = 8081; Path = "/api/v1/health" },
    @{ Name = "Policy";        Port = 8085; Path = "/api/v1/health" },
    @{ Name = "Data-pipeline"; Port = 8082; Path = "/api/portfolio/1" },
    @{ Name = "BFF";           Port = 8090; Path = "/api/bff/health" }
)

$allOk = $true
foreach ($svc in $services) {
    $url = "http://localhost:$($svc.Port)$($svc.Path)"
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        Write-Host "[OK] $($svc.Name) (port $($svc.Port)) - $($r.StatusCode)" -ForegroundColor Green
    } catch {
        $status = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "unreachable" }
        Write-Host "[FAIL] $($svc.Name) (port $($svc.Port)) - $status" -ForegroundColor Red
        $allOk = $false
    }
}

Write-Host ""
Write-Host "=== BFF Integration Test (Portfolio customerId=1) ===" -ForegroundColor Cyan
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8090/api/bff/portfolio/1" -UseBasicParsing -TimeoutSec 10
    Write-Host "[OK] Portfolio API - $($r.StatusCode)" -ForegroundColor Green
    Write-Host $r.Content
} catch {
    Write-Host "[FAIL] Portfolio API - $($_.Exception.Message)" -ForegroundColor Red
    $allOk = $false
}

Write-Host ""
Write-Host "=== Eureka Dashboard ===" -ForegroundColor Cyan
Write-Host "http://localhost:8761" -ForegroundColor White
Write-Host ""
if ($allOk) {
    Write-Host "All checks PASSED" -ForegroundColor Green
} else {
    Write-Host "Some checks FAILED - review above" -ForegroundColor Yellow
}
Write-Host ""
