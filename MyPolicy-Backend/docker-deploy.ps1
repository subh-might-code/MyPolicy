# MyPolicy Backend - Docker Quick Start Script

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     MyPolicy Backend - Docker Deployment Manager          ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
function Test-DockerRunning {
    try {
        docker ps | Out-Null
        return $true
    } catch {
        return $false
    }
}

# Check Docker Compose version
function Test-DockerCompose {
    try {
        docker compose version | Out-Null
        return $true
    } catch {
        return $false
    }
}

# Main Menu
function Show-Menu {
    Write-Host "🐳 Docker Deployment Options:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  1. 🚀 Build and Start All Services" -ForegroundColor Green
    Write-Host "  2. 📊 View Service Status" -ForegroundColor Cyan
    Write-Host "  3. 📋 View All Logs" -ForegroundColor Cyan
    Write-Host "  4. 🔍 View Specific Service Logs" -ForegroundColor Cyan
    Write-Host "  5. 🔄 Restart All Services" -ForegroundColor Yellow
    Write-Host "  6. 🔄 Restart Specific Service" -ForegroundColor Yellow
    Write-Host "  7. 🛑 Stop All Services" -ForegroundColor Red
    Write-Host "  8. 🗑️  Stop and Remove All (Keep Data)" -ForegroundColor Red
    Write-Host "  9. ⚠️  Full Clean (Removes ALL Data)" -ForegroundColor Red
    Write-Host "  10. 🏥 Health Check All Services" -ForegroundColor Magenta
    Write-Host "  11. 📦 Show Resource Usage" -ForegroundColor Cyan
    Write-Host "  0. ❌ Exit" -ForegroundColor White
    Write-Host ""
}

# Function 1: Build and Start
function Start-AllServices {
    Write-Host "`n🚀 Building and starting all services..." -ForegroundColor Yellow
    Write-Host "This may take several minutes on first run...`n" -ForegroundColor Gray
    
    docker compose up -d --build
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ All services started successfully!" -ForegroundColor Green
        Write-Host "`n⏳ Waiting for services to become healthy (30 seconds)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30
        Show-ServiceStatus
    } else {
        Write-Host "`n❌ Failed to start services. Check logs for details." -ForegroundColor Red
    }
}

# Function 2: Service Status
function Show-ServiceStatus {
    Write-Host "`n📊 Service Status:" -ForegroundColor Cyan
    docker compose ps
}

# Function 3: View All Logs
function Show-AllLogs {
    Write-Host "`n📋 Showing logs for all services (Ctrl+C to exit)..." -ForegroundColor Cyan
    docker compose logs -f
}

# Function 4: View Specific Logs
function Show-SpecificLogs {
    Write-Host "`n🔍 Available Services:" -ForegroundColor Cyan
    Write-Host "  1. config-service" -ForegroundColor White
    Write-Host "  2. customer-service" -ForegroundColor White
    Write-Host "  3. policy-service" -ForegroundColor White
    Write-Host "  4. data-pipeline-service" -ForegroundColor White
    Write-Host "  5. bff-service" -ForegroundColor White
    Write-Host "  6. mongodb" -ForegroundColor White
    
    $service = Read-Host "`nEnter service number"
    
    $serviceMap = @{
        "1" = "config-service"
        "2" = "customer-service"
        "3" = "policy-service"
        "4" = "data-pipeline-service"
        "5" = "bff-service"
        "6" = "mongodb"
    }
    
    if ($serviceMap.ContainsKey($service)) {
        Write-Host "`n📋 Showing logs for $($serviceMap[$service]) (Ctrl+C to exit)..." -ForegroundColor Cyan
        docker compose logs -f $serviceMap[$service]
    } else {
        Write-Host "`n❌ Invalid selection" -ForegroundColor Red
    }
}

# Function 5: Restart All
function Restart-AllServices {
    Write-Host "`n🔄 Restarting all services..." -ForegroundColor Yellow
    docker compose restart
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ All services restarted successfully!" -ForegroundColor Green
        Start-Sleep -Seconds 10
        Show-ServiceStatus
    } else {
        Write-Host "`n❌ Failed to restart services." -ForegroundColor Red
    }
}

# Function 6: Restart Specific Service
function Restart-SpecificService {
    Write-Host "`n🔄 Available Services:" -ForegroundColor Cyan
    Write-Host "  1. config-service" -ForegroundColor White
    Write-Host "  2. customer-service" -ForegroundColor White
    Write-Host "  3. policy-service" -ForegroundColor White
    Write-Host "  4. data-pipeline-service" -ForegroundColor White
    Write-Host "  5. bff-service" -ForegroundColor White
    
    $service = Read-Host "`nEnter service number"
    
    $serviceMap = @{
        "1" = "config-service"
        "2" = "customer-service"
        "3" = "policy-service"
        "4" = "data-pipeline-service"
        "5" = "bff-service"
    }
    
    if ($serviceMap.ContainsKey($service)) {
        Write-Host "`n🔄 Restarting $($serviceMap[$service])..." -ForegroundColor Yellow
        docker compose restart $serviceMap[$service]
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n✅ Service restarted successfully!" -ForegroundColor Green
        } else {
            Write-Host "`n❌ Failed to restart service." -ForegroundColor Red
        }
    } else {
        Write-Host "`n❌ Invalid selection" -ForegroundColor Red
    }
}

