package com.makura.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for newly created API key
 * Contains the full key (shown only once)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private Long id;
    private String routeId;
    private String apiKey; // Full key - shown only once!
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String createdBy;
    private LocalDateTime createdAt;
    private String warning; // "This key will not be shown again"
}



