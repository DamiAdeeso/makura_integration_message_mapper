# PowerShell script to start Prometheus and Grafana monitoring stack

Write-Host "Starting Prometheus and Grafana monitoring stack..." -ForegroundColor Green

# Check if Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Cyan
try {
    docker ps 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker command failed"
    }
    Write-Host "Docker is running" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "ERROR: Docker Desktop is not running!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please:" -ForegroundColor Yellow
    Write-Host "  1. Start Docker Desktop from the Start menu" -ForegroundColor White
    Write-Host "  2. Wait for Docker to fully start (whale icon in system tray)" -ForegroundColor White
    Write-Host "  3. Run this script again" -ForegroundColor White
    Write-Host ""
    exit 1
}

# Navigate to the directory containing docker-compose.yml
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

# Start the services
Write-Host "Starting services with Docker Compose..." -ForegroundColor Yellow
# Try new docker compose command first, fallback to docker-compose
$composeCmd = "docker compose"
try {
    docker compose version 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        $composeCmd = "docker-compose"
    }
} catch {
    $composeCmd = "docker-compose"
}

Write-Host "Using: $composeCmd" -ForegroundColor Gray
& $composeCmd -f docker-compose.yml up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "[OK] Monitoring stack started successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Access the services:" -ForegroundColor Cyan
    Write-Host "  Grafana:    http://localhost:3000 (admin/admin)" -ForegroundColor White
    Write-Host "  Prometheus: http://localhost:9090" -ForegroundColor White
    Write-Host ""
    Write-Host "Make sure your runtime service is running on http://localhost:8080" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To stop the services, run: docker compose down" -ForegroundColor Gray
} else {
    Write-Host "Error: Failed to start services" -ForegroundColor Red
    exit 1
}


