# ISO 20022 Dashboard Backend

Configuration and management dashboard for the ISO 20022 Translation Service.

## What's Been Built

### ✅ Project Structure
- Maven project with Spring Boot 3.2.0
- Java 21
- Complete dependency management (Security, JPA, JWT, Swagger, WebFlux)

### ✅ Database Schema
**Tables**:
- `dashboard_users` - User authentication and RBAC
- `route_configs` - Route configuration (mirrors runtime-service routes)
- `mapping_definitions` - Field-to-field mapping definitions
- `audit_log` - User action tracking

**Entities**:
- `User` - with roles (ADMIN, OPERATOR, VIEWER)
- `RouteConfig` - route management with YAML versioning
- `MappingDefinition` - source-to-ISO field mappings
- `AuditLog` - comprehensive audit trail

### ✅ Repositories (Spring Data JPA)
- `UserRepository`
- `RouteConfigRepository`
- `MappingDefinitionRepository`
- `AuditLogRepository`

## What Still Needs to Be Built

### 🔧 Security & Authentication
1. **JWT Utilities** (`security/JwtUtil.java`)
   - Token generation
   - Token validation
   - Claims extraction

2. **User Details Service** (`security/CustomUserDetailsService.java`)
   - Load user by username
   - Convert to Spring Security UserDetails

3. **JWT Filter** (`security/JwtAuthenticationFilter.java`)
   - Intercept requests
   - Validate JWT tokens
   - Set authentication context

4. **Security Configuration** (`config/SecurityConfig.java`)
   - Configure endpoint security
   - Enable CORS
   - Password encoding (BCrypt)

### 📡 REST APIs

#### Authentication API (`controller/AuthController.java`)
```
POST /api/v1/auth/login    - User login
POST /api/v1/auth/logout   - User logout
GET  /api/v1/auth/me       - Get current user
```

#### User Management API (`controller/UserController.java`)
```
GET    /api/v1/users          - List users
POST   /api/v1/users          - Create user
GET    /api/v1/users/{id}     - Get user
PUT    /api/v1/users/{id}     - Update user
DELETE /api/v1/users/{id}     - Delete user
```

#### Route Management API (`controller/RouteController.java`)
```
GET    /api/v1/routes              - List routes
POST   /api/v1/routes              - Create route
GET    /api/v1/routes/{id}         - Get route
PUT    /api/v1/routes/{id}         - Update route
DELETE /api/v1/routes/{id}         - Delete route
POST   /api/v1/routes/{id}/publish - Publish to runtime
```

#### Mapping API (`controller/MappingController.java`)
```
GET    /api/v1/mappings/fields/{format}   - Get available fields
POST   /api/v1/mappings/validate          - Validate mappings
POST   /api/v1/mappings/generate-yaml     - Generate YAML
POST   /api/v1/mappings/preview           - Preview transformation
```

#### Metrics API (`controller/MetricsController.java`)
```
GET /api/v1/metrics/overview           - Dashboard overview
GET /api/v1/metrics/routes             - All route metrics
GET /api/v1/metrics/routes/{id}        - Single route metrics
```

#### Audit Log API (`controller/AuditController.java`)
```
GET /api/v1/audit-logs    - List audit logs (paginated)
```

### 🔨 Services

1. **AuthService** - Login, JWT generation
2. **UserService** - User CRUD operations
3. **RouteService** - Route CRUD + YAML management
4. **MappingService** - Field mapping + YAML generation
5. **AuditService** - Audit logging
6. **RuntimeIntegrationService** - Communicate with runtime-service
7. **MetricsService** - Aggregate metrics from runtime

### 📋 DTOs (Data Transfer Objects)

- `LoginRequest`, `LoginResponse`
- `UserDTO`, `CreateUserRequest`, `UpdateUserRequest`
- `RouteDTO`, `CreateRouteRequest`, `UpdateRouteRequest`
- `MappingDTO`, `MappingValidationRequest`
- `MetricsDTO`, `RouteMetricsDTO`
- `AuditLogDTO`

## Quick Start

### 1. Configure Database

Update `application.yml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makura_dashboard
    username: your_username
    password: your_password
```

### 2. Build

```bash
cd dashboard-backend
mvn clean install
```

### 3. Run

```bash
mvn spring-boot:run
```

Or using the JAR:

```bash
java -jar target/dashboard-backend-1.0.0-SNAPSHOT.jar
```

### 4. Access

- **API Base URL**: http://localhost:8081/dashboard-api
- **Swagger UI**: http://localhost:8081/dashboard-api/swagger-ui.html
- **Health Check**: http://localhost:8081/dashboard-api/actuator/health

### 5. Default Credentials

First time setup will create an admin user:
- **Username**: `admin`
- **Password**: `admin123` (change immediately!)

## Integration with Runtime Service

The dashboard communicates with the runtime-service to:
1. **Publish routes** - Push YAML configs to runtime
2. **Fetch metrics** - Aggregate translation metrics
3. **Test routes** - Validate configurations

Configure runtime service URL in `application.yml`:

```yaml
makura:
  runtime:
    base-url: http://localhost:8080
```

## Security

- **JWT Authentication**: All endpoints except /auth/login require valid JWT
- **RBAC**: Role-based access control enforced
- **Password Hashing**: BCrypt with salt
- **Audit Logging**: All actions logged
- **CORS**: Configured for frontend origin

## API Documentation

After starting the service, visit:
http://localhost:8081/dashboard-api/swagger-ui.html

## Next Steps

1. Complete JWT security implementation
2. Implement all REST controllers
3. Build service layer
4. Add comprehensive error handling
5. Write tests
6. Build React frontend

## Architecture

```
dashboard-backend/
├── src/main/java/com/makura/dashboard/
│   ├── DashboardApplication.java
│   ├── config/                # Spring configuration
│   ├── controller/            # REST endpoints
│   ├── dto/                   # Request/response DTOs
│   ├── model/                 # JPA entities ✅
│   ├── repository/            # Data access ✅
│   ├── security/              # JWT & authentication
│   ├── service/               # Business logic
│   └── util/                  # Helpers
├── src/main/resources/
│   └── application.yml        ✅
└── pom.xml                    ✅
```

## Contact & Support

For issues or questions, refer to the main Makura documentation.




