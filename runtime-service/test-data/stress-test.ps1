# Real-Life Stress Test Scenarios for ISO 20022 Translation Service
# Simulates realistic production traffic patterns

param(
    [string]$BaseUrl = "http://localhost:8080/api/v1/translate",
    [string]$Scenario = "mixed"  # Options: burst, sustained, rampup, mixed, spike, all
)

# Color functions
function Write-Success { param($msg) Write-Host $msg -ForegroundColor Green }
function Write-Info { param($msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Warning { param($msg) Write-Host $msg -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host $msg -ForegroundColor Red }

# Test data templates
$paymentSmall = @{
    source = @{
        customer = @{ account = "1234567890"; name = "John Doe" }
        amount = "100.50"
        currency = "USD"
        reference = "SMALL-TXN"
    }
} | ConvertTo-Json -Depth 10 -Compress

$paymentMedium = @{
    source = @{
        customer = @{ account = "1234567890"; name = "John Doe" }
        amount = "5000.00"
        currency = "EUR"
        reference = "MEDIUM-TXN"
        description = "Invoice payment for services rendered during Q1 2024"
        creditor = @{ account = "9876543210"; name = "Jane Smith"; address = "123 Main St" }
    }
} | ConvertTo-Json -Depth 10 -Compress

$paymentLarge = @{
    source = @{
        customer = @{
            account = "1234567890"
            name = "Corporate Customer Ltd"
            address = "456 Business Park, Suite 100"
            taxId = "TAX123456"
        }
        amount = "50000.00"
        currency = "GBP"
        reference = "LARGE-BATCH-001"
        description = "Bulk payment processing for payroll - includes 150+ employees with various departments and cost centers"
        creditor = @{
            account = "9876543210"
            name = "Supplier Corporation"
            address = "789 Industrial Ave"
            bankCode = "SWIFT123"
        }
        metadata = @{
            department = "Finance"
            costCenter = "CC-1001"
            projectCode = "PROJ-2024-Q1"
            approvers = @("manager1@example.com", "manager2@example.com")
            attachments = @("invoice001.pdf", "receipt001.pdf")
        }
    }
} | ConvertTo-Json -Depth 10 -Compress

# Routes with weights (simulating real-world usage patterns)
$routes = @(
    @{ id = "CREDIT_TRANSFER_PACS008"; weight = 40; payload = $paymentMedium },
    @{ id = "PAYMENT_STATUS_PACS002"; weight = 25; payload = $paymentSmall },
    @{ id = "ACCOUNT_STATEMENT_CAMT053"; weight = 15; payload = $paymentMedium },
    @{ id = "BALANCE_REPORT_CAMT052"; weight = 10; payload = $paymentSmall },
    @{ id = "DIRECT_DEBIT_PAIN008"; weight = 7; payload = $paymentLarge },
    @{ id = "LEGACY_SOAP_BRIDGE"; weight = 3; payload = $paymentSmall }
)

function Get-WeightedRoute {
    $random = Get-Random -Minimum 1 -Maximum 100
    $cumulative = 0
    foreach ($route in $routes) {
        $cumulative += $route.weight
        if ($random -le $cumulative) {
            return $route
        }
    }
    return $routes[0]
}

function Invoke-TranslationRequest {
    param($RouteId, $Payload, $RequestId)
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/$RouteId" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{
                "X-API-Key" = "test-api-key-456"
                "X-Correlation-Id" = "stress-$RequestId"
            } `
            -Body $Payload `
            -TimeoutSec 30 `
            -ErrorAction Stop
        
        return @{ success = $true; route = $RouteId }
    } catch {
        return @{ success = $false; route = $RouteId; error = $_.Exception.Message }
    }
}

function Show-Results {
    param($Results, $StartTime, $EndTime)
    
    $duration = ($EndTime - $StartTime).TotalSeconds
    $total = $Results.Count
    $success = ($Results | Where-Object { $_.success }).Count
    $failed = $total - $success
    $successRate = if ($total -gt 0) { [math]::Round(($success / $total) * 100, 2) } else { 0 }
    
    Write-Host ""
    Write-Host "===========================================" -ForegroundColor White
    Write-Success "         STRESS TEST RESULTS"
    Write-Host "===========================================" -ForegroundColor White
    Write-Info "Total Requests:     $total"
    Write-Success "Successful:         $success"
    if ($failed -gt 0) { Write-Error "Failed:             $failed" } else { Write-Info "Failed:             $failed" }
    Write-Info "Success Rate:       $successRate%"
    Write-Info "Duration:           $([math]::Round($duration, 2))s"
    Write-Info "Throughput:         $([math]::Round($total / $duration, 2)) req/s"
    
    # Route breakdown
    Write-Host ""
    Write-Info "Route Distribution:"
    $routeGroups = $Results | Group-Object -Property route | Sort-Object Count -Descending
    foreach ($group in $routeGroups) {
        $routeSuccess = ($group.Group | Where-Object { $_.success }).Count
        $routeTotal = $group.Count
        Write-Host "  $($group.Name): $routeTotal requests ($routeSuccess success)" -ForegroundColor Yellow
    }
    
    Write-Host "═══════════════════════════════════════════" -ForegroundColor White
    Write-Host ""
}

# ========================================
# SCENARIO 1: BURST TRAFFIC
# ========================================
function Test-BurstTraffic {
    Write-Success "`n[SCENARIO 1] BURST TRAFFIC"
    Write-Info 'Simulates sudden traffic spike (e.g., month-end batch processing)'
    Write-Info "Pattern: 50 requests as fast as possible, then 5 sec cooldown, repeat 3 times"
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    
    for ($burst = 1; $burst -le 3; $burst++) {
        Write-Warning "Burst $burst/3 - Sending 50 requests..."
        
        for ($i = 1; $i -le 50; $i++) {
            $route = Get-WeightedRoute
            $result = Invoke-TranslationRequest -RouteId $route.id -Payload $route.payload -RequestId "$burst-$i"
            $results += $result
            
            if ($i % 10 -eq 0) {
                Write-Host "  Progress: $i/50" -ForegroundColor DarkGray
            }
        }
        
        if ($burst -lt 3) {
            Write-Info 'Cooldown period (5 seconds)...'
            Start-Sleep -Seconds 5
        }
    }
    
    $endTime = Get-Date
    Show-Results -Results $results -StartTime $startTime -EndTime $endTime
}

# ========================================
# SCENARIO 2: SUSTAINED LOAD
# ========================================
function Test-SustainedLoad {
    Write-Success "`n[SCENARIO 2] SUSTAINED LOAD"
    Write-Info "Simulates steady production traffic over time"
    Write-Info 'Pattern: 10 requests/second for 30 seconds (300 total)'
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    $targetDuration = 30
    $requestsPerSecond = 10
    $delayMs = 1000 / $requestsPerSecond
    
    $totalRequests = $targetDuration * $requestsPerSecond
    
    for ($i = 1; $i -le $totalRequests; $i++) {
        $route = Get-WeightedRoute
        $result = Invoke-TranslationRequest -RouteId $route.id -Payload $route.payload -RequestId $i
        $results += $result
        
        if ($i % 50 -eq 0) {
            $elapsed = ((Get-Date) - $startTime).TotalSeconds
            Write-Info "Progress: $i/$totalRequests | Elapsed: $([math]::Round($elapsed, 1))s"
        }
        
        Start-Sleep -Milliseconds $delayMs
    }
    
    $endTime = Get-Date
    Show-Results -Results $results -StartTime $startTime -EndTime $endTime
}

# ========================================
# SCENARIO 3: GRADUAL RAMP-UP
# ========================================
function Test-GradualRampUp {
    Write-Success "`n[SCENARIO 3] GRADUAL RAMP-UP"
    Write-Info "Simulates traffic increasing during business hours"
    Write-Info "Pattern: Start at 2 req/s, increase to 20 req/s over 30 seconds"
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    $duration = 30
    $minRate = 2
    $maxRate = 20
    
    for ($second = 1; $second -le $duration; $second++) {
        # Calculate current rate (linear ramp-up)
        $currentRate = $minRate + (($maxRate - $minRate) * ($second / $duration))
        $requestsThisSecond = [math]::Round($currentRate)
        $delayMs = 1000 / $requestsThisSecond
        
        for ($i = 1; $i -le $requestsThisSecond; $i++) {
            $route = Get-WeightedRoute
            $result = Invoke-TranslationRequest -RouteId $route.id -Payload $route.payload -RequestId "$second-$i"
            $results += $result
            
            Start-Sleep -Milliseconds $delayMs
        }
        
        if ($second % 5 -eq 0) {
            Write-Info "Second $second/$duration | Rate: $([math]::Round($currentRate, 1)) req/s | Total: $($results.Count)"
        }
    }
    
    $endTime = Get-Date
    Show-Results -Results $results -StartTime $startTime -EndTime $endTime
}

# ========================================
# SCENARIO 4: MIXED PATTERN (Realistic)
# ========================================
function Test-MixedPattern {
    Write-Success "`n[SCENARIO 4] MIXED PATTERN (Most Realistic)"
    Write-Info "Simulates real-world traffic: baseline + random spikes + quiet periods"
    Write-Info 'Pattern: 60 seconds with variable load (baseline 5 req/s, spikes to 25 req/s)'
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    $duration = 60
    $baselineRate = 5
    
    for ($second = 1; $second -le $duration; $second++) {
        # Random spikes (20% chance of spike)
        $spike = if ((Get-Random -Minimum 1 -Maximum 100) -le 20) { 
            Get-Random -Minimum 15 -Maximum 25 
        } else { 
            0 
        }
        
        # Quiet periods (10% chance of quiet)
        $quiet = if ((Get-Random -Minimum 1 -Maximum 100) -le 10) { 
            -3 
        } else { 
            0 
        }
        
        $currentRate = [math]::Max(1, $baselineRate + $spike + $quiet)
        $delayMs = 1000 / $currentRate
        
        for ($i = 1; $i -le $currentRate; $i++) {
            $route = Get-WeightedRoute
            $result = Invoke-TranslationRequest -RouteId $route.id -Payload $route.payload -RequestId "$second-$i"
            $results += $result
            
            Start-Sleep -Milliseconds $delayMs
        }
        
        if ($second % 10 -eq 0) {
            $status = if ($spike -gt 0) { "[SPIKE]" } elseif ($quiet -lt 0) { "[QUIET]" } else { "[NORMAL]" }
            Write-Info "Second $second/$duration | Rate: $currentRate req/s | Total: $($results.Count) $status"
        }
    }
    
    $endTime = Get-Date
    Show-Results -Results $results -StartTime $startTime -EndTime $endTime
}

# ========================================
# SCENARIO 5: TRAFFIC SPIKE
# ========================================
function Test-TrafficSpike {
    Write-Success "`n[SCENARIO 5] TRAFFIC SPIKE"
    Write-Info "Simulates sudden viral event or system integration"
    Write-Info "Pattern: Normal load → sudden 10x spike → gradual recovery"
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    
    # Phase 1: Normal (5 seconds @ 5 req/s)
    Write-Info 'Phase 1: Normal traffic (5 req/s)...'
    for ($i = 1; $i -le 25; $i++) {
        $route = Get-WeightedRoute
        $result = Invoke-TranslationRequest -RouteId $route.id -Payload $route.payload -RequestId "normal-$i"
        $results += $result
        Start-Sleep -Milliseconds 200
    }
    
    # Phase 2: SPIKE (10 seconds @ 50 req/s)
    Write-Warning 'Phase 2: TRAFFIC SPIKE! (50 req/s)...'
    for ($i = 1; $i -le 500; $i++) {
        $route = Get-WeightedRoute
        $result = Invoke-TranslationRequest -RouteId $route.id -Payload $route.payload -RequestId "spike-$i"
        $results += $result
        
        if ($i % 50 -eq 0) {
            Write-Host "  Spike progress: $i/500" -ForegroundColor Red
        }
        Start-Sleep -Milliseconds 20
    }
    
    # Phase 3: Recovery (5 seconds @ 10 req/s)
    Write-Info 'Phase 3: Recovery phase (10 req/s)...'
    for ($i = 1; $i -le 50; $i++) {
        $route = Get-WeightedRoute
        $result = Invoke-TranslationRequest -RouteId $route.id -Payload $route.payload -RequestId "recovery-$i"
        $results += $result
        Start-Sleep -Milliseconds 100
    }
    
    $endTime = Get-Date
    Show-Results -Results $results -StartTime $startTime -EndTime $endTime
}

# ========================================
# MAIN EXECUTION
# ========================================

Clear-Host
Write-Host ""
Write-Host "===========================================================" -ForegroundColor White
Write-Host "   ISO 20022 TRANSLATION SERVICE - STRESS TEST SUITE      " -ForegroundColor Cyan
Write-Host "===========================================================" -ForegroundColor White
Write-Host ""
Write-Info "Target: $BaseUrl"
Write-Info "Scenario: $Scenario"
Write-Host ""

switch ($Scenario.ToLower()) {
    "burst" { Test-BurstTraffic }
    "sustained" { Test-SustainedLoad }
    "rampup" { Test-GradualRampUp }
    "mixed" { Test-MixedPattern }
    "spike" { Test-TrafficSpike }
    "all" {
        Test-BurstTraffic
        Test-SustainedLoad
        Test-GradualRampUp
        Test-MixedPattern
        Test-TrafficSpike
        
        Write-Success "`n[SUCCESS] ALL SCENARIOS COMPLETED!"
    }
    default {
        Write-Error "Unknown scenario: $Scenario"
        Write-Info "Available scenarios: burst, sustained, rampup, mixed, spike, all"
        exit 1
    }
}

# Final metrics check
Write-Host ""
Write-Info "Fetching final system metrics..."
try {
    $metrics = Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/makura.translation.requests.total"
    Write-Success "[OK] Total Translation Requests (System): $($metrics.measurements[0].value)"
} catch {
    Write-Warning "Could not fetch system metrics"
}

Write-Host ""
Write-Info "View detailed metrics at:"
Write-Host "  - Grafana:    http://localhost:3000" -ForegroundColor Yellow
Write-Host "  - Prometheus: http://localhost:9090" -ForegroundColor Yellow
Write-Host "  - Dashboard:  http://localhost:5173/metrics" -ForegroundColor Yellow
Write-Host ""

