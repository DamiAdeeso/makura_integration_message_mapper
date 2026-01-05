import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utility to generate SHA-256 hash for API keys
 * Run this to get the hash for inserting into database
 */
public class GenerateApiKeyHash {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GenerateApiKeyHash <api-key>");
            System.out.println("Example: java GenerateApiKeyHash mak_test1234567890abcdef");
            System.exit(1);
        }
        
        String apiKey = args[0];
        String hash = hashApiKey(apiKey);
        
        System.out.println("API Key: " + apiKey);
        System.out.println("SHA-256 Hash: " + hash);
        System.out.println("\nSQL to insert:");
        System.out.println("INSERT INTO api_keys (route_id, key_hash, valid_from, valid_until, active, created_at, updated_at)");
        System.out.println("VALUES (");
        System.out.println("    'SYSTEM_TO_HYDROGEN',");
        System.out.println("    '" + hash + "',");
        System.out.println("    NOW(),");
        System.out.println("    DATE_ADD(NOW(), INTERVAL 1 YEAR),");
        System.out.println("    TRUE,");
        System.out.println("    NOW(),");
        System.out.println("    NOW()");
        System.out.println(");");
    }
    
    private static String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }
}



