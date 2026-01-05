package com.makura.runtime.auth;

import com.makura.runtime.model.ApiKey;
import com.makura.runtime.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

/**
 * API Key validation and licensing enforcement
 */
@Slf4j
@Component
public class ApiKeyValidator {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyValidator(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    /**
     * Validate API key for a route
     */
    public boolean validateApiKey(String routeId, String apiKey) {
        if (routeId == null || apiKey == null || apiKey.isEmpty()) {
            log.warn("Invalid API key validation request: routeId={}, apiKey present={}", 
                routeId, apiKey != null);
            return false;
        }

        try {
            // Trim whitespace from API key
            String trimmedKey = apiKey.trim();
            String keyHash = hashApiKey(trimmedKey);
            LocalDateTime now = LocalDateTime.now();
            
            boolean isValid = apiKeyRepository.findValidKey(routeId, keyHash, now).isPresent();
            
            if (!isValid) {
                log.debug("API key validation failed for routeId: {}, provided hash: {}, now: {}", 
                    routeId, keyHash, now);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error validating API key for routeId: {}", routeId, e);
            return false;
        }
    }

    /**
     * Hash API key for storage/comparison (SHA-256)
     */
    private String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

