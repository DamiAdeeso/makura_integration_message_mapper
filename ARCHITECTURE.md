# Makura ISO 20022 Platform - Architecture

## 🏗️ **System Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                        MAKURA PLATFORM                          │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────┐         ┌──────────────────────┐
│   Frontend (React)   │         │  Dashboard Backend   │
│   Port: 5173         │────────▶│  Port: 8081          │
│                      │  HTTP   │                      │
│  • Routes UI         │         │  • Route CRUD        │
│  • Users UI          │         │  • User Management   │
│  • Roles UI          │         │  • Permissions       │
│  • Mappings UI       │         │  • JWT Auth          │
│  • Metrics Charts    │         │  • REST API          │
└──────────────────────┘         └──────────┬───────────┘
                                            │
                                            │
┌──────────────────────┐                   │
│  External Clients    │                   │
│  (Banks, Systems)    │                   │
│                      │                   │
│  • API Keys          │                   ▼
│  • JSON/SOAP/XML     │         ┌─────────────────────┐
└──────────┬───────────┘         │   MySQL Database    │
           │                     │   makura_dashboard  │
           │                     │                     │
           │                     │  Tables:            │
           │                     │  • routes           │
           │                     │  • users            │
           │                     │  • roles            │
           │                     │  • permissions      │
           │                     │  • field_mappings   │
           │                     │  • audit_logs       │
           │                     └──────────┬──────────┘
           │                                │
           │                                │ SHARED DB
           │                                │
           ▼                                ▼
┌──────────────────────┐         ┌─────────────────────┐
│  Runtime Service     │◀────────│  Reads Routes       │
│  Port: 8080          │         │  from Database      │
│                      │         └─────────────────────┘
│  • Translation API   │
│  • ISO 20022 Gen.    │
│  • Field Mapping     │
│  • Forwarding        │
│  • Encryption        │
└──────────────────────┘
```

---

## 🔄 **Data Flow**

### **1. Route Configuration Flow**

```
Admin User
    │
    │ 1. Login to Dashboard
    ▼
Dashboard Frontend (5173)
    │
    │ 2. Create/Edit Route
    │    - Route ID: CREDIT_TRANSFER_PACS008
    │    - Mode: PASSIVE
    │    - Formats: JSON → ISO_XML
    ▼
Dashboard Backend (8081)
    │
    │ 3. Validate & Save
    ▼
MySQL Database (makura_dashboard)
    │
    │ 4. Store in 'routes' table
    │
    ├──▶ Dashboard reads for UI
    │
    └──▶ Runtime Service reads for translation
```

### **2. Translation Request Flow**

```
External Client
    │
    │ POST /api/v1/translate/CREDIT_TRANSFER_PACS008
    │ Header: X-API-Key: abc123
    │ Body: {"amount": 1000, "currency": "USD", ...}
    ▼
Runtime Service (8080)
    │
    │ 1. Validate API Key
    │ 2. Look up route: CREDIT_TRANSFER_PACS008
    ▼
MySQL Database
    │
    │ 3. Read route configuration
    │    - inboundFormat: JSON
    │    - outboundFormat: ISO_XML
    │    - mode: PASSIVE
    │    - encryption: NONE
    ▼
Runtime Service
    │
    │ 4. Load field mappings
    │ 5. Apply transformations
    │ 6. Generate ISO 20022 XML
    │
    ├──▶ If PASSIVE: Return ISO XML
    │
    └──▶ If ACTIVE: Forward to endpoint
              │
              ▼
         Downstream System
```

---

## 🗄️ **Database Schema**

### **Shared Database: `makura_dashboard`**

Both services connect to the same database:

```sql
-- Dashboard Backend
spring.datasource.url=jdbc:mysql://localhost:3306/makura_dashboard
spring.datasource.username=makura_user
spring.datasource.password=makura_pass

-- Runtime Service (SAME DATABASE)
spring.datasource.url=jdbc:mysql://localhost:3306/makura_dashboard
spring.datasource.username=makura_user
spring.datasource.password=makura_pass
```

### **Key Tables:**

```
routes
├── id (PK)
├── route_id (unique)
├── name
├── description
├── mode (ACTIVE/PASSIVE)
├── inbound_format
├── outbound_format
├── endpoint (nullable)
├── encryption_type
├── encryption_key_ref
├── active
├── published
└── timestamps

field_mappings
├── id (PK)
├── route_id (FK → routes)
├── source_field
├── target_field
├── transformation_type
├── transformation_rule
├── required
└── timestamps

users
├── id (PK)
├── username (unique)
├── password (hashed)
├── email
├── active
└── timestamps

roles
├── id (PK)
├── name (unique)
├── description
├── is_system_role
└── timestamps

