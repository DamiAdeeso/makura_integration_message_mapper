package com.makura.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for API Key (display only - never contains full key)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyDTO {
    private Long id;
    private String routeId;
    private String maskedKey; // e.g., "mak_1234****5678"
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean active;
    private Boolean expired;
    private Boolean expiringSoon;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;
}



