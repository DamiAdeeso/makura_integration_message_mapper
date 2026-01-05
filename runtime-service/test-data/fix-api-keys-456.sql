-- Fix API keys to use test-api-key-456 for both routes
USE makura_runtime;

-- Update SYSTEM_TO_NIP to use test-api-key-456
-- Hash: 53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097
UPDATE api_keys 
SET key_hash = '53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097',
    updated_at = NOW()
WHERE route_id = 'SYSTEM_TO_NIP';

-- Verify the update
SELECT route_id, LEFT(key_hash, 20) AS key_hash_preview, active, valid_from, valid_until 
FROM api_keys 
WHERE route_id IN ('SYSTEM_TO_NIP', 'SYSTEM_TO_NIP_PASSIVE');




