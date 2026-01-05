package com.makura.dashboard.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing route
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRouteRequest {
    
    private String name;
    
    private String description;
    
    @Pattern(regexp = "^(ACTIVE|PASSIVE)$", message = "Mode must be either ACTIVE or PASSIVE")
    private String mode;
    
    @Pattern(regexp = "^(JSON|SOAP|XML)$", message = "Inbound format must be JSON, SOAP, or XML")
    private String inboundFormat;
    
    @Pattern(regexp = "^(JSON|SOAP|XML)$", message = "Outbound format must be JSON, SOAP, or XML")
    private String outboundFormat;
    
    private String endpoint;
    
    private String encryptionType; // NONE, AES, PGP
    
    private String encryptionKeyRef;
    
    private String yamlContent;
    
    private Boolean active;
}

