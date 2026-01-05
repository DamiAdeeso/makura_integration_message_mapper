# Dashboard Implementation Plan

## Overview

Implementing the **ISO 20022 Dashboard** - a standalone management and configuration interface for the translation service, as specified in Makura.txt.

## Architecture

```
┌───────────────────────────┐        ┌──────────────────────────┐        ┌────────────────────────┐
│ Dashboard Frontend        │        │ Dashboard Backend        │        │ Runtime Service        │
│ (React + TypeScript)      │◄──────►│ (Spring Boot)           │◄──────►│ (Translation Engine)   │
│                           │  REST  │                          │  REST  │                        │
│ - Route Management        │        │ - Route CRUD API         │        │ - Translation API      │
│ - Mapping Builder         │        │ - YAML Generation        │        │ - Config Refresh       │
│ - Preview/Test            │        │ - Validation Engine      │        │ - Metrics Endpoint     │
│ - User Management (RBAC)  │        │ - User/Auth Management   │        │                        │
│ - Metrics Dashboard       │        │ - Audit Logging          │        └────────────────────────┘
│ - API Key Management      │        │ - Metrics Aggregation    │
└───────────────────────────┘        └──────────────────────────┘
                                                  │
                                                  ▼
                                        ┌──────────────────┐
                                        │ Dashboard DB     │
                                        │ - Users/Roles    │
                                        │ - Routes Config  │
                                        │ - Audit Log      │
                                        │ - API Keys       │
                                        └──────────────────┘
```

## Dashboard Backend (Spring Boot)

### Technology Stack:
- Java 21
- Spring Boot 3.2+
- Spring Security (JWT authentication)
- Spring Data JPA
- MySQL (shared with runtime or separate)
- Maven

### Core Features:

#### 1. Route Management API
```java
POST   /api/v1/routes              - Create route
GET    /api/v1/routes              - List all routes
GET    /api/v1/routes/{id}         - Get route details
PUT    /api/v1/routes/{id}         - Update route
DELETE /api/v1/routes/{id}         - Delete route
POST   /api/v1/routes/{id}/publish - Publish route to runtime
```

#### 2. Mapping Builder API
```java
POST   /api/v1/mappings/validate   - Validate mapping definition
POST   /api/v1/mappings/generate   - Generate YAML from UI config
GET    /api/v1/mappings/fields     - Get available ISO 20022 fields
POST   /api/v1/mappings/preview    - Preview transformation
```

#### 3. User & RBAC API
```java
POST   /api/v1/auth/login          - User login (returns JWT)
POST   /api/v1/auth/logout         - User logout
GET    /api/v1/users               - List users (admin only)
POST   /api/v1/users               - Create user (admin only)
PUT    /api/v1/users/{id}          - Update user (admin only)

Roles:
- ADMIN: Full access (create/edit/delete routes, manage users, API keys)
- OPERATOR: Create/edit routes, view metrics
- VIEWER: Read-only access
```

#### 4. API Key Management
```java
POST   /api/v1/api-keys            - Generate API key for route
GET    /api/v1/api-keys            - List API keys
PUT    /api/v1/api-keys/{id}       - Update validity period
DELETE /api/v1/api-keys/{id}       - Revoke API key
```

#### 5. Metrics & Observability API
```java
GET    /api/v1/metrics/routes              - Route-level metrics
GET    /api/v1/metrics/routes/{id}         - Single route metrics
GET    /api/v1/metrics/runtime/health      - Runtime service health
GET    /api/v1/audit-logs                  - Audit trail
```

#### 6. Preview & Testing API
```java
POST   /api/v1/preview/transform    - Test transformation with sample data
POST   /api/v1/preview/route        - Test full route flow (dry-run)
```

### Database Schema:

```sql
-- Dashboard Users (separate from runtime API keys)
CREATE TABLE dashboard_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role ENUM('ADMIN', 'OPERATOR', 'VIEWER') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Audit Log
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES dashboard_users(id)
);

-- Routes and API Keys tables can be shared with runtime-service
```

## Dashboard Frontend (React + TypeScript)

### Technology Stack:
- React 18
- TypeScript
- Vite (build tool)
- React Router (navigation)
- TanStack Query (data fetching)
- Zustand (state management)
- Ant Design / Material-UI (component library)
- Recharts (metrics visualization)
- Monaco Editor (YAML editing)

### Pages & Features:

#### 1. Login Page
- JWT-based authentication
- Role-based redirect

#### 2. Routes Management
- Table view of all routes
- Filter by status (active/inactive), mode (ACTIVE/PASSIVE)
- Create/Edit/Delete buttons
- Quick actions: Activate/Deactivate, Publish

