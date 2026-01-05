# SYSTEM_TO_HYDROGEN Route Stress Test

Realistic stress testing suite for the `SYSTEM_TO_HYDROGEN` route that converts TSQuerySingleRequest XML to ISO 20022 PAC.028 messages.

## Prerequisites

1. **Runtime service must be running** on `http://localhost:8080`
2. **API key must be configured** in the database for `SYSTEM_TO_HYDROGEN` route
3. **PowerShell 5.1+** (Windows) or PowerShell Core (Cross-platform)

## Quick Start

### Step 1: Get Your API Key

If you haven't created an API key yet, run:

```sql
USE makura_runtime;

INSERT INTO api_keys (route_id, key_hash, valid_from, valid_until, active, created_at, updated_at)
VALUES (
    'SYSTEM_TO_HYDROGEN',
    SHA2('mak_test_key_12345', 256),  -- Replace with your desired key
    NOW(),
    DATE_ADD(NOW(), INTERVAL 1 YEAR),
    TRUE,
    NOW(),
    NOW()
);
```

Note your API key (e.g., `mak_test_key_12345`)

### Step 2: Run the Stress Test

```powershell
# Basic mixed scenario (most realistic)
.\stress-test-hydrogen.ps1 -ApiKey "mak_test_key_12345" -Scenario "mixed"

# Burst traffic
.\stress-test-hydrogen.ps1 -ApiKey "mak_test_key_12345" -Scenario "burst"

# Sustained load
.\stress-test-hydrogen.ps1 -ApiKey "mak_test_key_12345" -Scenario "sustained"

# All scenarios
.\stress-test-hydrogen.ps1 -ApiKey "mak_test_key_12345" -Scenario "all"
```

## Test Scenarios

### 1. Burst Traffic
- **Description**: Simulates sudden traffic spikes (e.g., batch processing, end-of-day queries)
- **Pattern**: 100 requests as fast as possible, 5 sec cooldown, repeat 5 times
- **Total Requests**: 500
- **Duration**: ~15-20 seconds

### 2. Sustained Load
- **Description**: Steady production traffic over time
- **Pattern**: 15 requests/second for 60 seconds
- **Total Requests**: 900
- **Duration**: ~60 seconds

### 3. Gradual Ramp-Up
- **Description**: Traffic increasing during business hours
- **Pattern**: Start at 2 req/s, ramp up to 30 req/s over 45 seconds
- **Total Requests**: ~720
- **Duration**: 45 seconds

### 4. Mixed Pattern (Recommended)
- **Description**: Most realistic - baseline + random spikes + quiet periods
- **Pattern**: 120 seconds with variable load (baseline 8 req/s, spikes to 35 req/s)
- **Total Requests**: ~960-1200 (variable)
- **Duration**: 120 seconds

### 5. Traffic Spike
- **Description**: Sudden viral event or system integration
- **Pattern**: Normal (10 req/s) → Spike (100 req/s) → Recovery (15 req/s)
- **Total Requests**: 1,750
- **Duration**: ~25 seconds

### 6. Extreme Load
- **Description**: Maximum capacity test
- **Pattern**: 50 requests/second for 30 seconds
- **Total Requests**: 1,500
- **Duration**: 30 seconds
- **Warning**: Generates significant load!

## Test Payload Format

The stress test automatically generates realistic `TSQuerySingleRequest` XML payloads:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<TSQuerySingleRequest>
    <SessionID>TSQ20241225123456-5678</SessionID>
    <SourceInstitutionCode>999058</SourceInstitutionCode>
    <RequestTimestamp>2024-12-25T12:34:56.789</RequestTimestamp>
