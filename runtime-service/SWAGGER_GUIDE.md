# Swagger/OpenAPI Documentation Guide

The runtime service includes Swagger/OpenAPI documentation for easy API exploration and testing.

## Accessing Swagger UI

Once the service is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## API Documentation Endpoints

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/api-docs.yaml`

## Features

### 1. Interactive API Testing
- Test endpoints directly from the browser
- No need for curl or Postman
- See request/response examples

### 2. API Documentation
- Complete endpoint descriptions
- Request/response schemas
- Example values
- Error responses

### 3. Authentication
- API key authentication is documented
- Use the "Authorize" button to set your API key
- Key: `X-API-Key` header

## Using Swagger UI

### Step 1: Open Swagger UI
Navigate to: `http://localhost:8080/swagger-ui.html`

### Step 2: Authorize (Optional)
1. Click the "Authorize" button at the top
2. Enter your API key (e.g., `test-api-key-123`)
3. Click "Authorize" then "Close"

### Step 3: Test an Endpoint
1. Expand the "Translation" section
2. Click on `POST /api/v1/translate/{routeId}`
3. Click "Try it out"
4. Fill in:
   - `routeId`: `SYSTEM_TO_NIP`
   - `X-API-Key`: `test-api-key-123`
   - `requestBody`: Paste your JSON/XML
5. Click "Execute"
6. View the response

## Example Request Body

For the translation endpoint, use:

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

## Available Endpoints

### Translation Endpoints
- `POST /api/v1/translate/{routeId}` - Translate message to ISO 20022

### Configuration Endpoints
- `POST /api/v1/config/refresh/{routeId}` - Refresh mapping cache for a route
- `POST /api/v1/config/refresh/all` - Refresh all mapping caches

## Troubleshooting

### Swagger UI not loading
- Ensure the service is running
- Check the port (default: 8080)
- Verify SpringDoc dependency is included

### API key not working
- Verify the API key hash in the database
- Check the routeId matches
- Ensure API key is within validity period

### CORS issues
- Swagger UI runs on the same origin, so CORS shouldn't be an issue
- If accessing from different origin, configure CORS in Spring

## Customization

Edit `OpenApiConfig.java` to customize:
- API title and description
- Contact information
- Server URLs
- License information

## Export Documentation

You can export the OpenAPI specification:

```bash
# JSON format
curl http://localhost:8080/api-docs > api-docs.json

# YAML format
curl http://localhost:8080/api-docs.yaml > api-docs.yaml
```

This can be imported into tools like:
- Postman
- Insomnia
- API clients
- Documentation generators




