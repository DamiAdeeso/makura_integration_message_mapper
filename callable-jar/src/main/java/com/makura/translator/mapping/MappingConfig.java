package com.makura.translator.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MappingConfig {
    private String routeId;
    private String inboundFormat;
    private String outboundFormat;
    private String mode;
    private String endpoint;
    private AuthConfig auth;
    private NamespaceConfig namespace; // Optional namespace configuration for XML output
    private String rootElementName; // Optional root element name (defaults to "Document" if not specified)
    private Mappings mappings;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NamespaceConfig {
        private String uri; // Namespace URI (required if namespace is specified)
        private String prefix; // Namespace prefix (optional, defaults to empty for default namespace)
        private String rootElementPrefix; // Prefix to use on root element in output (optional)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthConfig {
        private String type;
        private String key;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mappings {
        private List<FieldMapping> request;
        private List<FieldMapping> response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldMapping {
        private String from;
        private String to;
        private String transform; // Optional transformation function
        private String defaultValue; // Optional default value if source is null
    }
}


