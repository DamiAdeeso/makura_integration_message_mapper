# ISO 20022 Callable JAR

A standalone, embeddable Java library for bi-directional ISO 20022 message translation. This library can be integrated into any Java application to provide ISO 20022 translation capabilities without requiring a full runtime service.

## Features

- ✅ **Bi-Directional Translation**: Transform messages from source formats to ISO 20022 and back
- ✅ **Multiple Input Formats**: Supports JSON, SOAP, and XML input formats
- ✅ **YAML-Based Mapping**: Flexible field-to-field mapping configuration via YAML files
- ✅ **Encryption Support**: Optional AES and PGP encryption/decryption
- ✅ **HTTP Forwarding**: Optional HTTP client for forwarding messages to downstream systems
- ✅ **Zero Dependencies on Spring**: Pure Java implementation, works in any Java 21+ application
- ✅ **Builder Pattern**: Easy configuration with fluent API

## Technology Stack

- Java 21
- SnakeYAML (YAML parsing)
- Jackson (JSON/XML processing)
- Dom4j (XML manipulation)
- BouncyCastle (encryption)
- Jakarta SOAP API

## Installation

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.makura</groupId>
    <artifactId>callable-jar</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Build from Source

```bash
cd callable-jar
mvn clean install
```

## Quick Start

### Basic Usage

```java
import com.makura.translator.*;

// Create translator with default settings
IsoTranslator translator = new IsoTranslatorBuilder()
    .withMappingsPath("./mappings")
    .build();

// Translate request to ISO
SourceMessage request = new SourceMessage(jsonContent, "JSON");
IsoMessage isoMessage = translator.translateRequest(request, "SYSTEM_TO_NIP");

// Translate ISO response back to source format
IsoMessage isoResponse = new IsoMessage(isoXmlContent);
SourceMessage sourceResponse = translator.translateResponse(isoResponse, "SYSTEM_TO_NIP");
```

### Advanced Usage with Encryption

```java
// Create translator with encryption support
IsoTranslator translator = new IsoTranslatorBuilder()
    .withMappingsPath("./mappings")
    .withEncryption("./keys")
    .build();

// Use with options
TranslationOptions options = TranslationOptions.builder()
    .routeId("SYSTEM_TO_NIP")
    .encrypt(true)
    .encryptionType(TranslationOptions.EncryptionType.AES)
    .encryptionKeyRef("my-aes-key")
    .build();

TranslationResult result = translator.translateWithOptions(request, options);
String encryptedIso = result.getIsoMessage();
```

### Advanced Usage with HTTP Forwarding

```java
// Create translator with forwarding support
IsoTranslator translator = new IsoTranslatorBuilder()
    .withMappingsPath("./mappings")
    .withForwarding()
    .withTimeouts(5000, 30000)
    .build();

// Forward to downstream system
TranslationOptions options = TranslationOptions.builder()
    .routeId("SYSTEM_TO_NIP")
    .forward(true)
    .endpoint("https://api.example.com/iso")
    .forwardingApiKey("your-api-key")
    .build();

TranslationResult result = translator.translateWithOptions(request, options);
String isoMessage = result.getIsoMessage();
String downstreamResponse = result.getForwardingResponse();
```

### All Features Combined

```java
// Create fully-featured translator
IsoTranslator translator = new IsoTranslatorBuilder()
    .withMappingsPath("./mappings")
    .withEncryption("./keys")
    .withForwarding()
    .withTimeouts(5000, 30000)
    .build();

// Use all features
TranslationOptions options = TranslationOptions.builder()
    .routeId("SYSTEM_TO_NIP")
    .encrypt(true)
    .encryptionType(TranslationOptions.EncryptionType.PGP)
    .encryptionKeyRef("pgp-key")
    .forward(true)
    .endpoint("https://api.example.com/iso")
    .forwardingApiKey("your-api-key")
    .connectTimeout(5000)
    .readTimeout(30000)
    .build();

TranslationResult result = translator.translateWithOptions(request, options);
```

## YAML Mapping Format

Create mapping files in your mappings directory (e.g., `mappings/SYSTEM_TO_NIP.yaml`):

