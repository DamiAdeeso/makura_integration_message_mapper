-- Complete database reset script
-- WARNING: This will DELETE all data and recreate the database from scratch

DROP DATABASE IF EXISTS makura_runtime;
CREATE DATABASE makura_runtime;
USE makura_runtime;

-- Create routes table
CREATE TABLE routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id VARCHAR(100) NOT NULL UNIQUE,
    inbound_format VARCHAR(50) NOT NULL,
    outbound_format VARCHAR(50) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    endpoint VARCHAR(500),
    encryption_type VARCHAR(50),
    encryption_key_ref VARCHAR(200),
    yaml_profile_path VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_route_id (route_id),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create api_keys table
CREATE TABLE api_keys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id VARCHAR(100) NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    valid_from DATETIME NOT NULL,
    valid_until DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_route_id (route_id),
    INDEX idx_key_hash (key_hash),
    INDEX idx_valid_until (valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert test routes
INSERT INTO routes (route_id, inbound_format, outbound_format, mode, endpoint, active, created_at, updated_at)
VALUES 
    ('SYSTEM_TO_NIP', 'JSON', 'ISO_XML', 'ACTIVE', 'https://nip.bank/api/payments', true, NOW(), NOW()),
    ('SYSTEM_TO_NIP_PASSIVE', 'JSON', 'ISO_XML', 'PASSIVE', NULL, true, NOW(), NOW()),
    ('XML_TO_ISO', 'XML', 'ISO_XML', 'ACTIVE', 'https://payment-gateway.example.com/process', true, NOW(), NOW());

-- Insert test API keys with real SHA-256 hashes
-- Hash for "test-api-key-123": a2e4ab0472c808a1ff2ce147ae4f6cd9ecd8bcc8a49c48350f97e6811ace7464
-- Hash for "test-api-key-456": 53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097
-- Hash for "xml-api-key-789": 42e77bbbc3406667df7346573c6e27cc2d38987e7b9549c423e97931f3cf67d2

INSERT INTO api_keys (route_id, key_hash, valid_from, valid_until, active, created_at, updated_at)
VALUES 
    (
        'SYSTEM_TO_NIP',
        'a2e4ab0472c808a1ff2ce147ae4f6cd9ecd8bcc8a49c48350f97e6811ace7464',
        NOW(),
        DATE_ADD(NOW(), INTERVAL 1 YEAR),
        true,
        NOW(),
        NOW()
    ),
    (
        'SYSTEM_TO_NIP_PASSIVE',
        '53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097',
        NOW(),
        DATE_ADD(NOW(), INTERVAL 1 YEAR),
        true,
        NOW(),
        NOW()
    ),
    (
        'XML_TO_ISO',
        '42e77bbbc3406667df7346573c6e27cc2d38987e7b9549c423e97931f3cf67d2',
        NOW(),
        DATE_ADD(NOW(), INTERVAL 1 YEAR),
        true,
        NOW(),
        NOW()
    );

-- Verify inserted data
SELECT '=== Routes ===' AS Status;
SELECT route_id, inbound_format, outbound_format, mode, endpoint, active FROM routes;

SELECT '=== API Keys ===' AS Status;
SELECT route_id, LEFT(key_hash, 20) AS key_hash_preview, valid_from, valid_until, active FROM api_keys;

SELECT '=== Setup Complete ===' AS Status;