# Function 7: Stop All
function Stop-AllServices {
    Write-Host "`n🛑 Stopping all services..." -ForegroundColor Yellow
    docker compose stop
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ All services stopped successfully!" -ForegroundColor Green
    } else {
        Write-Host "`n❌ Failed to stop services." -ForegroundColor Red
    }
}

# Function 8: Down (Keep Data)
function Remove-AllContainers {
    Write-Host "`n⚠️  This will stop and remove all containers but keep your data." -ForegroundColor Yellow
    $confirm = Read-Host "Continue? (y/N)"
    
    if ($confirm -eq "y" -or $confirm -eq "Y") {
        Write-Host "`n🗑️  Removing all containers..." -ForegroundColor Yellow
        docker compose down
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n✅ All containers removed successfully!" -ForegroundColor Green
        } else {
            Write-Host "`n❌ Failed to remove containers." -ForegroundColor Red
        }
    } else {
        Write-Host "`n❌ Operation cancelled." -ForegroundColor Gray
    }
}

# Function 9: Full Clean
function Remove-Everything {
    Write-Host "`n⚠️⚠️⚠️  WARNING ⚠️⚠️⚠️" -ForegroundColor Red
    Write-Host "This will remove ALL containers, volumes, and data!" -ForegroundColor Red
    Write-Host "Your databases will be completely wiped!" -ForegroundColor Red
    $confirm = Read-Host "`nAre you ABSOLUTELY sure? Type 'DELETE' to confirm"
    
    if ($confirm -eq "DELETE") {
        Write-Host "`n🗑️  Removing everything..." -ForegroundColor Red
        docker compose down -v
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n✅ Everything removed successfully!" -ForegroundColor Green
        } else {
            Write-Host "`n❌ Failed to remove everything." -ForegroundColor Red
        }
    } else {
        Write-Host "`n✅ Operation cancelled. Your data is safe." -ForegroundColor Green
    }
}

# Function 10: Health Check
function Test-AllServices {
    Write-Host "`n🏥 Checking service health..." -ForegroundColor Magenta
    Write-Host ""
    
    $services = @(
        @{Name="Config Service"; Port=8888},
        @{Name="Customer Service"; Port=8081},
        @{Name="Policy Service"; Port=8085},
        @{Name="Data Pipeline Service"; Port=8082},
        @{Name="BFF Service"; Port=8080}
    )
    
    $healthyCount = 0
    foreach ($service in $services) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)/actuator/health" -UseBasicParsing -TimeoutSec 5
            Write-Host "  ✅ $($service.Name) (Port $($service.Port)): HEALTHY" -ForegroundColor Green
            $healthyCount++
        } catch {
            Write-Host "  ❌ $($service.Name) (Port $($service.Port)): NOT RESPONDING" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "📊 Summary: $healthyCount/$($services.Count) services healthy" -ForegroundColor Cyan
}

# Function 11: Resource Usage
function Show-ResourceUsage {
    Write-Host "`n📦 Resource Usage:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Container Stats:" -ForegroundColor Yellow
    docker stats --no-stream
    
    Write-Host "`n`nDisk Usage:" -ForegroundColor Yellow
    docker system df
}

# Main Script
if (-not (Test-DockerRunning)) {
    Write-Host "❌ Docker is not running!" -ForegroundColor Red
    Write-Host "Please start Docker Desktop and try again." -ForegroundColor Yellow
    exit 1
}

if (-not (Test-DockerCompose)) {
    Write-Host "❌ Docker Compose is not available!" -ForegroundColor Red
    Write-Host "Please install Docker Compose and try again." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Docker is running" -ForegroundColor Green
Write-Host "✅ Docker Compose is available" -ForegroundColor Green
Write-Host ""

# Main loop
do {
    Show-Menu
    $choice = Read-Host "Select an option"
    
    switch ($choice) {
        "1" { Start-AllServices }
        "2" { Show-ServiceStatus }
        "3" { Show-AllLogs }
        "4" { Show-SpecificLogs }
        "5" { Restart-AllServices }
        "6" { Restart-SpecificService }
        "7" { Stop-AllServices }
        "8" { Remove-AllContainers }
        "9" { Remove-Everything }
        "10" { Test-AllServices }
        "11" { Show-ResourceUsage }
        "0" { 
            Write-Host "`n👋 Goodbye!" -ForegroundColor Cyan
            exit 0
        }
        default { 
            Write-Host "`n❌ Invalid option. Please try again." -ForegroundColor Red
        }
    }
    
    if ($choice -ne "0") {
        Write-Host "`n"
        Read-Host "Press Enter to continue"
        Clear-Host
        Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
        Write-Host "║     MyPolicy Backend - Docker Deployment Manager          ║" -ForegroundColor Cyan
        Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
        Write-Host ""
    }
} while ($true)
