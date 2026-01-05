-- Create database and tables for runtime service
-- Run this first before setup-test-data.sql

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

