-- Fix API keys - update with correct hashes if needed
USE makura_runtime;

-- Verify current hashes
SELECT routeId, keyHash, active, validFrom, validUntil FROM api_keys;

-- If hashes are wrong, update them:
-- Hash for "test-api-key-123": a2e4ab0472c808a1ff2ce147ae4f6cd9ecd8bcc8a49c48350f97e6811ace7464
-- Hash for "test-api-key-456": 53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097
-- Hash for "xml-api-key-789": 42e77bbbc3406667df7346573c6e27cc2d38987e7b9549c423e97931f3cf67d2

UPDATE api_keys 
SET keyHash = 'a2e4ab0472c808a1ff2ce147ae4f6cd9ecd8bcc8a49c48350f97e6811ace7464',
    validFrom = NOW(),
    validUntil = DATE_ADD(NOW(), INTERVAL 1 YEAR),
    active = true
WHERE routeId = 'SYSTEM_TO_NIP';

UPDATE api_keys 
SET keyHash = '53d79b29889771d5c3444e286a7040554405f605228a9f9128df5a0fe321c097',
    validFrom = NOW(),
    validUntil = DATE_ADD(NOW(), INTERVAL 1 YEAR),
    active = true
WHERE routeId = 'SYSTEM_TO_NIP_PASSIVE';

UPDATE api_keys 
SET keyHash = '42e77bbbc3406667df7346573c6e27cc2d38987e7b9549c423e97931f3cf67d2',
    validFrom = NOW(),
    validUntil = DATE_ADD(NOW(), INTERVAL 1 YEAR),
    active = true
WHERE routeId = 'XML_TO_ISO';

-- Verify updated
SELECT routeId, LEFT(keyHash, 20) AS keyHash_preview, active FROM api_keys;




