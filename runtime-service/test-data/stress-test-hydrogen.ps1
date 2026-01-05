# Real-Life Stress Test for SYSTEM_TO_HYDROGEN Route
# Tests the actual TSQuerySingleRequest -> PAC.028 translation with realistic load patterns

param(
    [string]$BaseUrl = "http://localhost:8080/api/v1/translate",
    [string]$RouteId = "SYSTEM_TO_HYDROGEN",
    [string]$ApiKey = "mak_test1234567890abcdef",  # Update this with your actual API key
    [string]$Scenario = "mixed"  # Options: burst, sustained, rampup, mixed, spike, all, extreme
)

# Color functions
function Write-Success { param($msg) Write-Host $msg -ForegroundColor Green }
function Write-Info { param($msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Warning { param($msg) Write-Host $msg -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host $msg -ForegroundColor Red }

# Generate realistic TSQuerySingleRequest XML payload
function Get-TSQueryRequest {
    param(
        [string]$SessionID,
        [string]$SourceInstitutionCode = "999058"
    )
    
    # Create unique SessionID if not provided
    if ([string]::IsNullOrEmpty($SessionID)) {
        $SessionID = "TSQ" + (Get-Date -Format "yyyyMMddHHmmss") + "-" + (Get-Random -Minimum 1000 -Maximum 9999)
    }
    
    # Generate realistic XML payload
    $xmlPayload = @"
<?xml version="1.0" encoding="UTF-8"?>
<TSQuerySingleRequest>
    <SessionID>$SessionID</SessionID>
    <SourceInstitutionCode>$SourceInstitutionCode</SourceInstitutionCode>
    <RequestTimestamp>$(Get-Date -Format "yyyy-MM-ddTHH:mm:ss.fff")</RequestTimestamp>
</TSQuerySingleRequest>
"@
    
    return $xmlPayload
}

# Track response times for analysis
$responseTimes = @()
$errorDetails = @()

function Invoke-TranslationRequest {
    param(
        [string]$Payload, 
        [string]$RequestId,
        [int]$RequestNumber
    )
    
    $startTime = [System.Diagnostics.Stopwatch]::StartNew()
    $correlationId = "stress-$RouteId-$RequestId"
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/$RouteId" `
            -Method Post `
            -ContentType "application/xml" `
            -Headers @{
                "X-API-Key" = $ApiKey
                "X-Correlation-Id" = $correlationId
            } `
            -Body $Payload `
            -TimeoutSec 30 `
            -ErrorAction Stop
        
        $startTime.Stop()
        $responseTime = $startTime.ElapsedMilliseconds
        
        $script:responseTimes += [PSCustomObject]@{
            RequestNumber = $RequestNumber
            CorrelationId = $correlationId
            ResponseTimeMs = $responseTime
            Success = $true
            Timestamp = Get-Date
        }
        
        return @{ 
            success = $true
            responseTimeMs = $responseTime
            correlationId = $correlationId
        }
    } catch {
        $startTime.Stop()
        $responseTime = $startTime.ElapsedMilliseconds
        
        $script:errorDetails += [PSCustomObject]@{
            RequestNumber = $RequestNumber
            CorrelationId = $correlationId
            Error = $_.Exception.Message
            ResponseTimeMs = $responseTime
            Timestamp = Get-Date
        }
        
        $script:responseTimes += [PSCustomObject]@{
            RequestNumber = $RequestNumber
            CorrelationId = $correlationId
            ResponseTimeMs = $responseTime
            Success = $false
            Error = $_.Exception.Message
            Timestamp = Get-Date
        }
        
        return @{ 
            success = $false
            error = $_.Exception.Message
            responseTimeMs = $responseTime
            correlationId = $correlationId
        }
    }
}

function Show-Results {
    param($Results, $StartTime, $EndTime)
    
    $duration = ($EndTime - $StartTime).TotalSeconds
    $total = $Results.Count
    $success = ($Results | Where-Object { $_.success }).Count
    $failed = $total - $success
    $successRate = if ($total -gt 0) { [math]::Round(($success / $total) * 100, 2) } else { 0 }
    
    # Calculate response time statistics
    $successfulRequests = $script:responseTimes | Where-Object { $_.Success -eq $true }
    if ($successfulRequests.Count -gt 0) {
        $avgResponseTime = ($successfulRequests | Measure-Object -Property ResponseTimeMs -Average).Average
        $minResponseTime = ($successfulRequests | Measure-Object -Property ResponseTimeMs -Minimum).Minimum
        $maxResponseTime = ($successfulRequests | Measure-Object -Property ResponseTimeMs -Maximum).Maximum
        $p50 = ($successfulRequests | Sort-Object ResponseTimeMs)[[math]::Floor($successfulRequests.Count * 0.50)].ResponseTimeMs
        $p95 = ($successfulRequests | Sort-Object ResponseTimeMs)[[math]::Floor($successfulRequests.Count * 0.95)].ResponseTimeMs
        $p99 = ($successfulRequests | Sort-Object ResponseTimeMs)[[math]::Floor($successfulRequests.Count * 0.99)].ResponseTimeMs
    }
    
    Write-Host ""
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor White
    Write-Success "        STRESS TEST RESULTS - SYSTEM_TO_HYDROGEN"
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor White
    Write-Info "Route:                  $RouteId"
    Write-Info "Total Requests:         $total"
    Write-Success "Successful:             $success"
    if ($failed -gt 0) { 
        Write-Error "Failed:                 $failed" 
    } else { 
        Write-Info "Failed:                 $failed" 
    }
    Write-Info "Success Rate:           $successRate%"
    Write-Info "Duration:               $([math]::Round($duration, 2))s"
    Write-Info "Throughput:             $([math]::Round($total / $duration, 2)) req/s"
    
    if ($successfulRequests.Count -gt 0) {
        Write-Host ""
        Write-Info "Response Time Statistics (ms):"
        Write-Host "  Average:              $([math]::Round($avgResponseTime, 2))" -ForegroundColor Yellow
        Write-Host "  Min:                  $minResponseTime" -ForegroundColor Green
        Write-Host "  Max:                  $maxResponseTime" -ForegroundColor $(if ($maxResponseTime -gt 100) { "Red" } else { "Yellow" })
        Write-Host "  P50 (Median):         $p50" -ForegroundColor Cyan
        Write-Host "  P95:                  $p95" -ForegroundColor $(if ($p95 -gt 50) { "Yellow" } else { "Green" })
        Write-Host "  P99:                  $p99" -ForegroundColor $(if ($p99 -gt 100) { "Red" } else { "Yellow" })
    }
    
    if ($failed -gt 0) {
        Write-Host ""
        Write-Error "Error Summary:"
        $errorGroups = $errorDetails | Group-Object -Property Error | Sort-Object Count -Descending
        foreach ($group in $errorGroups) {
            Write-Host "  $($group.Count)x - $($group.Name)" -ForegroundColor Red
        }
    }
    
    Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor White
    Write-Host ""
}

# ========================================
# SCENARIO 1: BURST TRAFFIC
# ========================================
function Test-BurstTraffic {
    Write-Success "`n[SCENARIO 1] BURST TRAFFIC"
    Write-Info 'Simulates sudden traffic spike (e.g., batch processing, end-of-day queries)'
    Write-Info "Pattern: 100 requests as fast as possible, then 5 sec cooldown, repeat 5 times"
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    
    for ($burst = 1; $burst -le 5; $burst++) {
        Write-Warning "Burst $burst/5 - Sending 100 requests..."
        
        for ($i = 1; $i -le 100; $i++) {
            $sessionId = "BURST$burst-" + (Get-Date -Format "HHmmss") + "-$i"
            $payload = Get-TSQueryRequest -SessionID $sessionId
            $requestNum = (($burst - 1) * 100) + $i
            $result = Invoke-TranslationRequest -Payload $payload -RequestId "$burst-$i" -RequestNumber $requestNum
            $results += $result
            
            if ($i % 25 -eq 0) {
                Write-Host "  Progress: $i/100" -ForegroundColor DarkGray
            }
        }
        
        if ($burst -lt 5) {
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
    Write-Info 'Pattern: 15 requests/second for 60 seconds (900 total requests)'
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    $targetDuration = 60
    $requestsPerSecond = 15
    $delayMs = [math]::Round(1000 / $requestsPerSecond)
    
    $totalRequests = $targetDuration * $requestsPerSecond
    
    for ($i = 1; $i -le $totalRequests; $i++) {
        $sessionId = "SUSTAINED-" + (Get-Date -Format "HHmmssfff") + "-$i"
        $payload = Get-TSQueryRequest -SessionID $sessionId
        $result = Invoke-TranslationRequest -Payload $payload -RequestId $i -RequestNumber $i
        $results += $result
        
        if ($i % 100 -eq 0) {
            $elapsed = ((Get-Date) - $startTime).TotalSeconds
            $successSoFar = ($results | Where-Object { $_.success }).Count
            Write-Info "Progress: $i/$totalRequests | Elapsed: $([math]::Round($elapsed, 1))s | Success: $successSoFar"
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
    Write-Info "Pattern: Start at 2 req/s, increase to 30 req/s over 45 seconds"
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    $duration = 45
    $minRate = 2
    $maxRate = 30
    $requestNum = 0
    
    for ($second = 1; $second -le $duration; $second++) {
        # Calculate current rate (linear ramp-up)
        $currentRate = $minRate + (($maxRate - $minRate) * ($second / $duration))
        $requestsThisSecond = [math]::Round($currentRate)
        $delayMs = [math]::Round(1000 / $requestsThisSecond)
        
        for ($i = 1; $i -le $requestsThisSecond; $i++) {
            $requestNum++
            $sessionId = "RAMPUP-$second-$i-" + (Get-Date -Format "HHmmssfff")
            $payload = Get-TSQueryRequest -SessionID $sessionId
            $result = Invoke-TranslationRequest -Payload $payload -RequestId "$second-$i" -RequestNumber $requestNum
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
    Write-Info 'Pattern: 120 seconds with variable load (baseline 8 req/s, spikes to 35 req/s)'
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    $duration = 120
    $baselineRate = 8
    $requestNum = 0
    
    for ($second = 1; $second -le $duration; $second++) {
        # Random spikes (25% chance of spike)
        $spike = if ((Get-Random -Minimum 1 -Maximum 100) -le 25) { 
            Get-Random -Minimum 20 -Maximum 35 
        } else { 
            0 
        }
        
        # Quiet periods (15% chance of quiet)
        $quiet = if ((Get-Random -Minimum 1 -Maximum 100) -le 15) { 
            -4 
        } else { 
            0 
        }
        
        $currentRate = [math]::Max(1, $baselineRate + $spike + $quiet)
        $delayMs = [math]::Round(1000 / $currentRate)
        
        for ($i = 1; $i -le $currentRate; $i++) {
            $requestNum++
            $sessionId = "MIXED-$second-$i-" + (Get-Date -Format "HHmmssfff")
            $payload = Get-TSQueryRequest -SessionID $sessionId
            $result = Invoke-TranslationRequest -Payload $payload -RequestId "$second-$i" -RequestNumber $requestNum
            $results += $result
            
            Start-Sleep -Milliseconds $delayMs
        }
        
        if ($second % 15 -eq 0) {
            $status = if ($spike -gt 0) { "[SPIKE]" } elseif ($quiet -lt 0) { "[QUIET]" } else { "[NORMAL]" }
            $successSoFar = ($results | Where-Object { $_.success }).Count
            Write-Info "Second $second/$duration | Rate: $currentRate req/s | Total: $($results.Count) | Success: $successSoFar $status"
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
    Write-Info "Pattern: Normal load → sudden 15x spike → gradual recovery"
    Write-Host ""
    
    $results = @()
    $startTime = Get-Date
    $requestNum = 0
    
    # Phase 1: Normal (10 seconds @ 10 req/s)
    Write-Info 'Phase 1: Normal traffic (10 req/s)...'
    for ($i = 1; $i -le 100; $i++) {
        $requestNum++
        $sessionId = "NORMAL-$i-" + (Get-Date -Format "HHmmssfff")
        $payload = Get-TSQueryRequest -SessionID $sessionId
        $result = Invoke-TranslationRequest -Payload $payload -RequestId "normal-$i" -RequestNumber $requestNum
        $results += $result
        Start-Sleep -Milliseconds 100
    }
    
    # Phase 2: SPIKE (15 seconds @ 100 req/s)
    Write-Warning 'Phase 2: TRAFFIC SPIKE! (100 req/s)...'
    for ($i = 1; $i -le 1500; $i++) {
        $requestNum++
        $sessionId = "SPIKE-$i-" + (Get-Date -Format "HHmmssfff")
        $payload = Get-TSQueryRequest -SessionID $sessionId
        $result = Invoke-TranslationRequest -Payload $payload -RequestId "spike-$i" -RequestNumber $requestNum
        $results += $result
        
        if ($i % 150 -eq 0) {
            Write-Host "  Spike progress: $i/1500" -ForegroundColor Red
        }
        Start-Sleep -Milliseconds 10
    }
    
    # Phase 3: Recovery (10 seconds @ 15 req/s)
    Write-Info 'Phase 3: Recovery phase (15 req/s)...'
    for ($i = 1; $i -le 150; $i++) {
        $requestNum++
        $sessionId = "RECOVERY-$i-" + (Get-Date -Format "HHmmssfff")
        $payload = Get-TSQueryRequest -SessionID $sessionId
        $result = Invoke-TranslationRequest -Payload $payload -RequestId "recovery-$i" -RequestNumber $requestNum
        $results += $result
        Start-Sleep -Milliseconds 66
    }
    
    $endTime = Get-Date
    Show-Results -Results $results -StartTime $startTime -EndTime $endTime
}

# ========================================
# SCENARIO 6: EXTREME LOAD
# ========================================
function Test-ExtremeLoad {
    Write-Success "`n[SCENARIO 6] EXTREME LOAD TEST"
    Write-Info "Pushes system to maximum capacity"
    Write-Info "Pattern: 50 req/s for 30 seconds (1500 requests total)"
    Write-Warning "WARNING: This will generate significant load!"
    Write-Host ""
    
    $continue = Read-Host "Continue with extreme load test? (yes/no)"
    if ($continue -ne "yes") {
        Write-Info "Extreme load test cancelled."
        return
    }
    
    $results = @()
    $startTime = Get-Date
    $targetDuration = 30
    $requestsPerSecond = 50
    $delayMs = [math]::Round(1000 / $requestsPerSecond)
    
    $totalRequests = $targetDuration * $requestsPerSecond
    
    Write-Warning "Starting extreme load: $totalRequests requests at $requestsPerSecond req/s"
    
    for ($i = 1; $i -le $totalRequests; $i++) {
        $sessionId = "EXTREME-$i-" + (Get-Date -Format "HHmmssfff")
        $payload = Get-TSQueryRequest -SessionID $sessionId
        $result = Invoke-TranslationRequest -Payload $payload -RequestId $i -RequestNumber $i
        $results += $result
        
        if ($i % 250 -eq 0) {
            $elapsed = ((Get-Date) - $startTime).TotalSeconds
            $successSoFar = ($results | Where-Object { $_.success }).Count
            $currentRate = [math]::Round($i / $elapsed, 2)
            Write-Info "Progress: $i/$totalRequests | Elapsed: $([math]::Round($elapsed, 1))s | Rate: $currentRate req/s | Success: $successSoFar"
        }
        
        Start-Sleep -Milliseconds $delayMs
    }
    
    $endTime = Get-Date
    Show-Results -Results $results -StartTime $startTime -EndTime $endTime
}

# ========================================
# MAIN EXECUTION
# ========================================

Clear-Host
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor White
Write-Host "  SYSTEM_TO_HYDROGEN ROUTE - STRESS TEST SUITE" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor White
Write-Host ""
Write-Info "Target:       $BaseUrl"
Write-Info "Route:        $RouteId"
Write-Info "API Key:      $($ApiKey.Substring(0, [math]::Min(10, $ApiKey.Length)))..."
Write-Info "Scenario:     $Scenario"
Write-Host ""

# Verify API key is set
if ([string]::IsNullOrEmpty($ApiKey) -or $ApiKey -eq "YOUR_API_KEY_HERE") {
    Write-Error "ERROR: Please set the ApiKey parameter!"
    Write-Info "Example: .\stress-test-hydrogen.ps1 -ApiKey 'mak_test_key_12345'"
    exit 1
}

switch ($Scenario.ToLower()) {
    "burst" { Test-BurstTraffic }
    "sustained" { Test-SustainedLoad }
    "rampup" { Test-GradualRampUp }
    "mixed" { Test-MixedPattern }
    "spike" { Test-TrafficSpike }
    "extreme" { Test-ExtremeLoad }
    "all" {
        Test-BurstTraffic
        Start-Sleep -Seconds 3
        Test-SustainedLoad
        Start-Sleep -Seconds 3
        Test-GradualRampUp
        Start-Sleep -Seconds 3
        Test-MixedPattern
        Start-Sleep -Seconds 3
        Test-TrafficSpike
        
        Write-Success "`n[SUCCESS] ALL SCENARIOS COMPLETED!"
    }
    default {
        Write-Error "Unknown scenario: $Scenario"
        Write-Info "Available scenarios: burst, sustained, rampup, mixed, spike, extreme, all"
        exit 1
    }
}

# Final metrics check
Write-Host ""
Write-Info "Fetching final system metrics..."
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -ErrorAction SilentlyContinue
    Write-Success "[OK] Service Health: $($health.status)"
    
    $metrics = Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/makura.translation.requests.total" -ErrorAction SilentlyContinue
    Write-Success "[OK] Total Translation Requests (System): $($metrics.measurements[0].value)"
    
    # Timer metrics expose multiple statistics - calculate average from TOTAL_TIME / COUNT
    try {
        $durationMetric = Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/makura.translation.duration" -ErrorAction SilentlyContinue
        if ($durationMetric -and $durationMetric.measurements.Count -gt 0) {
            # Find COUNT and TOTAL_TIME measurements
            $count = ($durationMetric.measurements | Where-Object { $_.statistic -eq "COUNT" }).value
            $totalTime = ($durationMetric.measurements | Where-Object { $_.statistic -eq "TOTAL_TIME" }).value
            $max = ($durationMetric.measurements | Where-Object { $_.statistic -eq "MAX" }).value
            
            if ($count -gt 0 -and $totalTime) {
                # Timer metrics are in seconds, convert to milliseconds
                $avgDurationMs = ($totalTime / $count) * 1000
                $maxDurationMs = $max * 1000
                Write-Success "[OK] Average Duration: $([math]::Round($avgDurationMs, 2))ms"
                Write-Success "[OK] Max Duration: $([math]::Round($maxDurationMs, 2))ms"
            }
        }
    } catch {
        Write-Warning "Could not fetch duration metrics: $_"
    }
} catch {
    Write-Warning "Could not fetch system metrics: $_"
}

Write-Host ""
Write-Info "View detailed metrics at:"
Write-Host "  - Actuator:    http://localhost:8080/actuator" -ForegroundColor Yellow
Write-Host "  - Prometheus:  http://localhost:8080/actuator/prometheus" -ForegroundColor Yellow
Write-Host "  - Swagger:     http://localhost:8080/swagger-ui.html" -ForegroundColor Yellow
Write-Host ""

# Export detailed results to CSV
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$csvFile = "stress-test-results-$RouteId-$timestamp.csv"
$responseTimes | Export-Csv -Path $csvFile -NoTypeInformation
Write-Info "Detailed response times exported to: $csvFile"

if ($errorDetails.Count -gt 0) {
    $errorFile = "stress-test-errors-$RouteId-$timestamp.csv"
    $errorDetails | Export-Csv -Path $errorFile -NoTypeInformation
    Write-Warning "Error details exported to: $errorFile"
}

Write-Host ""

