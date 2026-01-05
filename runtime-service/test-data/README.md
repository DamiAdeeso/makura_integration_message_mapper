# Test Data Directory

This directory contains example test data files for testing the runtime service.

## Files

### Input Files
- **input-json-example.json** - Example JSON input matching the YAML mapping structure
- **input-xml-example.xml** - Example XML input for XML-to-ISO translation

### Expected Output Files
- **expected-iso-output-example.xml** - Expected ISO XML output after translation
- **iso-response-example.xml** - Example ISO response from downstream system
- **expected-response-json-example.json** - Expected JSON response after reverse translation

## Usage

These files can be used with curl commands for testing:

```bash
# Test JSON input
curl -X POST http://localhost:8080/api/v1/translate/SYSTEM_TO_NIP \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-api-key-123" \
  -d @input-json-example.json

# Test XML input
curl -X POST http://localhost:8080/api/v1/translate/XML_TO_ISO \
  -H "Content-Type: application/xml" \
  -H "X-API-Key: xml-api-key-789" \
  -d @input-xml-example.xml
```

## Customizing Test Data

Modify these files to match your specific use cases:
1. Update field values to test different scenarios
2. Add/remove fields to test mapping edge cases
3. Create variations for different route configurations




