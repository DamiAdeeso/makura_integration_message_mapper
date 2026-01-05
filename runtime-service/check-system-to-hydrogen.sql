-- Check if SYSTEM_TO_HYDROGEN route exists in makura_runtime
USE makura_runtime;

-- 1. Check if route exists
SELECT 
    id,
    route_id,
    inbound_format,
    outbound_format,
    mode,
    active,
    yaml_profile_path
FROM routes
WHERE route_id = 'SYSTEM_TO_HYDROGEN';

-- 2. Check if API key exists for this route
SELECT 
    id,
    route_id,
    LEFT(key_hash, 20) AS key_hash_preview,
    valid_from,
    valid_until,
    active,
    CASE 
        WHEN NOW() BETWEEN valid_from AND valid_until AND active = 1 THEN 'VALID'
        WHEN NOW() < valid_from THEN 'NOT_YET_VALID'
        WHEN NOW() > valid_until THEN 'EXPIRED'
        WHEN active = 0 THEN 'INACTIVE'
        ELSE 'INVALID'
    END AS status
FROM api_keys
WHERE route_id = 'SYSTEM_TO_HYDROGEN';

-- 3. If route doesn't exist, create it
-- INSERT INTO routes (route_id, inbound_format, outbound_format, mode, yaml_profile_path, active, created_at, updated_at)
-- VALUES (
--     'SYSTEM_TO_HYDROGEN',
--     'XML',
--     'ISO_XML',
--     'PASSIVE',
--     'SYSTEM_TO_HYDROGEN.yaml',
--     TRUE,
--     NOW(),
--     NOW()
-- );

-- 4. To create an API key, you need to:
--    a. Generate an API key (e.g., "mak_1234567890abcdef")
--    b. Hash it using SHA-256
--    c. Insert into api_keys table
-- 
-- Example (replace 'YOUR_API_KEY_HERE' with actual key):
-- INSERT INTO api_keys (route_id, key_hash, valid_from, valid_until, active, created_at, updated_at)
-- VALUES (
--     'SYSTEM_TO_HYDROGEN',
--     SHA2('YOUR_API_KEY_HERE', 256),  -- MySQL SHA-256 hash
--     NOW(),
--     DATE_ADD(NOW(), INTERVAL 1 YEAR),
--     TRUE,
--     NOW(),
--     NOW()
-- );



