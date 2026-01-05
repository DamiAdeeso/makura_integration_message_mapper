# Testing Guide

This guide explains how to test the runtime service with the provided example data.

## Prerequisites

1. MySQL database running
2. Runtime service built and running
3. Example YAML mappings in `./mappings/` directory
4. Test data files in `./test-data/` directory

## Setup Steps

### Option 1: Complete SQL Setup (Recommended)

Run the complete setup script that creates everything:

```bash
mysql -u root -p < test-data/setup-complete.sql
```

This creates:
- Database
- Tables (routes, api_keys)
- Test routes
- Test API keys with real hashes

### Option 2: Auto-Create Tables (Alternative)

The runtime service will auto-create tables on first run via JPA. Then insert test data:

```bash
# Start service once to create tables, then stop it
# Then run:
mysql -u root -p makura_runtime < test-data/setup-test-data.sql
```

### Option 3: Manual Setup

Create database and tables manually:

```bash
mysql -u root -p < test-data/create-tables.sql
mysql -u root -p makura_runtime < test-data/setup-test-data.sql
```

### 2. Insert Test Route

Insert a test route into the database:

```sql
INSERT INTO routes (routeId, inboundFormat, outboundFormat, mode, endpoint, active, createdAt, updatedAt)
VALUES ('SYSTEM_TO_NIP', 'JSON', 'ISO_XML', 'ACTIVE', 'https://nip.bank/api/payments', true, NOW(), NOW());
```

### 3. Insert Test API Key

Insert a test API key (hashed):

```sql
-- Hash for "test-api-key-123" (SHA-256)
INSERT INTO api_keys (routeId, keyHash, validFrom, validUntil, active, createdAt, updatedAt)
VALUES (
    'SYSTEM_TO_NIP',
    'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',  -- SHA-256 of "test-api-key-123"
    NOW(),
    DATE_ADD(NOW(), INTERVAL 1 YEAR),
    true,
    NOW(),
    NOW()
);
```

**Note**: The keyHash above is just an example. In production, use the actual SHA-256 hash of your API key.

## Testing Scenarios

### Scenario 1: JSON to ISO Translation (ACTIVE Mode)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/translate/SYSTEM_TO_NIP \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-123" \
  -H "X-Correlation-Id: test-001" \
  -d @test-data/input-json-example.json
```

**Expected Flow:**
1. Service validates API key
2. Parses JSON input
3. Applies YAML mappings
4. Generates ISO XML
5. Forwards to endpoint (if ACTIVE mode)
6. Transforms response back to JSON

### Scenario 2: JSON to ISO Translation (PASSIVE Mode)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/translate/SYSTEM_TO_NIP_PASSIVE \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-456" \
  -d @test-data/input-json-example.json
```

**Expected Flow:**
1. Service validates API key
2. Parses JSON input
3. Applies YAML mappings
4. Generates ISO XML
5. Returns ISO XML directly (no forwarding)

### Scenario 3: XML to ISO Translation

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/translate/XML_TO_ISO \
  -H "Content-Type: application/xml" \
  -H "X-API-Key: xml-api-key-789" \
  -d @test-data/input-xml-example.xml
```

## Manual Testing Steps

### Step 1: Start the Service

```bash
cd runtime-service
java -jar target/runtime-service-1.0.0-SNAPSHOT.jar
```

Or with Maven:
```bash
mvn spring-boot:run
```

### Step 2: Verify Health

```bash
curl http://localhost:8080/actuator/health
```

### Step 3: Test Translation Endpoint

Using the example JSON file:

```bash
curl -X POST http://localhost:8080/api/v1/translate/SYSTEM_TO_NIP \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-123" \
  -d '{
    "source": {
      "customer": {
        "account": "1234567890",
        "name": "John Doe"
      },
      "amount": "1000.50",
      "currency": "USD",
      "reference": "TXN-2024-001",
      "creditor": {
        "account": "9876543210",
        "name": "Jane Smith"
      }
    }
  }'
```

### Step 4: Check Metrics

```bash
curl http://localhost:8080/actuator/metrics/makura.translation.requests.total
curl http://localhost:8080/actuator/prometheus
```

## Expected Output

### Input (JSON):
```json
{
  "source": {
    "customer": {
      "account": "1234567890",
      "name": "John Doe"
    },
    "amount": "1000.50",
    "currency": "USD",
    "reference": "TXN-2024-001"
  }
}
```

### Intermediate ISO XML:
```xml
<Document>
    <DebtorAccount>
        <Identification>1234567890</Identification>
        <Name>John Doe</Name>
    </DebtorAccount>
    <InstructedAmount>
        <Amount>1000.50</Amount>
        <Currency>USD</Currency>
    </InstructedAmount>
    <EndToEndIdentification>TXN-2024-001</EndToEndIdentification>
</Document>
```

### Output (JSON Response):
```json
{
  "response": "...",
  "correlationId": "..."
}
```

## Troubleshooting

### API Key Validation Fails
- Check that the API key hash in database matches the SHA-256 hash of your API key
- Verify the routeId matches between route and API key records
- Check that `validFrom` and `validUntil` dates are correct

### Mapping Not Found
- Ensure YAML file exists in `./mappings/` directory
- File name must match routeId: `{routeId}.yaml`
- Check YAML syntax is valid

### Translation Errors
- Verify input format matches `inboundFormat` in YAML
- Check field paths in mappings match your input structure
- Review logs for detailed error messages

### Database Connection Issues
- Verify MySQL is running
- Check `application.yml` database configuration
- Ensure database exists: `makura_runtime`

## Test Data Files

- `input-json-example.json` - Sample JSON input
- `input-xml-example.xml` - Sample XML input
- `expected-iso-output-example.xml` - Expected ISO XML output
- `iso-response-example.xml` - Sample ISO response
- `expected-response-json-example.json` - Expected JSON response

## Next Steps

1. Create more complex mapping scenarios
2. Test with encryption (AES/PGP)
3. Test error handling
4. Load testing with multiple concurrent requests
5. Integration testing with actual downstream systems

