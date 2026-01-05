package com.makura.dashboard.service;

import com.makura.dashboard.dto.ApiKeyDTO;
import com.makura.dashboard.dto.ApiKeyResponse;
import com.makura.dashboard.dto.CreateApiKeyRequest;
import com.makura.dashboard.dto.UpdateApiKeyRequest;
import com.makura.dashboard.runtime.model.ApiKey;
import com.makura.dashboard.runtime.repository.ApiKeyRepository;
import com.makura.dashboard.runtime.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing API keys
 * Writes directly to makura_runtime database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final RouteRepository routeRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String KEY_PREFIX = "mak_";

    /**
     * Generate a secure random API key
     */
    private String generateApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomString = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return KEY_PREFIX + randomString;
    }

    /**
     * Hash API key using SHA-256
     */
    private String hashApiKey(String apiKey) {
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

    /**
     * Extract prefix and suffix for display
     */
    private String[] extractKeyParts(String apiKey) {
        if (apiKey == null || apiKey.length() < 12) {
            return new String[]{"", ""};
        }
        String withoutPrefix = apiKey.startsWith(KEY_PREFIX) 
            ? apiKey.substring(KEY_PREFIX.length()) 
            : apiKey;
        
        String prefix = withoutPrefix.length() >= 8 
            ? withoutPrefix.substring(0, 8) 
            : withoutPrefix.substring(0, Math.min(4, withoutPrefix.length()));
        String suffix = withoutPrefix.length() >= 4 
            ? withoutPrefix.substring(withoutPrefix.length() - 4) 
            : "";
        
        return new String[]{prefix, suffix};
    }

    /**
     * Get all API keys
     */
    public List<ApiKeyDTO> getAllApiKeys() {
        return apiKeyRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get API keys by route
     */
    public List<ApiKeyDTO> getApiKeysByRoute(String routeId) {
        return apiKeyRepository.findByRouteId(routeId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get API key by ID
     */
    public ApiKeyDTO getApiKeyById(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API key not found with id: " + id));
        return toDTO(apiKey);
    }

    /**
     * Create a new API key
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public ApiKeyResponse createApiKey(CreateApiKeyRequest request, String createdBy) {
        // Validate route exists
        routeRepository.findByRouteId(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found: " + request.getRouteId()));

        // Validate dates
        if (request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new IllegalArgumentException("Valid until date must be after valid from date");
        }

        // Generate API key
        String apiKey = generateApiKey();
        String keyHash = hashApiKey(apiKey);

        // Create entity (runtime ApiKey doesn't have keyPrefix, keySuffix, description, createdBy)
        ApiKey entity = ApiKey.builder()
                .routeId(request.getRouteId())
                .keyHash(keyHash)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .active(true)
                .build();

        ApiKey saved = apiKeyRepository.save(entity);
        log.info("Created API key in runtime database for route: {} by user: {}", request.getRouteId(), createdBy);

        // Return response with full key (only time it's shown)
        return ApiKeyResponse.builder()
                .id(saved.getId())
                .routeId(saved.getRouteId())
                .apiKey(apiKey)
                .description(request.getDescription()) // Store in response, not DB
                .validFrom(saved.getValidFrom())
                .validUntil(saved.getValidUntil())
                .createdBy(createdBy) // Store in response, not DB
                .createdAt(saved.getCreatedAt())
                .warning("⚠️ This key will not be shown again. Please copy it now and store it securely.")
                .build();
    }

    /**
     * Update API key
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public ApiKeyDTO updateApiKey(Long id, UpdateApiKeyRequest request) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API key not found with id: " + id));

        // Runtime ApiKey doesn't have description field
        if (request.getValidFrom() != null) {
            apiKey.setValidFrom(request.getValidFrom());
        }
        if (request.getValidUntil() != null) {
            apiKey.setValidUntil(request.getValidUntil());
        }
        if (request.getActive() != null) {
            apiKey.setActive(request.getActive());
        }

        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("Updated API key in runtime database: {}", id);
        return toDTO(updated);
    }

    /**
     * Delete API key
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public void deleteApiKey(Long id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new RuntimeException("API key not found with id: " + id);
        }
        apiKeyRepository.deleteById(id);
        log.info("Deleted API key from runtime database: {}", id);
    }

    /**
     * Regenerate API key (creates new key, deactivates old one)
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public ApiKeyResponse regenerateApiKey(Long id, String createdBy) {
        ApiKey oldKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API key not found with id: " + id));

        // Deactivate old key
        oldKey.setActive(false);
        apiKeyRepository.save(oldKey);

        // Create new key with same route and similar expiration
        CreateApiKeyRequest request = CreateApiKeyRequest.builder()
                .routeId(oldKey.getRouteId())
                .description("Regenerated API key") // Runtime doesn't store description
                .validFrom(LocalDateTime.now())
                .validUntil(oldKey.getValidUntil())
                .build();

        return createApiKey(request, createdBy);
    }

    /**
     * Convert entity to DTO
     */
    private ApiKeyDTO toDTO(ApiKey apiKey) {
        LocalDateTime now = LocalDateTime.now();
        boolean expired = apiKey.getValidUntil().isBefore(now);
        boolean expiringSoon = !expired && apiKey.getValidUntil().isBefore(now.plusDays(30));

        // Runtime ApiKey doesn't have keyPrefix/keySuffix, so we can't show masked key
        // We'll show a generic masked key
        String maskedKey = KEY_PREFIX + "****";

        return ApiKeyDTO.builder()
                .id(apiKey.getId())
                .routeId(apiKey.getRouteId())
                .maskedKey(maskedKey)
                .description(null) // Runtime doesn't store description
                .validFrom(apiKey.getValidFrom())
                .validUntil(apiKey.getValidUntil())
                .active(apiKey.getActive())
                .expired(expired)
                .expiringSoon(expiringSoon)
                .createdBy(null) // Runtime doesn't store createdBy
                .createdAt(apiKey.getCreatedAt())
                .updatedAt(apiKey.getUpdatedAt())
                .lastUsedAt(null) // Runtime doesn't track lastUsedAt
                .build();
    }
}

