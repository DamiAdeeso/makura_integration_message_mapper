# PowerShell script to stop Prometheus and Grafana monitoring stack

Write-Host "Stopping Prometheus and Grafana monitoring stack..." -ForegroundColor Yellow

# Navigate to the directory containing docker-compose.yml
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptPath
Set-Location $projectRoot

# Stop the services
docker-compose -f docker-compose.yml down

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Monitoring stack stopped successfully!" -ForegroundColor Green
} else {
    Write-Host "Error: Failed to stop services" -ForegroundColor Red
    exit 1
}




