#!/bin/bash
# Quick test script for runtime service
# Make sure the service is running on localhost:8080

BASE_URL="http://localhost:8080/api/v1/translate"

echo "=== Testing SYSTEM_TO_NIP (ACTIVE mode) ==="
curl -X POST "${BASE_URL}/SYSTEM_TO_NIP" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-123" \
  -H "X-Correlation-Id: test-001" \
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
  }' | jq '.'

echo -e "\n=== Testing SYSTEM_TO_NIP_PASSIVE (PASSIVE mode) ==="
curl -X POST "${BASE_URL}/SYSTEM_TO_NIP_PASSIVE" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-456" \
  -d '{
    "source": {
      "customer": {
        "account": "1234567890",
        "name": "John Doe"
      },
      "amount": "1000.50",
      "currency": "USD",
      "reference": "TXN-2024-001"
    }
  }' | jq '.'

echo -e "\n=== Testing Health Endpoint ==="
curl -s http://localhost:8080/actuator/health | jq '.'

echo -e "\n=== Testing Metrics ==="
curl -s http://localhost:8080/actuator/metrics/makura.translation.requests.total | jq '.'