#### 3. Route Editor
- **Step 1: Basic Info**
  - Route ID
  - Inbound Format (JSON, XML, SOAP)
  - Outbound Format (ISO XML)
  - Mode (ACTIVE/PASSIVE)
  - Endpoint URL (for ACTIVE)
  - Encryption Type (None, AES, PGP)

- **Step 2: Request Mapping**
  - Visual drag-and-drop or form-based field mapper
  - Source fields → ISO 20022 fields
  - Add/remove mappings
  - Transformation functions (if any)

- **Step 3: Response Mapping**
  - ISO 20022 fields → Source fields
  - Similar to request mapping

- **Step 4: Preview & Test**
  - Input sample source data
  - Preview generated ISO XML
  - Test response transformation
  - Validate YAML

- **Step 5: Review & Publish**
  - Review generated YAML
  - Edit manually if needed (Monaco editor)
  - Publish to runtime service

#### 4. API Keys Management
- List all API keys
- Generate new key
- Set validity period
- Revoke keys
- Copy key to clipboard

#### 5. Metrics Dashboard
- **Overview**:
  - Total requests today/week/month
  - Success rate
  - Average latency
  - Active routes count

- **Route-Level Metrics**:
  - Requests per second
  - Error rate
  - Latency percentiles (p50, p95, p99)
  - Recent errors

- **Charts**:
  - Request volume over time
  - Success/failure ratio
  - Latency trends

#### 6. User Management (Admin only)
- List users
- Create/Edit users
- Assign roles
- Activate/Deactivate users

#### 7. Audit Log (Admin only)
- Searchable/filterable log
- Show who did what and when
- Export to CSV

### UI Components:

```tsx
// Route List
<RouteTable 
  routes={routes}
  onEdit={handleEdit}
  onDelete={handleDelete}
  onToggleActive={handleToggle}
/>

// Mapping Builder
<MappingBuilder
  sourceFields={sourceFields}
  isoFields={isoFields}
  mappings={mappings}
  onAddMapping={handleAdd}
  onRemoveMapping={handleRemove}
/>

// Preview Panel
<PreviewPanel
  sourceData={sampleInput}
  isoOutput={previewOutput}
  onTest={handlePreview}
/>

// Metrics Chart
<MetricsChart
  data={metricsData}
  type="line|bar|pie"
/>
```

## Implementation Phases

### Phase 1: Dashboard Backend Foundation
1. ✅ Set up Spring Boot project
2. ✅ Configure database and JPA entities
3. ✅ Implement authentication (JWT)
4. ✅ Implement RBAC
5. ✅ Create Route CRUD API
6. ✅ Create User Management API

### Phase 2: Mapping & YAML Generation
1. ✅ Implement YAML generation from mapping definition
2. ✅ Implement validation engine
3. ✅ Create preview/test API
4. ✅ Integrate with runtime-service for publishing

### Phase 3: Dashboard Frontend Foundation
1. ✅ Set up React + TypeScript + Vite project
2. ✅ Configure routing and authentication
3. ✅ Create layout and navigation
4. ✅ Implement login page
5. ✅ Create route management UI

### Phase 4: Mapping Builder UI
1. ✅ Create visual mapping builder
2. ✅ Implement preview functionality
3. ✅ Add YAML editor
4. ✅ Connect to backend APIs

### Phase 5: Metrics & Observability
1. ✅ Implement metrics API
2. ✅ Create metrics dashboard UI
3. ✅ Add charts and visualizations
4. ✅ Implement audit log viewer

### Phase 6: Polish & Production
1. ✅ Add error handling
2. ✅ Implement loading states
3. ✅ Add form validation
4. ✅ Write documentation
5. ✅ Add tests
6. ✅ Deploy and configure

## Next Steps

1. **Start with Dashboard Backend** - create Spring Boot project structure
2. **Set up database schema** - users, routes, audit log
3. **Implement authentication** - JWT-based auth with Spring Security
4. **Create core APIs** - routes, mappings, users
5. **Build React frontend** - modern UI with TypeScript

## Questions to Clarify

1. **Database**: Use the same MySQL as runtime-service or separate database?
2. **Authentication**: OAuth2/OIDC integration or simple JWT?
3. **Deployment**: Separate services or monorepo?
4. **UI Library**: Ant Design, Material-UI, or custom?

Shall I start implementing? Which component would you like me to begin with?
- **A) Dashboard Backend** (Spring Boot)
- **B) Dashboard Frontend** (React)
- **C) Database Schema** (SQL setup)