</TSQuerySingleRequest>
```

Each request uses a unique `SessionID` to ensure realistic test conditions.

## Metrics Collected

The stress test automatically collects and reports:

### Performance Metrics
- **Total Requests**: Number of requests sent
- **Success Rate**: Percentage of successful responses
- **Throughput**: Requests per second
- **Response Time Statistics**:
  - Average
  - Min/Max
  - P50 (Median)
  - P95
  - P99

### Output Files

After each test run, detailed results are exported to CSV:

- `stress-test-results-SYSTEM_TO_HYDROGEN-YYYYMMDD_HHMMSS.csv`
  - Contains response times, success status, correlation IDs for all requests
  
- `stress-test-errors-SYSTEM_TO_HYDROGEN-YYYYMMDD_HHMMSS.csv` (if errors occur)
  - Contains error details for failed requests

## Example Output

```
═══════════════════════════════════════════════════════
        STRESS TEST RESULTS - SYSTEM_TO_HYDROGEN
═══════════════════════════════════════════════════════
Route:                  SYSTEM_TO_HYDROGEN
Total Requests:         900
Successful:             895
Failed:                 5
Success Rate:           99.44%
Duration:               60.23s
Throughput:             14.94 req/s

Response Time Statistics (ms):
  Average:              11.45
  Min:                  8
  Max:                  45
  P50 (Median):         11
  P95:                  18
  P99:                  32
═══════════════════════════════════════════════════════
```

## Integration with Monitoring

The stress test automatically queries system metrics after completion:

- **Service Health**: Checks `/actuator/health`
- **Total Requests**: Fetches from `/actuator/metrics/makura.translation.requests.total`
- **Average Duration**: Fetches from `/actuator/metrics/makura.translation.duration`

View detailed metrics:
- **Actuator**: http://localhost:8080/actuator
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Performance Expectations

Based on optimization work, expected performance:

- **Average Response Time**: 6-11ms (after JVM warm-up)
- **P95 Response Time**: < 20ms
- **P99 Response Time**: < 50ms
- **Success Rate**: > 99.9%

## Troubleshooting

### "Invalid or expired API key"
- Verify the API key exists in `makura_runtime.api_keys` table
- Ensure the key hash matches: `SHA2('your-key', 256)`
- Check that `valid_from <= NOW() <= valid_until` and `active = TRUE`

### High error rate
- Check service logs: Look for stack traces or errors
- Verify route is active: `SELECT * FROM routes WHERE route_id = 'SYSTEM_TO_HYDROGEN'`
- Check YAML mapping file exists: `runtime-service/mappings/SYSTEM_TO_HYDROGEN.yaml`

### Slow response times
- Ensure JVM has warmed up (run a few requests first)
- Check database connection pool settings
- Monitor system resources (CPU, memory, network)
- Verify caching is working (check cache hit rates)

### Connection errors
- Verify service is running: `curl http://localhost:8080/actuator/health`
- Check firewall/network settings
- Increase timeout if needed: Modify `-TimeoutSec 30` in script

## Advanced Usage

### Custom Parameters

```powershell
# Custom base URL
.\stress-test-hydrogen.ps1 -BaseUrl "http://production-server:8080/api/v1/translate" -ApiKey "..." -Scenario "mixed"

# Different API key
.\stress-test-hydrogen.ps1 -ApiKey "prod_api_key_xyz" -Scenario "sustained"

# Test different route (if you have one)
.\stress-test-hydrogen.ps1 -RouteId "SYSTEM_TO_HYDROGEN_PROD" -ApiKey "..." -Scenario "burst"
```

### Running Multiple Tests in Sequence

```powershell
# Warm-up
.\stress-test-hydrogen.ps1 -ApiKey "..." -Scenario "burst"

# Main test
Start-Sleep -Seconds 10
.\stress-test-hydrogen.ps1 -ApiKey "..." -Scenario "mixed"

# Stress test
Start-Sleep -Seconds 10
.\stress-test-hydrogen.ps1 -ApiKey "..." -Scenario "extreme"
```

## Best Practices

1. **Warm up the JVM**: Run a small burst test first before heavy load
2. **Monitor during test**: Watch CPU, memory, and database connections
3. **Run during off-peak**: Avoid testing on production during business hours
4. **Analyze results**: Review CSV files for patterns in errors or slow requests
5. **Compare baselines**: Run tests before/after optimizations to measure impact
6. **Document findings**: Record results for performance regression testing

## License

Internal Use Only - Proprietary


