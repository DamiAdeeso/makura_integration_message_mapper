package com.makura.dashboard.controller;

import com.makura.dashboard.dto.ApiKeyDTO;
import com.makura.dashboard.dto.ApiKeyResponse;
import com.makura.dashboard.dto.CreateApiKeyRequest;
import com.makura.dashboard.dto.UpdateApiKeyRequest;
import com.makura.dashboard.security.RequiresPermission;
import com.makura.dashboard.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing API keys
 */
@Slf4j
@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "API key management for developers")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    @GetMapping
    @RequiresPermission("api-keys:view")
    @Operation(summary = "Get all API keys", description = "Retrieve all API keys (masked)")
    public ResponseEntity<List<ApiKeyDTO>> getAllApiKeys(
            @RequestParam(required = false) String routeId) {
        
        List<ApiKeyDTO> keys = routeId != null
                ? apiKeyService.getApiKeysByRoute(routeId)
                : apiKeyService.getAllApiKeys();
        
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/{id}")
    @RequiresPermission("api-keys:view")
    @Operation(summary = "Get API key by ID", description = "Retrieve a specific API key (masked)")
    public ResponseEntity<ApiKeyDTO> getApiKeyById(@PathVariable Long id) {
        ApiKeyDTO apiKey = apiKeyService.getApiKeyById(id);
        return ResponseEntity.ok(apiKey);
    }

    @PostMapping
    @RequiresPermission("api-keys:create")
    @Operation(summary = "Create API key", description = "Generate a new API key. The full key is shown only once.")
    public ResponseEntity<ApiKeyResponse> createApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        log.info("Creating new API key for route: {}", request.getRouteId());
        ApiKeyResponse response = apiKeyService.createApiKey(request, getCurrentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @RequiresPermission("api-keys:update")
    @Operation(summary = "Update API key", description = "Update API key details (description, expiration, active status)")
    public ResponseEntity<ApiKeyDTO> updateApiKey(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApiKeyRequest request) {
        log.info("Updating API key: {}", id);
        ApiKeyDTO updated = apiKeyService.updateApiKey(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("api-keys:delete")
    @Operation(summary = "Delete API key", description = "Permanently delete an API key")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        log.info("Deleting API key: {}", id);
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/regenerate")
    @RequiresPermission("api-keys:create")
    @Operation(summary = "Regenerate API key", description = "Create a new API key and deactivate the old one")
    public ResponseEntity<ApiKeyResponse> regenerateApiKey(@PathVariable Long id) {
        log.info("Regenerating API key: {}", id);
        ApiKeyResponse response = apiKeyService.regenerateApiKey(id, getCurrentUsername());
        return ResponseEntity.ok(response);
    }
}



