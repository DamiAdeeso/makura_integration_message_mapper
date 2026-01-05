# Stress Test Guide for ISO 20022 Translation Service

## 📋 Overview

This directory contains comprehensive stress testing tools for the Makura ISO 20022 Translation Service. The tests simulate real-world production scenarios to validate performance, scalability, and reliability.

---

## 🚀 Quick Start

### Prerequisites
- Runtime service running on `http://localhost:8080`
- PowerShell (Windows) or PowerShell Core (cross-platform)
- At least 2GB available RAM

### Run All Scenarios
```powershell
cd runtime-service/test-data
.\stress-test.ps1 -Scenario all
```

### Run Specific Scenario
```powershell
.\stress-test.ps1 -Scenario mixed      # Realistic mixed traffic
.\stress-test.ps1 -Scenario burst      # Burst traffic spikes
.\stress-test.ps1 -Scenario sustained  # Steady load
.\stress-test.ps1 -Scenario rampup     # Gradual increase
.\stress-test.ps1 -Scenario spike      # Sudden traffic spike
```

### Simple Load Test (200 requests)
```powershell
.\quick-test.ps1
```

---

## 📊 Test Scenarios Explained

### 1️⃣ **Burst Traffic** 🔥
**Real-world equivalent**: Month-end batch processing, payroll runs

**Pattern**:
- 3 bursts of 50 requests
- Sent as fast as possible
- 5-second cooldown between bursts

**Use case**: Tests how the service handles sudden spikes from batch jobs.

**Expected**: 
- ✅ All requests complete successfully
- ✅ No connection timeouts
- ⚠️ Response times may increase during burst

```powershell
.\stress-test.ps1 -Scenario burst
```

---

### 2️⃣ **Sustained Load** ⏱️
**Real-world equivalent**: Normal business hours traffic

**Pattern**:
- 10 requests per second
- Maintained for 30 seconds
- Total: 300 requests

**Use case**: Tests service stability under continuous load.

**Expected**:
- ✅ Consistent response times
- ✅ No memory leaks
- ✅ 100% success rate

```powershell
.\stress-test.ps1 -Scenario sustained
```

---

### 3️⃣ **Gradual Ramp-Up** 📈
**Real-world equivalent**: Morning office hours (9 AM - 10 AM)

**Pattern**:
- Starts at 2 requests/second
- Increases linearly to 20 requests/second
- Over 30 seconds

**Use case**: Tests how service scales as load increases.

**Expected**:
- ✅ Graceful performance degradation (if any)
- ✅ Auto-scaling triggers (if configured)
- ✅ No sudden failures

```powershell
.\stress-test.ps1 -Scenario rampup
```

---

### 4️⃣ **Mixed Pattern** 🌐 ⭐ *Most Realistic*
**Real-world equivalent**: Actual production traffic with unpredictable patterns

**Pattern**:
- Baseline: 5 requests/second
- Random spikes: 25 requests/second (20% probability)
- Quiet periods: 2 requests/second (10% probability)
- Duration: 60 seconds

**Use case**: Closest simulation to real production environment.

**Expected**:
- ✅ Handles variability smoothly
- ✅ Recovers quickly from spikes
- ✅ Metrics show realistic distribution

```powershell
.\stress-test.ps1 -Scenario mixed
```

---

### 5️⃣ **Traffic Spike** ⚡
**Real-world equivalent**: Viral event, marketing campaign launch, system integration go-live

**Pattern**:
1. **Normal Phase**: 5 seconds @ 5 req/s (25 requests)
2. **SPIKE Phase**: 10 seconds @ 50 req/s (500 requests) 🔥
3. **Recovery Phase**: 5 seconds @ 10 req/s (50 requests)

**Total**: 575 requests in 20 seconds

**Use case**: Tests system resilience during unexpected traffic surge.

**Expected**:
- ⚠️ Some request queueing during spike
- ✅ No service crashes
- ✅ Clean recovery after spike

```powershell
.\stress-test.ps1 -Scenario spike
```

---

## 🎯 Test Data Characteristics

### Payload Sizes (Simulates Real Transactions)

| Size | Description | Example Use Case | Payload Size |
|------|-------------|------------------|--------------|
| **Small** | Simple payment | Retail transactions | ~200 bytes |
| **Medium** | Standard business payment | B2B invoices | ~500 bytes |
| **Large** | Complex batch payment | Payroll, bulk transfers | ~1.5 KB |

### Route Distribution (Production-Weighted)

Routes are weighted based on typical production usage:

| Route | Weight | Description |
|-------|--------|-------------|
| `CREDIT_TRANSFER_PACS008` | 40% | Most common - customer credit transfers |
| `PAYMENT_STATUS_PACS002` | 25% | Status reports |
| `ACCOUNT_STATEMENT_CAMT053` | 15% | Account statements |
| `BALANCE_REPORT_CAMT052` | 10% | Balance inquiries |
| `DIRECT_DEBIT_PAIN008` | 7% | Direct debits |
| `LEGACY_SOAP_BRIDGE` | 3% | Legacy system integration |

---

