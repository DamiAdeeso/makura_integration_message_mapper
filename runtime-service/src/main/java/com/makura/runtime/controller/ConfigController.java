package com.makura.runtime.controller;

import com.makura.runtime.mapping.MappingLoader;
import com.makura.runtime.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Configuration management endpoints (for dashboard-triggered refresh)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/config")
@Tag(name = "Configuration", description = "Configuration management endpoints for mapping cache refresh")
public class ConfigController {

    private final MappingLoader mappingLoader;
    private final RouteService routeService;

    public ConfigController(MappingLoader mappingLoader, RouteService routeService) {
        this.mappingLoader = mappingLoader;
        this.routeService = routeService;
    }

    /**
     * Refresh mapping configuration cache for a specific route
     * Called by dashboard with proper permissions
     */
    @Operation(
        summary = "Refresh mapping cache for a route",
        description = "Refreshes the cached YAML mapping configuration for a specific route. " +
            "This is typically called by the dashboard after mapping changes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Failed to refresh cache")
    })
    @PostMapping("/refresh/{routeId}")
    public ResponseEntity<?> refreshRouteConfig(
            @Parameter(description = "Route identifier", required = true, example = "SYSTEM_TO_NIP")
            @PathVariable String routeId) {
        try {
            mappingLoader.refreshMappingCache(routeId);
            routeService.evictRouteCache(routeId);
            log.info("Mapping and route cache refreshed for routeId: {}", routeId);
            return ResponseEntity.ok(new RefreshResponse("Cache refreshed successfully", routeId));
        } catch (Exception e) {
            log.error("Error refreshing cache for routeId: {}", routeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to refresh cache: " + e.getMessage()));
        }
    }

    /**
     * Refresh all mapping configurations
     */
    @Operation(
        summary = "Refresh all mapping caches",
        description = "Refreshes all cached YAML mapping configurations. " +
            "This clears the entire mapping cache and forces reload from filesystem."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All caches refreshed successfully"),
        @ApiResponse(responseCode = "500", description = "Failed to refresh caches")
    })
    @PostMapping("/refresh/all")
    @CacheEvict(value = {"mappingConfigs", "routes"}, allEntries = true)
    public ResponseEntity<?> refreshAllConfigs() {
        try {
            log.info("All mapping and route caches refreshed");
            return ResponseEntity.ok(new RefreshResponse("All caches refreshed successfully", null));
        } catch (Exception e) {
            log.error("Error refreshing all caches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to refresh caches: " + e.getMessage()));
        }
    }

    @Schema(description = "Cache refresh response")
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RefreshResponse {
        @Schema(description = "Success message", example = "Cache refreshed successfully")
        private String message;
        
        @Schema(description = "Route ID (null if refreshing all)", example = "SYSTEM_TO_NIP")
        private String routeId;
    }

    @Schema(description = "Error response")
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        @Schema(description = "Error message", example = "Failed to refresh cache: Mapping file not found")
        private String error;
    }
}

