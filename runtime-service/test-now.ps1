# Quick test script - run this now that the service is up
Write-Host "=== Testing Runtime Service ===" -ForegroundColor Green

Write-Host "`n1. Health Check:" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
    $health | ConvertTo-Json
    Write-Host "✓ Service is healthy!" -ForegroundColor Green
} catch {
    Write-Host "✗ Service not responding" -ForegroundColor Red
    exit
}

Write-Host "`n2. Testing Translation (SYSTEM_TO_NIP):" -ForegroundColor Yellow
$body = @{
    source = @{
        customer = @{
            account = "1234567890"
            name = "John Doe"
        }
        amount = "1000.50"
        currency = "USD"
        reference = "TXN-2024-001"
        creditor = @{
            account = "9876543210"
            name = "Jane Smith"
        }
    }
} | ConvertTo-Json -Depth 10

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/translate/SYSTEM_TO_NIP" `
        -Method Post `
        -ContentType "application/json" `
        -Headers @{
            "X-API-Key" = "test-api-key-123"
            "X-Correlation-Id" = "test-001"
        } `
        -Body $body
    
    Write-Host "✓ Translation successful!" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✗ Translation failed" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
}

Write-Host "`n3. Testing Metrics:" -ForegroundColor Yellow
try {
    $metrics = Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/makura.translation.requests.total"
    Write-Host "✓ Metrics available!" -ForegroundColor Green
    $metrics | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Metrics not available yet (normal if no requests processed)" -ForegroundColor Yellow
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green