## 📈 Monitoring During Tests

### Real-Time Monitoring

**Option 1: Grafana Dashboard** (Recommended)
```
http://localhost:3000
```
- Pre-built dashboards
- Visual charts and graphs
- Alerts (if configured)

**Option 2: Prometheus**
```
http://localhost:9090
```
- Raw metrics
- Query language (PromQL)
- Custom graphs

**Option 3: Dashboard UI**
```
http://localhost:5173/metrics
```
- Web UI metrics page
- Route-specific stats
- Health status

### Key Metrics to Watch

1. **Request Rate** (`makura_translation_requests_total`)
   - Should match test scenario pattern
   
2. **Success Rate** (`makura_translation_success_total / makura_translation_requests_total`)
   - Target: >99% under normal load
   - Acceptable: >95% under spike conditions
   
3. **Response Time** (`makura_translation_duration_seconds`)
   - P50: <100ms
   - P95: <500ms
   - P99: <1000ms
   
4. **Error Rate** (`makura_translation_errors_total`)
   - Target: <1% of total requests
   
5. **JVM Memory** (Heap usage)
   - Should stabilize, not continuously grow
   
6. **Thread Count**
   - Should stay within configured pool limits

---

## 🔧 Customization

### Change Target URL
```powershell
.\stress-test.ps1 -BaseUrl "http://production-server:8080/api/v1/translate" -Scenario mixed
```

### Adjust Test Parameters

Edit `stress-test.ps1` to modify:

```powershell
# Burst Traffic
$burstsCount = 3        # Number of bursts
$requestsPerBurst = 50  # Requests per burst
$cooldownSeconds = 5    # Cooldown between bursts

# Sustained Load
$requestsPerSecond = 10
$durationSeconds = 30

# Mixed Pattern
$baselineRate = 5       # Normal requests/sec
$spikeProbability = 20  # % chance of spike
$spikeRate = 25         # Requests/sec during spike
```

---

## 🎓 Interpreting Results

### Successful Test Run

```
═══════════════════════════════════════════
         STRESS TEST RESULTS
═══════════════════════════════════════════
Total Requests:     300
Successful:         300
Failed:             0
Success Rate:       100%
Duration:           30.45s
Throughput:         9.85 req/s
```

✅ **Good indicators**:
- 100% success rate
- Throughput matches expected rate
- No errors or timeouts

### Warning Signs

⚠️ **Watch for**:
- Success rate drops below 95%
- Increasing response times over duration
- Connection timeout errors
- Out of memory errors in service logs

### Failed Test Run

```
Total Requests:     300
Successful:         250
Failed:             50
Success Rate:       83.33%  ❌
```

❌ **Investigate**:
1. Check service logs for exceptions
2. Review database connection pool settings
3. Check JVM heap size
4. Verify network connectivity
5. Review thread pool configuration

---

## 🐛 Troubleshooting

### Service is slow/timing out

**Possible causes**:
- Insufficient CPU/RAM
- Database connection pool exhausted
- Thread pool too small
- Network latency

**Solutions**:
```yaml
# application.yml adjustments
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase from default 10
      
server:
  tomcat:
    threads:
      max: 200               # Increase from default 200
      min-spare: 10
```

### Memory issues

**Check JVM settings**:
```bash
java -Xmx2g -Xms512m -jar runtime-service.jar
```

### Connection refused errors

**Check**:
1. Service is running: `curl http://localhost:8080/actuator/health`
2. Port is correct
3. Firewall allows connections

---

## 📋 Best Practices

### Before Testing
1. ✅ Start with small load tests
2. ✅ Monitor baseline performance first
3. ✅ Have monitoring dashboards ready
4. ✅ Note current system metrics (CPU, RAM, disk)

### During Testing
1. 👁️ Watch for errors in service logs
2. 👁️ Monitor system resources (CPU, memory)
3. 👁️ Check database connections
4. 👁️ Observe response time trends

### After Testing
1. 📊 Review Grafana/Prometheus metrics
2. 📝 Document any performance issues
3. 🔍 Analyze error logs if failures occurred
4. ♻️ Allow system to cool down before next test

---

## 🎯 Production Readiness Checklist

Use these tests to validate production readiness:

- [ ] **Burst Traffic**: ≥95% success rate
- [ ] **Sustained Load**: 100% success for 30 seconds
- [ ] **Ramp-Up**: No failures during scaling
- [ ] **Mixed Pattern**: ≥98% success rate over 60 seconds
- [ ] **Traffic Spike**: Service recovers within 10 seconds

**If all pass**: ✅ Service is production-ready!

**If any fail**: ⚠️ Review and optimize before production deployment.

---

## 📞 Support

For issues or questions:
- Review service logs: `./logs/runtime-service.log`
- Check metrics: http://localhost:9090
- Review architecture: `../ARCHITECTURE.md`

---

## 🔗 Related Files

- `quick-test.ps1` - Simple 200-request load test
- `input-json-example.json` - Sample JSON payload
- `input-soap-example.xml` - Sample SOAP payload
- `../monitoring/` - Prometheus & Grafana configs



