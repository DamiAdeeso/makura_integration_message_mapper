# Quick Start Guide

## ✅ Step 1: Database Setup (DONE)
You've already set up the database with test data.

## 🚀 Step 2: Start the Runtime Service

### Option A: Using Maven
```bash
cd runtime-service
mvn spring-boot:run
```

### Option B: Using JAR
```bash
cd runtime-service
java -jar target/runtime-service-1.0.0-SNAPSHOT.jar
```

The service will start on `http://localhost:8080`

## 🧪 Step 3: Test the Service

### Test 1: Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Test 2: Translate Request (JSON to ISO)
```bash
curl -X POST http://localhost:8080/api/v1/translate/SYSTEM_TO_NIP \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-123" \
  -H "X-Correlation-Id: test-001" \
  -d @test-data/input-json-example.json
```

### Test 3: Passive Mode (No Forwarding)
```bash
curl -X POST http://localhost:8080/api/v1/translate/SYSTEM_TO_NIP_PASSIVE \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-456" \
  -d @test-data/input-json-example.json
```

### Test 4: Using PowerShell Script
```powershell
.\test-data\quick-test.ps1
```

## 📊 Step 4: Check Metrics

```bash
# View all metrics
curl http://localhost:8080/actuator/metrics

# View translation requests
curl http://localhost:8080/actuator/metrics/makura.translation.requests.total

# Prometheus format
curl http://localhost:8080/actuator/prometheus
```

## 🔄 Step 5: Refresh Configuration

```bash
# Refresh a specific route
curl -X POST http://localhost:8080/api/v1/config/refresh/SYSTEM_TO_NIP

# Refresh all routes
curl -X POST http://localhost:8080/api/v1/config/refresh/all
```

## 🐛 Troubleshooting

### Service won't start
- Check MySQL is running: `mysql -u root -p -e "SELECT 1;"`
- Verify database exists: `mysql -u root -p -e "SHOW DATABASES LIKE 'makura_runtime';"`
- Check application.yml database credentials

### API Key validation fails
- Verify API key hash in database matches the key you're using
- Check routeId matches between route and API key
- Ensure API key is within validFrom/validUntil dates

### Mapping not found
- Ensure YAML file exists: `mappings/{routeId}.yaml`
- Check file name matches routeId exactly
- Verify YAML syntax is valid

### Translation errors
- Check input format matches YAML `inboundFormat`
- Verify field paths in mappings match your input structure
- Review service logs for detailed error messages

## 📝 Next Steps

1. **Customize Mappings**: Edit YAML files in `mappings/` directory
2. **Add More Routes**: Create new YAML files and insert into database
3. **Test with Real Data**: Replace test data with your actual message formats
4. **Configure Encryption**: Set up AES/PGP keys for secure forwarding
5. **Monitor**: Use metrics endpoints for observability

## 🎯 Example Workflow

1. Start service: `mvn spring-boot:run`
2. Test health: `curl http://localhost:8080/actuator/health`
3. Send test request: Use the curl commands above
4. Check response: Verify ISO XML output
5. View metrics: Monitor translation performance

Happy testing! 🚀




