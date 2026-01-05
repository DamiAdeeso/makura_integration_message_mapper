# PowerShell script for load testing
# Runs 200 requests in quick succession to generate metrics data
# Make sure the service is running on localhost:8080

$baseUrl = "http://localhost:8080/api/v1/translate"
$iterations = 200

Write-Host "=== Starting Load Test: $iterations requests ===" -ForegroundColor Green
Write-Host "Target: $baseUrl" -ForegroundColor Cyan
Write-Host ""

# Test routes to cycle through
$routes = @("SYSTEM_TO_NIP_PASSIVE", "CREDIT_TRANSFER_PACS008", "PAYMENT_STATUS_PACS002", "ACCOUNT_STATEMENT_CAMT053")

# Test data
$body = @{
    source = @{
        customer = @{
            account = "1234567890"
            name = "John Doe"
        }
        amount = "1000.50"
        currency = "USD"
        reference = "TXN-2024-001"
    }
} | ConvertTo-Json -Depth 10

# Counters
$successCount = 0
$errorCount = 0
$startTime = Get-Date

# Run load test
for ($i = 1; $i -le $iterations; $i++) {
    # Rotate through routes
    $routeId = $routes[($i - 1) % $routes.Length]
    
    try {
        # Make request (suppress output with Out-Null)
        Invoke-RestMethod -Uri "$baseUrl/$routeId" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{
                "X-API-Key" = "test-api-key-456"
                "X-Correlation-Id" = "load-test-$i"
            } `
            -Body $body `
            -ErrorAction Stop | Out-Null
        
        $successCount++
        
        # Progress indicator (every 10 requests)
        if ($i % 10 -eq 0) {
            $percent = [math]::Round(($i / $iterations) * 100)
            Write-Host "Progress: $i/$iterations ($percent%) | Success: $successCount | Errors: $errorCount" -ForegroundColor Cyan
        }
        
    } catch {
        $errorCount++
        if ($errorCount -le 5) {
            Write-Host "Error on request $i : $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
    # Small delay to avoid overwhelming the server (optional - comment out for max speed)
    Start-Sleep -Milliseconds 10
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

Write-Host ""
Write-Host "=== Load Test Complete ===" -ForegroundColor Green
Write-Host "Total Requests: $iterations" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $errorCount" -ForegroundColor Red
Write-Host "Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan
Write-Host "Requests/sec: $([math]::Round($iterations / $duration, 2))" -ForegroundColor Cyan
Write-Host ""

Write-Host "=== Checking Final Metrics ===" -ForegroundColor Green
try {
    $metrics = Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/makura.translation.requests.total"
    Write-Host "Total Translation Requests: $($metrics.measurements[0].value)" -ForegroundColor Yellow
} catch {
    Write-Host "Could not fetch metrics: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "View metrics in:" -ForegroundColor White
Write-Host "  - Grafana: http://localhost:3000" -ForegroundColor Cyan
Write-Host "  - Prometheus: http://localhost:9090" -ForegroundColor Cyan
Write-Host "  - Dashboard: http://localhost:5173/metrics" -ForegroundColor Cyan

