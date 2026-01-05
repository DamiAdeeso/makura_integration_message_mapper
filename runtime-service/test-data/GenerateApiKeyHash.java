// Utility to generate SHA-256 hash for API keys
// Compile: javac GenerateApiKeyHash.java
// Run: java GenerateApiKeyHash "your-api-key"

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class GenerateApiKeyHash {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java GenerateApiKeyHash <api-key>");
            System.exit(1);
        }
        
        String apiKey = args[0];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            String hashHex = bytesToHex(hash);
            System.out.println("API Key: " + apiKey);
            System.out.println("SHA-256 Hash: " + hashHex);
            System.out.println("\nSQL INSERT statement:");
            System.out.println("INSERT INTO api_keys (routeId, keyHash, validFrom, validUntil, active, createdAt, updatedAt)");
            System.out.println("VALUES ('YOUR_ROUTE_ID', '" + hashHex + "', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), true, NOW(), NOW());");
        } catch (Exception e) {
            System.err.println("Error generating hash: " + e.getMessage());
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}




