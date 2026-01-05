package com.makura.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Route Configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {
    private Long id;
    private String routeId;
    private String name;
    private String description;
    private String mode; // ACTIVE or PASSIVE
    private String inboundFormat; // JSON, SOAP, XML
    private String outboundFormat; // JSON, SOAP, XML
    private String endpoint;
    private String encryptionType; // NONE, AES, PGP
    private String encryptionKeyRef;
    private String yamlContent;
    private Integer yamlVersion;
    private Boolean active;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}

