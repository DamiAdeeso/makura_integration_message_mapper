# ISO 20022 Runtime Service

Real-time translation and routing service for ISO 20022 messages. This service wraps the **callable-jar** core translation library with REST endpoints, API key validation, forwarding, encryption, and observability features.

## Architecture

This service **depends on and uses** the **callable-jar** module, which contains the core translation logic. The runtime service adds:

- Spring Boot REST API endpoints
- API key authentication and validation
- HTTP forwarding for ACTIVE mode routes
- Encryption/decryption (AES, PGP)
- Metrics and observability
- Database integration (MySQL)

## Features

- **Bi-Directional Translation**: Transform messages from source formats to ISO 20022 and back
- **Multiple Input Formats**: Supports JSON, SOAP, and XML input formats
- **YAML-Based Mapping**: Flexible field-to-field mapping configuration via YAML files
- **Route-Based Processing**: Configurable routes with ACTIVE (forwarding) or PASSIVE modes
- **API Key Authentication**: Per-route API key validation with validity periods
- **Encryption Support**: AES and PGP encryption/decryption for secure message transport
- **High Performance**: Stateless, horizontally scalable design with caching
- **Observability**: Metrics, structured logging, and correlation ID tracking

## Technology Stack

- Java 21
- Spring Boot 3.2.0
- MySQL (for route and API key storage)
- Maven
- Micrometer (metrics)
- BouncyCastle (encryption)
- **callable-jar** (core translation library)

## Project Structure

```
runtime-service/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/makura/runtime/
    │   ├── auth/              # ApiKeyValidator
    │   ├── config/           # CacheConfig, CorrelationIdFilter
    │   ├── controller/       # TranslationController, ConfigController
    │   ├── encryption/       # EncryptionService
    │   ├── forwarding/       # HttpForwardingClient
    │   ├── mapping/          # MappingLoader (wrapper with caching)
    │   ├── metrics/         # TranslationMetrics
    │   ├── model/           # Route, ApiKey entities
    │   ├── repository/      # RouteRepository, ApiKeyRepository
    │   └── service/         # TranslationService (uses callable-jar)
    └── resources/
        └── application.yml
```

## Dependencies

This service depends on the **callable-jar** module:

```xml
<dependency>
    <groupId>com.makura</groupId>
    <artifactId>callable-jar</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Important**: Build the callable-jar module first, then install it to your local Maven repository:

```bash
cd callable-jar
mvn clean install
cd ../runtime-service
mvn clean package
```

## Configuration

### Application Properties

Configure the service via `application.yml` or environment variables:

```yaml
makura:
  runtime:
    mappings:
      base-path: ${MAPPINGS_BASE_PATH:./mappings}  # YAML mapping files location
    encryption:
      keys-path: ${ENCRYPTION_KEYS_PATH:./keys}     # Encryption keys location
    http-client:
      connect-timeout: 5000
      read-timeout: 30000
```

### Database Setup

The service requires a MySQL database. Configure connection in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makura_runtime
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
```

### Directory Structure

Create the following directories:

```
./mappings/          # YAML mapping files (one per route)
./keys/
  ├── aes/           # AES key files
  └── pgp/           # PGP key files (public and private)
```

## YAML Mapping Format

Example mapping configuration (`SYSTEM_TO_NIP.yaml`):

```yaml
routeId: SYSTEM_TO_NIP
inboundFormat: JSON
outboundFormat: ISO_XML
mode: ACTIVE
endpoint: "https://nip.bank/api/payments"
auth:
  type: API_KEY
  key: "abc123"
mappings:
  request:
    - from: source.customer.account
      to: iso:DebtorAccount/Identification
    - from: source.amount
      to: iso:InstructedAmount/Amount
  response:
    - from: iso:TxSts
      to: source.status
    - from: iso:ClrSysRef
      to: source.reference
```

## API Endpoints

### Translate Request

```http
POST /api/v1/translate/{routeId}
Headers:
  X-API-Key: <api-key>
  X-Correlation-Id: <optional-correlation-id>
  Content-Type: application/json (or text/xml for SOAP/XML)
Body: <source message>
```

### Refresh Configuration

```http
POST /api/v1/config/refresh/{routeId}
POST /api/v1/config/refresh/all
```

### Health & Metrics

```http
GET /actuator/health
GET /actuator/metrics
GET /actuator/prometheus
```

## Database Schema

### Routes Table

Stores route configuration:
- `routeId` (unique)
- `inboundFormat`, `outboundFormat`
- `mode` (ACTIVE/PASSIVE)
- `endpoint` (for ACTIVE mode)
- `encryptionType`, `encryptionKeyRef`
- `yamlProfilePath`
- `active`

### API Keys Table

Stores API key hashes and validity:
- `routeId`
- `keyHash` (SHA-256 hash)
- `validFrom`, `validUntil`
- `active`

## Building & Running

### Prerequisites

1. Build and install callable-jar:
   ```bash
   cd callable-jar
   mvn clean install
   ```

2. Build runtime-service:
   ```bash
   cd runtime-service
   mvn clean package
   ```

### Run

```bash
java -jar target/runtime-service-1.0.0-SNAPSHOT.jar
```

Or with Maven:

```bash
mvn spring-boot:run
```

## Development

### Prerequisites

- Java 21
- Maven 3.8+
- MySQL 8.0+

### Testing

```bash
mvn test
```

## Relationship with Callable JAR

The runtime service **uses** the callable-jar module internally:

- **callable-jar**: Contains core translation logic (IsoTranslator, MappingEngine, PathResolver, etc.)
- **runtime-service**: Wraps callable-jar with Spring Boot REST API, database, forwarding, encryption, metrics

This architecture allows:
1. **Standalone usage**: callable-jar can be embedded in any Java application
2. **Service deployment**: runtime-service provides REST API and enterprise features
3. **Single source of truth**: Core logic is maintained in one place

## Security Notes

- API keys are hashed (SHA-256) before storage
- Encryption keys should be stored securely (filesystem with proper permissions)
- Use HTTPS in production
- Implement proper RBAC for config refresh endpoints

## License

Proprietary - Internal Use Only
