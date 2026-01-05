package com.makura.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for complete mapping configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingConfigDTO {
    private String routeId;
    private String name;
    private String description;
    private String inputFormat;  // JSON, SOAP, XML
    private String outputFormat; // ISO_XML
    private String mode; // ACTIVE or PASSIVE
    private String endpoint; // For ACTIVE mode
    private NamespaceConfigDTO namespace; // Optional namespace configuration for XML output
    private List<FieldMappingDTO> mappings; // Legacy: flat list (will be split into request/response)
    private List<FieldMappingDTO> requestMappings; // Request direction mappings
    private List<FieldMappingDTO> responseMappings; // Response direction mappings
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NamespaceConfigDTO {
        private String uri; // Namespace URI (required if namespace is specified)
        private String prefix; // Namespace prefix (optional, defaults to empty for default namespace)
        private String rootElementPrefix; // Prefix to use on root element in output (optional)
    }
}

