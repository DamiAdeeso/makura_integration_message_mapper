package com.makura.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an API key
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApiKeyRequest {
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean active;
}



