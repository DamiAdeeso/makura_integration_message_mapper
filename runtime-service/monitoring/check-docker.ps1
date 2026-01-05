# Docker Desktop Status Checker
# Helps diagnose Docker connection issues

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Docker Desktop Status Checker" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check 1: Docker command availability
Write-Host "[1/4] Checking if Docker command is available..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Docker command found: $dockerVersion" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Docker command not found!" -ForegroundColor Red
        Write-Host "  Please install Docker Desktop from: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "  [ERROR] Docker command not found!" -ForegroundColor Red
    Write-Host "  Please install Docker Desktop from: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Check 2: Docker daemon connection
Write-Host ""
Write-Host "[2/4] Checking Docker daemon connection..." -ForegroundColor Yellow
try {
    $null = docker ps 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Docker daemon is running!" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Cannot connect to Docker daemon!" -ForegroundColor Red
        Write-Host ""
        Write-Host "  Possible causes:" -ForegroundColor Yellow
        Write-Host "    1. Docker Desktop is not running" -ForegroundColor White
        Write-Host "    2. Docker Desktop is still starting up" -ForegroundColor White
        Write-Host "    3. Docker Desktop service is not running" -ForegroundColor White
        Write-Host ""
        Write-Host "  Solutions:" -ForegroundColor Yellow
        Write-Host "    1. Open Docker Desktop from Start Menu" -ForegroundColor White
        Write-Host "    2. Wait for the whale icon to appear in system tray" -ForegroundColor White
        Write-Host "    3. Check system tray for Docker Desktop icon" -ForegroundColor White
        Write-Host "    4. Try restarting Docker Desktop" -ForegroundColor White
        exit 1
    }
} catch {
    Write-Host "  [ERROR] Cannot connect to Docker daemon!" -ForegroundColor Red
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}

# Check 3: Docker Compose availability
Write-Host ""
Write-Host "[3/4] Checking Docker Compose..." -ForegroundColor Yellow
try {
    # Try new docker compose command
    $composeVersion = docker compose version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Docker Compose found: $composeVersion" -ForegroundColor Green
        $composeCmd = "docker compose"
    } else {
        # Fallback to docker-compose
        $composeVersion = docker-compose --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  [OK] Docker Compose found (legacy): $composeVersion" -ForegroundColor Green
            $composeCmd = "docker-compose"
        } else {
            Write-Host "  [WARNING] Docker Compose not found, but Docker is running" -ForegroundColor Yellow
            $composeCmd = "docker compose"
        }
    }
} catch {
    Write-Host "  [WARNING] Could not check Docker Compose" -ForegroundColor Yellow
    $composeCmd = "docker compose"
}

# Check 4: Running containers
Write-Host ""
Write-Host "[4/4] Checking running containers..." -ForegroundColor Yellow
try {
    $containers = docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>&1
    if ($LASTEXITCODE -eq 0) {
        if ($containers -match "NAMES") {
            Write-Host "  [OK] Docker is working! Running containers:" -ForegroundColor Green
            Write-Host $containers -ForegroundColor Gray
        } else {
            Write-Host "  [OK] Docker is working! (No containers running)" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "  [WARNING] Could not list containers" -ForegroundColor Yellow
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($LASTEXITCODE -eq 0 -or $null -eq $LASTEXITCODE) {
    Write-Host "[SUCCESS] Docker Desktop is ready!" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can now run:" -ForegroundColor Yellow
    Write-Host "  docker compose up -d" -ForegroundColor White
    Write-Host ""
    Write-Host "Or use the start script:" -ForegroundColor Yellow
    Write-Host "  .\monitoring\start-monitoring.ps1" -ForegroundColor White
} else {
    Write-Host "[ERROR] Docker Desktop is not ready!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please:" -ForegroundColor Yellow
    Write-Host "  1. Start Docker Desktop from Start Menu" -ForegroundColor White
    Write-Host "  2. Wait for it to fully start (whale icon in system tray)" -ForegroundColor White
    Write-Host "  3. Run this check again: .\monitoring\check-docker.ps1" -ForegroundColor White
}

Write-Host ""



