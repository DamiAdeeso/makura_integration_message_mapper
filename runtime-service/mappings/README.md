# YAML Mapping Files

This directory contains YAML mapping configuration files for route definitions.

## File Naming Convention

Mapping files must be named: `{routeId}.yaml`

For example:
- `SYSTEM_TO_NIP.yaml` → routeId: `SYSTEM_TO_NIP`
- `XML_TO_ISO.yaml` → routeId: `XML_TO_ISO`

## Example Files

### SYSTEM_TO_NIP.yaml
- Maps JSON input to target XML format
- ACTIVE mode (forwards to downstream endpoint)
- Includes both request and response mappings

### SYSTEM_TO_NIP_PASSIVE.yaml
- Maps JSON input to target XML format
- PASSIVE mode (no forwarding, returns target format directly)
- Simplified mapping example

### XML_TO_ISO.yaml
- Maps XML input to target XML format
- ACTIVE mode with forwarding
- Demonstrates XML path navigation

## YAML Structure

```yaml
routeId: ROUTE_ID
inboundFormat: JSON|SOAP|XML|PROPRIETARY_XML
outboundFormat: XML|JSON|ISO_XML  # XML is generic, ISO_XML is legacy alias
mode: ACTIVE|PASSIVE
endpoint: "https://downstream-endpoint.com/api"  # Required for ACTIVE mode
auth:
  type: API_KEY
  key: "api-key-value"
mappings:
  request:
    - from: source.field.path
      to: target:Element/SubElement
  response:
    - from: target:ResponseElement
      to: source.response.field
```

## Path Expressions

### Source Paths (from)
- Use dot notation: `source.customer.account`
- Supports nested objects and arrays
- Remove "source." prefix is optional

### Target Paths (to)
- Use "target:" prefix: `target:DebtorAccount/Identification`
- Forward slash separates XML elements
- Creates XML structure automatically

## Creating New Mappings

1. Create a new YAML file: `{routeId}.yaml`
2. Define the route configuration
3. Map source fields to target format elements
4. Map target format response fields back to source format
5. Ensure the route exists in the database
6. Test with sample data

## Validation

The runtime service validates:
- File exists for the routeId
- YAML syntax is correct
- Required fields are present
- Path expressions are valid

## Hot Reload

Mapping files can be refreshed without restarting:
```bash
curl -X POST http://localhost:8080/api/v1/config/refresh/{routeId}
```