permissions
├── id (PK)
├── name (unique)
├── resource
├── action
└── description
```

---

## 🎯 **Route Modes Explained**

### **PASSIVE Mode** (Default)
```
Client → Runtime Service → Translate → Return ISO XML
```
- Runtime service translates and returns result
- No forwarding to downstream systems
- Used for: Testing, client-side processing

**Example Routes:**
- `CREDIT_TRANSFER_PACS008`
- `PAYMENT_STATUS_PACS002`
- `ACCOUNT_STATEMENT_CAMT053`

### **ACTIVE Mode** (With Forwarding)
```
Client → Runtime Service → Translate → Forward → Downstream System
```
- Runtime service translates AND forwards
- Requires `endpoint` configuration
- Used for: Integration with banks, clearing houses

**Example Route:**
```java
RouteConfig.builder()
    .routeId("SECURITIES_SETTLEMENT_SESE023")
    .mode(RouteConfig.RouteMode.ACTIVE)
    .endpoint("https://securities-clearinghouse.example.com/api/settlement")
    .build();
```

---

## 🔐 **Security Architecture**

### **Dashboard Backend (8081)**
- **Authentication:** JWT tokens
- **Authorization:** Role-based + Permission-based
- **Users:** Admin, Operator, Viewer
- **Permissions:** Fine-grained (e.g., `routes:create`, `users:view`)

### **Runtime Service (8080)**
- **Authentication:** API Keys
- **Stored in:** Database (api_keys table)
- **Validation:** Per-request header check
- **Rate Limiting:** Configurable per API key

---

## 📊 **Service Responsibilities**

### **Dashboard Backend (8081)**
✅ **Management & Configuration**
- Route CRUD operations
- User & role management
- Permission management
- Field mapping configuration
- Audit logging
- Metrics aggregation

### **Runtime Service (8080)**
✅ **Execution & Translation**
- Process translation requests
- Read route configurations from DB
- Apply field mappings
- Generate ISO 20022 messages
- Handle encryption/decryption
- Forward to downstream systems (ACTIVE mode)
- Collect metrics

---

## 🔄 **Synchronization**

### **Always In Sync** ✅
Both services read from the **same database**, so:
- ✅ No sync delays
- ✅ No API calls needed
- ✅ Single source of truth
- ✅ Real-time updates

### **When Dashboard Updates a Route:**
```
1. Admin edits route in dashboard UI
2. Dashboard backend saves to database
3. Runtime service reads updated config on next request
4. No manual refresh needed!
```

---

## 🚀 **Deployment**

### **Development (Current)**
```bash
# Terminal 1: Dashboard Backend
cd dashboard-backend
mvn spring-boot:run
# Runs on: http://localhost:8081

# Terminal 2: Runtime Service
cd runtime-service
mvn spring-boot:run
# Runs on: http://localhost:8080

# Terminal 3: Frontend
cd dashboard-frontend
npm run dev
# Runs on: http://localhost:5173
```

### **Production**
```bash
# Build JARs
mvn clean package -DskipTests

# Run Dashboard Backend
java -jar dashboard-backend/target/dashboard-backend.jar

# Run Runtime Service
java -jar runtime-service/target/runtime-service.jar

# Frontend (build & serve)
cd dashboard-frontend
npm run build
# Serve dist/ with nginx/apache
```

---

## 🎨 **Technology Stack**

### **Frontend**
- React 18 + TypeScript
- Vite (build tool)
- Ant Design (UI components)
- React Query (data fetching)
- Zustand (state management)
- Recharts (charts)

### **Backend (Both Services)**
- Java 21
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.x
- Lombok
- OpenAPI/Swagger

### **Infrastructure**
- MySQL Database (shared)
- Prometheus (metrics)
- Grafana (visualization)

---

## 📈 **Scalability**

### **Horizontal Scaling**
```
Load Balancer
    │
    ├──▶ Dashboard Backend Instance 1 (8081)
    ├──▶ Dashboard Backend Instance 2 (8082)
    └──▶ Dashboard Backend Instance 3 (8083)
              │
              ▼
         MySQL (Shared)
              ▲
              │
    ├──▶ Runtime Service Instance 1 (8080)
    ├──▶ Runtime Service Instance 2 (8090)
    └──▶ Runtime Service Instance 3 (8091)
```

### **Database Optimization**
- Connection pooling
- Read replicas for runtime service
- Caching layer (Redis) for route configs

---

## ✅ **Benefits of This Architecture**

1. **Single Source of Truth** - Database is the authority
2. **Always In Sync** - No manual refresh needed
3. **Clean Separation** - Management vs Execution
4. **Scalable** - Each service scales independently
5. **Maintainable** - Clear responsibilities
6. **Secure** - Different auth mechanisms per service
7. **Observable** - Centralized metrics and logging

---

## 🎯 **Summary**

```
Dashboard Backend (8081)
    ↓ Manages
MySQL Database (makura_dashboard)
    ↑ Reads
Runtime Service (8080)
    ↓ Executes
ISO 20022 Translations
```

**Perfect architecture for enterprise ISO 20022 translation platform!** 🚀



