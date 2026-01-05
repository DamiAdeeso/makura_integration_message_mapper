package com.makura.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new route
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteRequest {
    
    @NotBlank(message = "Route ID is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Route ID must contain only uppercase letters, numbers, and underscores")
    private String routeId;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Mode is required")
    @Pattern(regexp = "^(ACTIVE|PASSIVE)$", message = "Mode must be either ACTIVE or PASSIVE")
    private String mode;
    
    @NotBlank(message = "Inbound format is required")
    @Pattern(regexp = "^(JSON|SOAP|XML)$", message = "Inbound format must be JSON, SOAP, or XML")
    private String inboundFormat;
    
    @NotBlank(message = "Outbound format is required")
    @Pattern(regexp = "^(JSON|SOAP|XML)$", message = "Outbound format must be JSON, SOAP, or XML")
    private String outboundFormat;
    
    private String endpoint;
    
    private String encryptionType; // NONE, AES, PGP
    
    private String encryptionKeyRef;
    
    private String yamlContent;
    
    @Builder.Default
    private Boolean active = true;
}

