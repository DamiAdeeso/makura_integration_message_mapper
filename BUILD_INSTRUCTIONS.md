# Build Instructions

## Prerequisites

- Java 21 installed
- Maven 3.8+ installed
- MySQL 8.0+ (for runtime-service)

## Setting JAVA_HOME

**Important**: Maven needs to use Java 21. Set JAVA_HOME before building:

### Windows (PowerShell)
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
```

### Windows (Permanent - System Environment Variables)
1. Open System Properties → Environment Variables
2. Set `JAVA_HOME` to: `C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot`
3. Add `%JAVA_HOME%\bin` to `PATH` if not already present

### Verify Java Version
```bash
mvn -version
```
Should show: `Java version: 21.x.x`

## Building the Projects

### Step 1: Build callable-jar (must be built first)

```bash
cd callable-jar
mvn clean install
```

This installs the callable-jar to your local Maven repository.

### Step 2: Build runtime-service

```bash
cd ../runtime-service
mvn clean package
```

Or to run directly:

```bash
mvn spring-boot:run
```

## Build Order

1. **callable-jar** - Core translation library (must be built first)
2. **runtime-service** - REST API wrapper (depends on callable-jar)

## Troubleshooting

### "release version 21 not supported"
- Maven is not using Java 21
- Set JAVA_HOME to point to Java 21 installation
- Verify with: `mvn -version`

### "Missing artifact com.makura:callable-jar"
- Build callable-jar first: `cd callable-jar && mvn clean install`
- Then build runtime-service

### Compilation Errors
- Ensure Java 21 is installed and JAVA_HOME is set correctly
- Clean and rebuild: `mvn clean install`




