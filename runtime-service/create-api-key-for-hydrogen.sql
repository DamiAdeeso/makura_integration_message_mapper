-- Create API key for SYSTEM_TO_HYDROGEN route
-- Replace 'mak_YOUR_API_KEY_HERE' with your actual API key
USE makura_runtime;

-- Option 1: Create API key with a specific key value
-- Replace 'mak_test1234567890abcdef' with your desired API key
SET @api_key = 'mak_test1234567890abcdef';
SET @route_id = 'SYSTEM_TO_HYDROGEN';

-- Insert API key (MySQL will hash it using SHA2)
INSERT INTO api_keys (route_id, key_hash, valid_from, valid_until, active, created_at, updated_at)
VALUES (
    @route_id,
    SHA2(@api_key, 256),  -- SHA-256 hash
    NOW(),
    DATE_ADD(NOW(), INTERVAL 1 YEAR),  -- Valid for 1 year
    TRUE,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    key_hash = SHA2(@api_key, 256),
    valid_until = DATE_ADD(NOW(), INTERVAL 1 YEAR),
    active = TRUE,
    updated_at = NOW();

-- Verify the key was created
SELECT 
    route_id,
    LEFT(key_hash, 20) AS key_hash_preview,
    valid_from,
    valid_until,
    active,
    'Use this API key in your requests: ' AS note,
    @api_key AS your_api_key
FROM api_keys
WHERE route_id = @route_id;