```yaml
routeId: SYSTEM_TO_NIP
inboundFormat: JSON
outboundFormat: ISO_XML
mode: ACTIVE
endpoint: "https://nip.bank/api/payments"

mappings:
  request:
    - from: source.customer.account
      to: iso:DebtorAccount/Identification
    - from: source.customer.name
      to: iso:Debtor/Name
    - from: source.amount
      to: iso:InstructedAmount/Amount
    - from: source.currency
      to: iso:InstructedAmount/@Ccy
    - from: source.reference
      to: iso:EndToEndIdentification
    - from: source.creditor.account
      to: iso:CreditorAccount/Identification
    - from: source.creditor.name
      to: iso:Creditor/Name
      
  response:
    - from: iso:TxSts
      to: source.status
    - from: iso:ClrSysRef
      to: source.reference
```

## API Reference

### IsoTranslatorBuilder

| Method | Description |
|--------|-------------|
| `withMappingsPath(String)` | Set path to YAML mapping files |
| `withEncryption(String)` | Enable encryption with keys path |
| `withForwarding()` | Enable HTTP forwarding |
| `withTimeouts(int, int)` | Set connect and read timeouts (ms) |
| `build()` | Build the IsoTranslator instance |

### IsoTranslator

| Method | Description |
|--------|-------------|
| `translateRequest(SourceMessage, String)` | Translate source to ISO |
| `translateResponse(IsoMessage, String)` | Translate ISO to source |
| `translateWithOptions(SourceMessage, TranslationOptions)` | Translate with advanced options |

### TranslationOptions

| Field | Type | Description |
|-------|------|-------------|
| `routeId` | String | Route identifier |
| `encrypt` | boolean | Enable encryption |
| `encryptionType` | EncryptionType | AES or PGP |
| `encryptionKeyRef` | String | Key reference name |
| `forward` | boolean | Enable HTTP forwarding |
| `endpoint` | String | Forwarding endpoint URL |
| `forwardingApiKey` | String | API key for forwarding |
| `connectTimeout` | int | Connect timeout (ms) |
| `readTimeout` | int | Read timeout (ms) |

### TranslationResult

| Field | Type | Description |
|-------|------|-------------|
| `isoMessage` | String | The translated ISO message |
| `forwardingResponse` | String | Response from downstream (if forwarded) |
| `forwarded` | boolean | Whether message was forwarded |

## Encryption Setup

### AES Keys

Place AES key files in `keys/aes/`:

```
keys/
└── aes/
    └── my-aes-key.key  (32 bytes for AES-256)
```

### PGP Keys

Place PGP key files in `keys/pgp/`:

```
keys/
└── pgp/
    ├── my-pgp-key_public.asc
    └── my-pgp-key_private.asc
```

## Error Handling

All methods throw `IsoTranslator.TranslationException`:

```java
try {
    IsoMessage result = translator.translateRequest(request, "ROUTE_ID");
} catch (IsoTranslator.TranslationException e) {
    // Handle translation error
    System.err.println("Translation failed: " + e.getMessage());
}
```

## Thread Safety

The `IsoTranslator` implementation is thread-safe and can be shared across multiple threads. Create one instance and reuse it.

## Performance

- Mapping configurations are loaded on-demand and can be cached externally
- No blocking I/O during translation (except for encryption key loading)
- Suitable for high-throughput applications

## Comparison with Runtime Service

| Feature | Callable JAR | Runtime Service |
|---------|--------------|-----------------|
| Translation | ✅ | ✅ |
| Encryption | ✅ | ✅ |
| HTTP Forwarding | ✅ | ✅ |
| REST API | ❌ | ✅ |
| Database Integration | ❌ | ✅ |
| API Key Validation | ❌ | ✅ |
| Metrics/Monitoring | ❌ | ✅ |
| Spring Framework | ❌ | ✅ |

## Examples

See the `examples/` directory for complete working examples:

- `SimpleTranslation.java` - Basic translation
- `WithEncryption.java` - Translation with encryption
- `WithForwarding.java` - Translation with HTTP forwarding
- `FullFeatures.java` - All features combined

## License

Proprietary - Internal Use Only
