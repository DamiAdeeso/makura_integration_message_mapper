# Route Seeder Guide

## 🌱 **What Was Created**

A comprehensive route seeder that automatically populates your dashboard with **8 realistic sample routes** on first startup.

**File:** `src/main/java/com/makura/dashboard/config/RouteSeeder.java`

---

## 📦 **8 Sample Routes Created**

### **Payment Routes (3)**

#### 1. **CREDIT_TRANSFER_PACS008** ✅
- **Name:** Customer Credit Transfer (pacs.008)
- **Type:** ACTIVE (forwards to runtime service)
- **Flow:** JSON → ISO 20022 XML → Runtime Service
- **Endpoint:** `http://localhost:8080/api/v1/translate/CREDIT_TRANSFER_PACS008`
- **Status:** Active & Published
- **Use Case:** Standard payment transfers between accounts

#### 2. **PAYMENT_STATUS_PACS002** ✅
- **Name:** Payment Status Report (pacs.002)
- **Type:** PASSIVE (returns ISO only, no forwarding)
- **Flow:** JSON → ISO 20022 XML
- **Status:** Active & Published
- **Use Case:** Payment status updates and confirmations

#### 3. **DIRECT_DEBIT_PAIN008** ⚠️
- **Name:** Direct Debit Initiation (pain.008)
- **Type:** ACTIVE
- **Flow:** JSON → ISO 20022 XML → Runtime Service
- **Endpoint:** `http://localhost:8080/api/v1/translate/DIRECT_DEBIT_PAIN008`
- **Status:** Active but NOT Published (draft)
- **Use Case:** Direct debit instructions

---

### **Account Routes (2)**

#### 4. **ACCOUNT_STATEMENT_CAMT053** ✅
- **Name:** Account Statement (camt.053)
- **Type:** PASSIVE
- **Flow:** JSON → ISO 20022 XML
- **Status:** Active & Published
- **Use Case:** Bank account statements

#### 5. **BALANCE_REPORT_CAMT052** 🔒
- **Name:** Account Balance Report (camt.052)
- **Type:** ACTIVE with AES Encryption
- **Flow:** JSON → ISO 20022 XML → Runtime Service (encrypted)
- **Endpoint:** `http://localhost:8080/api/v1/translate/BALANCE_REPORT_CAMT052`
- **Encryption:** AES with key reference "balance-key-001"
- **Status:** Active & Published
- **Use Case:** Encrypted balance reporting

---

### **Securities Routes (1)**

#### 6. **SECURITIES_SETTLEMENT_SESE023** 🔐
- **Name:** Securities Settlement (sese.023)
- **Type:** ACTIVE with PGP Encryption
- **Flow:** XML → ISO 20022 XML → Runtime Service (PGP encrypted)
- **Endpoint:** `http://localhost:8080/api/v1/translate/SECURITIES_SETTLEMENT_SESE023`
- **Encryption:** PGP with key "securities-pgp-key"
- **Status:** INACTIVE & Not Published (example only)
- **Use Case:** Secure securities settlement

---

### **Legacy & Demo Routes (2)**

#### 7. **LEGACY_SOAP_BRIDGE** ✅
- **Name:** Legacy SOAP to ISO Bridge
- **Type:** ACTIVE
- **Flow:** SOAP → ISO 20022 XML → Runtime Service
- **Endpoint:** `http://localhost:8080/api/v1/translate/LEGACY_SOAP_BRIDGE`
- **Status:** Active & Published
- **Use Case:** Bridge old SOAP systems to modern ISO 20022

#### 8. **DEMO_TEST_ROUTE** 🧪
- **Name:** Demo & Testing Route
- **Type:** PASSIVE
- **Flow:** JSON → JSON (no transformation)
- **Status:** Active but NOT Published
- **Use Case:** Testing and demonstrations

---

## 🔄 **How It Works**

### **Automatic Seeding:**
```java
@Order(2)  // Runs after DataInitializer (users/roles/permissions)
```

1. Backend starts up
2. DataInitializer creates users, roles, permissions
3. RouteSeeder checks if routes exist
4. If database is empty → creates 8 sample routes
5. If routes already exist → skips seeding

