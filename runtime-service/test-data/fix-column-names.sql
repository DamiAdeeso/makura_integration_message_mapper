-- Fix column names to match entity mappings (snake_case)
-- Run this if your tables have camelCase columns

USE makura_runtime;

-- Fix routes table
ALTER TABLE routes 
    CHANGE COLUMN routeId route_id VARCHAR(100),
    CHANGE COLUMN inboundFormat inbound_format VARCHAR(50),
    CHANGE COLUMN outboundFormat outbound_format VARCHAR(50),
    CHANGE COLUMN encryptionType encryption_type VARCHAR(50),
    CHANGE COLUMN encryptionKeyRef encryption_key_ref VARCHAR(200),
    CHANGE COLUMN yamlProfilePath yaml_profile_path VARCHAR(100),
    CHANGE COLUMN createdAt created_at DATETIME,
    CHANGE COLUMN updatedAt updated_at DATETIME;

-- Fix api_keys table
ALTER TABLE api_keys
    CHANGE COLUMN routeId route_id VARCHAR(100),
    CHANGE COLUMN keyHash key_hash VARCHAR(255),
    CHANGE COLUMN validFrom valid_from DATETIME,
    CHANGE COLUMN validUntil valid_until DATETIME,
    CHANGE COLUMN createdAt created_at DATETIME,
    CHANGE COLUMN updatedAt updated_at DATETIME;

-- Recreate indexes
DROP INDEX idx_route_id ON routes;
DROP INDEX idx_route_id ON api_keys;
DROP INDEX idx_key_hash ON api_keys;
DROP INDEX idx_valid_until ON api_keys;

CREATE INDEX idx_route_id ON routes(route_id);
CREATE INDEX idx_route_id ON api_keys(route_id);
CREATE INDEX idx_key_hash ON api_keys(key_hash);
CREATE INDEX idx_valid_until ON api_keys(valid_until);

-- Verify
DESCRIBE routes;
DESCRIBE api_keys;




