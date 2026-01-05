-- Complete setup script - creates database, tables, and test data
-- Run this single script to set up everything

-- Create database
CREATE DATABASE IF NOT EXISTS makura_runtime;
USE makura_runtime;

-- Create routes table
CREATE TABLE IF NOT EXISTS routes (
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
CREATE TABLE IF NOT EXISTS api_keys (
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
    ('XML_TO_ISO', 'XML', 'ISO_XML', 'ACTIVE', 'https://payment-gateway.example.com/process', true, NOW(), NOW()),
    ('SOAP_TO_ISO', 'SOAP', 'ISO_XML', 'PASSIVE', NULL, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Insert test API keys with real SHA-256 hashes
INSERT INTO api_keys (route_id, key_hash, valid_from, valid_until, active, created_at, updated_at)
VALUES 
    (
        'SYSTEM_TO_NIP',
        '53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097',  -- SHA-256 of "test-api-key-456"
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
    ),
    (
        'SOAP_TO_ISO',
        '6f98b08de391e2f43ae71dd4d51c60d9efa426610e07049b25919a1614babeae',  -- SHA-256 of "soap-api-key-789"
        NOW(),
        DATE_ADD(NOW(), INTERVAL 1 YEAR),
        true,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Verify inserted data
SELECT 'Routes inserted:' AS Status;
SELECT * FROM routes WHERE route_id IN ('SYSTEM_TO_NIP', 'SYSTEM_TO_NIP_PASSIVE', 'XML_TO_ISO', 'SOAP_TO_ISO');

SELECT 'API Keys inserted:' AS Status;
SELECT route_id, LEFT(key_hash, 20) AS key_hash_preview, valid_from, valid_until, active 
FROM api_keys 
WHERE route_id IN ('SYSTEM_TO_NIP', 'SYSTEM_TO_NIP_PASSIVE', 'XML_TO_ISO', 'SOAP_TO_ISO');

