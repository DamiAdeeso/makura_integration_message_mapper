-- Setup script for test data
-- NOTE: This script assumes tables already exist!
-- If tables don't exist, run setup-complete.sql instead, or start the runtime service once
-- to auto-create tables via JPA, then run this script.

USE makura_runtime;

-- Insert test routes
INSERT INTO routes (route_id, inbound_format, outbound_format, mode, endpoint, active, created_at, updated_at)
VALUES 
    ('SYSTEM_TO_NIP', 'JSON', 'ISO_XML', 'ACTIVE', 'https://nip.bank/api/payments', true, NOW(), NOW()),
    ('SYSTEM_TO_NIP_PASSIVE', 'JSON', 'ISO_XML', 'PASSIVE', NULL, true, NOW(), NOW()),
    ('XML_TO_ISO', 'XML', 'ISO_XML', 'ACTIVE', 'https://payment-gateway.example.com/process', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Insert test API keys
-- IMPORTANT: These hash values are PLACEHOLDERS for testing only!
-- In production, generate real SHA-256 hashes using:
--   - Java: MessageDigest.getInstance("SHA-256")
--   - Online tool: https://emn178.github.io/online-tools/sha256.html
--   - Or use the generate-api-key-hash.java utility
--
-- To generate real hashes:
-- 1. cd test-data
-- 2. Compile: javac GenerateApiKeyHash.java
-- 3. Run: java GenerateApiKeyHash "your-api-key"
-- 4. Replace the hash values below with the generated hashes
--
-- Real SHA-256 hashes generated using GenerateApiKeyHash.java
INSERT INTO api_keys (route_id, key_hash, valid_from, valid_until, active, created_at, updated_at)
VALUES 
    (
        'SYSTEM_TO_NIP',
        'a2e4ab0472c808a1ff2ce147ae4f6cd9ecd8bcc8a49c48350f97e6811ace7464',  -- SHA-256 of "test-api-key-123"
        NOW(),
        DATE_ADD(NOW(), INTERVAL 1 YEAR),
        true,
        NOW(),
        NOW()
    ),
    (
        'SYSTEM_TO_NIP_PASSIVE',
        '53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097',  -- SHA-256 of "test-api-key-456"
        NOW(),
        DATE_ADD(NOW(), INTERVAL 1 YEAR),
        true,
        NOW(),
        NOW()
    ),
    (
        'XML_TO_ISO',
        '42e77bbbc3406667df7346573c6e27cc2d38987e7b9549c423e97931f3cf67d2',  -- SHA-256 of "xml-api-key-789"
        NOW(),
        DATE_ADD(NOW(), INTERVAL 1 YEAR),
        true,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Verify inserted data
SELECT * FROM routes WHERE route_id IN ('SYSTEM_TO_NIP', 'SYSTEM_TO_NIP_PASSIVE', 'XML_TO_ISO');
SELECT route_id, key_hash, valid_from, valid_until, active FROM api_keys WHERE route_id IN ('SYSTEM_TO_NIP', 'SYSTEM_TO_NIP_PASSIVE', 'XML_TO_ISO');