### **Smart Detection:**
```java
if (routeRepository.count() > 0) {
    log.info("Routes already exist, skipping seeding");
    return;
}
```

---

## 🎯 **Runtime Service Integration**

All ACTIVE routes point to the runtime service:
```java
private static final String RUNTIME_SERVICE_URL = "http://localhost:8080/api/v1/translate";
```

### **Example Flow:**
```
Frontend → Dashboard API → Runtime Service → ISO 20022 Translation
         (8081)            (8080)
```

1. User creates/tests a route in dashboard (port 8081)
2. Dashboard stores configuration in database
3. Runtime service reads YAML files from:
   ```
   C:\Users\USER\Documents\Makura\runtime-service\mappings
   ```
4. Translation happens at:
   ```
   http://localhost:8080/api/v1/translate/{routeId}
   ```

---

## 🚀 **What To Do Next**

### **Step 1: Restart Backend**
```bash
# Stop current backend (Ctrl+C)
cd dashboard-backend
mvn spring-boot:run
```

### **Step 2: Check Logs**
You should see:
```
=== Seeding Sample Routes ===
✓ Created route: CREDIT_TRANSFER_PACS008
✓ Created route: PAYMENT_STATUS_PACS002
✓ Created route: DIRECT_DEBIT_PAIN008
✓ Created route: ACCOUNT_STATEMENT_CAMT053
✓ Created route: BALANCE_REPORT_CAMT052
✓ Created route: SECURITIES_SETTLEMENT_SESE023
✓ Created route: LEGACY_SOAP_BRIDGE
✓ Created route: DEMO_TEST_ROUTE
=== Seeded 8 sample routes ===
```

### **Step 3: Refresh Dashboard**
1. Open browser: `http://localhost:5173`
2. Login: `admin` / `admin123`
3. Navigate to **Routes** page
4. You should see **8 routes** in the table! 🎉

---

## 📊 **Route Statistics**

```
Total Routes:        8
├─ Active:           7 (87.5%)
├─ Inactive:         1 (12.5%)
├─ Published:        5 (62.5%)
├─ Draft:            3 (37.5%)
├─ ACTIVE Mode:      5 (forward to runtime)
├─ PASSIVE Mode:     3 (return ISO only)
├─ No Encryption:    6
├─ AES Encrypted:    1
└─ PGP Encrypted:    1
```

---

## 🎨 **Visual Legend**

| Symbol | Meaning |
|--------|---------|
| ✅ | Active & Published (ready for production) |
| ⚠️ | Active but Draft (testing phase) |
| 🔒 | AES Encrypted |
| 🔐 | PGP Encrypted |
| 🧪 | Demo/Testing only |

---

## 🔧 **Customization**

### **Change Runtime Service URL:**
Edit line 20 in `RouteSeeder.java`:
```java
private static final String RUNTIME_SERVICE_URL = "http://your-runtime-service:8080/api/v1/translate";
```

### **Add More Routes:**
Add methods to `RouteSeeder.java`:
```java
private void createCustomRoutes() {
    RouteConfig customRoute = RouteConfig.builder()
            .routeId("MY_CUSTOM_ROUTE")
            .name("My Custom Route")
            // ... more config
            .build();
    routeRepository.save(customRoute);
}
```

Call it in `run()` method:
```java
createPaymentRoutes();
createAccountRoutes();
createSecuritiesRoutes();
createCustomRoutes();  // ← Add this
```

### **Delete All Routes (Reset):**
```sql
-- In MySQL
USE makura_dashboard;
TRUNCATE TABLE routes;

-- Restart backend to re-seed
```

---

## ✅ **Ready!**

**Restart your backend now** and the routes will be automatically created! 🚀

The dashboard will come alive with realistic sample data for:
- ✅ Payments (pacs.008, pacs.002, pain.008)
- ✅ Account Reporting (camt.053, camt.052)
- ✅ Securities (sese.023)
- ✅ Legacy SOAP integration
- ✅ Demo/Testing

**All routes are production-ready examples based on real ISO 20022 message types!**



