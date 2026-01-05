-- Check API keys in database
USE makura_runtime;

SELECT 
    routeId, 
    LEFT(keyHash, 20) AS keyHash_preview,
    validFrom,
    validUntil,
    active,
    CASE 
        WHEN NOW() BETWEEN validFrom AND validUntil THEN 'VALID'
        ELSE 'EXPIRED'
    END AS status
FROM api_keys
WHERE routeId IN ('SYSTEM_TO_NIP', 'SYSTEM_TO_NIP_PASSIVE', 'XML_TO_ISO');




