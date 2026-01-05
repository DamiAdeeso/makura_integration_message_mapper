package com.makura.dashboard.service;

import com.makura.dashboard.dto.FieldMappingDTO;
import com.makura.dashboard.dto.MappingConfigDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating YAML mapping files
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YamlGenerationService {

    @Value("${makura.runtime.mappings.base-path:../runtime-service/mappings}")
    private String mappingsBasePath;

    /**
     * Generate YAML content from mapping configuration
     * Generates format compatible with runtime service: routeId, inboundFormat, outboundFormat, mode, mappings.request, mappings.response
     */
    public String generateYaml(MappingConfigDTO config) {
        Map<String, Object> yamlMap = new HashMap<>();
        
        // Add route metadata (required by runtime service)
        yamlMap.put("routeId", config.getRouteId() != null ? config.getRouteId() : "");
        yamlMap.put("inboundFormat", config.getInputFormat() != null ? config.getInputFormat() : "XML");
        yamlMap.put("outboundFormat", config.getOutputFormat() != null ? config.getOutputFormat() : "ISO_XML");
        yamlMap.put("mode", config.getMode() != null ? config.getMode() : "PASSIVE");
        
        if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
            yamlMap.put("endpoint", config.getEndpoint());
        }
        
        // Add namespace configuration if provided
        if (config.getNamespace() != null) {
            Map<String, Object> namespaceMap = new HashMap<>();
            if (config.getNamespace().getUri() != null) {
                namespaceMap.put("uri", config.getNamespace().getUri());
            }
            if (config.getNamespace().getPrefix() != null) {
                namespaceMap.put("prefix", config.getNamespace().getPrefix());
            }
            if (config.getNamespace().getRootElementPrefix() != null) {
                namespaceMap.put("rootElementPrefix", config.getNamespace().getRootElementPrefix());
            }
            if (!namespaceMap.isEmpty()) {
                yamlMap.put("namespace", namespaceMap);
            }
        }
        
        // Build mappings structure with request and response
        Map<String, List<Map<String, Object>>> mappings = new HashMap<>();
        
        // Process request mappings
        List<Map<String, Object>> requestMappings = new ArrayList<>();
        List<FieldMappingDTO> requestList = config.getRequestMappings() != null ? config.getRequestMappings() : 
            (config.getMappings() != null ? config.getMappings() : new ArrayList<>());
        
        for (FieldMappingDTO fieldMapping : requestList) {
            Map<String, Object> mapping = buildMappingMap(fieldMapping);
            requestMappings.add(mapping);
        }
        mappings.put("request", requestMappings);
        
        // Process response mappings
        List<Map<String, Object>> responseMappings = new ArrayList<>();
        List<FieldMappingDTO> responseList = config.getResponseMappings() != null ? config.getResponseMappings() : new ArrayList<>();
        
        for (FieldMappingDTO fieldMapping : responseList) {
            Map<String, Object> mapping = buildMappingMap(fieldMapping);
            responseMappings.add(mapping);
        }
        mappings.put("response", responseMappings);
        
        yamlMap.put("mappings", mappings);
        
        // Convert to YAML string
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        
        Yaml yaml = new Yaml(options);
        String yamlContent = yaml.dump(yamlMap);
        
        log.info("Generated YAML for route: {}", config.getRouteId());
        return yamlContent;
    }
    
    /**
     * Build a mapping map from FieldMappingDTO
     */
    private Map<String, Object> buildMappingMap(FieldMappingDTO fieldMapping) {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("from", fieldMapping.getSourcePath() != null ? fieldMapping.getSourcePath() : "");
        mapping.put("to", fieldMapping.getTargetPath() != null ? fieldMapping.getTargetPath() : "");
        
        // Use transform field if available, otherwise fall back to transformation
        String transformValue = fieldMapping.getTransform() != null ? fieldMapping.getTransform() : 
            (fieldMapping.getTransformation() != null ? fieldMapping.getTransformation() : null);
        
        if (transformValue != null && !transformValue.trim().isEmpty()) {
            mapping.put("transform", transformValue);
        }
        
        if (fieldMapping.getDefaultValue() != null && !fieldMapping.getDefaultValue().isEmpty()) {
            mapping.put("defaultValue", fieldMapping.getDefaultValue());
        }
        
        return mapping;
    }

    /**
     * Validate YAML content
     */
    public boolean validateYaml(String yamlContent) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(yamlContent);
            
            // Check required fields
            if (!yamlMap.containsKey("name") || !yamlMap.containsKey("format") || !yamlMap.containsKey("mappings")) {
                log.error("YAML validation failed: missing required fields");
                return false;
            }
            
            // Validate format section
            @SuppressWarnings("unchecked")
            Map<String, String> format = (Map<String, String>) yamlMap.get("format");
            if (!format.containsKey("input") || !format.containsKey("output")) {
                log.error("YAML validation failed: format section incomplete");
                return false;
            }
            
            // Validate mappings section
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mappings = (List<Map<String, Object>>) yamlMap.get("mappings");
            for (Map<String, Object> mapping : mappings) {
                if (!mapping.containsKey("source") || !mapping.containsKey("target")) {
                    log.error("YAML validation failed: mapping missing source or target");
                    return false;
                }
            }
            
            log.info("YAML validation successful");
            return true;
            
        } catch (Exception e) {
            log.error("YAML validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parse YAML content into MappingConfigDTO
     * Supports both old format (format.input/output, mappings as flat list) and new format (routeId, inboundFormat, mappings.request/response)
     */
    public MappingConfigDTO parseYaml(String yamlContent) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMap = yaml.load(yamlContent);
            
            MappingConfigDTO.MappingConfigDTOBuilder builder = MappingConfigDTO.builder();
            
            // Check if it's the new runtime service format
            if (yamlMap.containsKey("routeId")) {
                // New format: routeId, inboundFormat, outboundFormat, mode, mappings.request/response
                builder.routeId((String) yamlMap.get("routeId"));
                builder.inputFormat((String) yamlMap.get("inboundFormat"));
                builder.outputFormat((String) yamlMap.get("outboundFormat"));
                builder.mode((String) yamlMap.get("mode"));
                builder.endpoint((String) yamlMap.get("endpoint"));
                
                // Parse namespace configuration if present
                @SuppressWarnings("unchecked")
                Map<String, Object> namespaceMap = (Map<String, Object>) yamlMap.get("namespace");
                if (namespaceMap != null) {
                    MappingConfigDTO.NamespaceConfigDTO.NamespaceConfigDTOBuilder nsBuilder = 
                        MappingConfigDTO.NamespaceConfigDTO.builder();
                    nsBuilder.uri((String) namespaceMap.get("uri"));
                    nsBuilder.prefix((String) namespaceMap.get("prefix"));
                    nsBuilder.rootElementPrefix((String) namespaceMap.get("rootElementPrefix"));
                    builder.namespace(nsBuilder.build());
                }
                
                @SuppressWarnings("unchecked")
                Map<String, List<Map<String, Object>>> mappings = (Map<String, List<Map<String, Object>>>) yamlMap.get("mappings");
                
                if (mappings != null) {
                    List<FieldMappingDTO> requestMappings = parseMappingList(mappings.get("request"));
                    List<FieldMappingDTO> responseMappings = parseMappingList(mappings.get("response"));
                    builder.requestMappings(requestMappings);
                    builder.responseMappings(responseMappings);
                }
            } else {
                // Old format: format.input/output, mappings as flat list
                @SuppressWarnings("unchecked")
                Map<String, String> format = (Map<String, String>) yamlMap.get("format");
                
                if (format != null) {
                    builder.inputFormat(format.get("input"));
                    builder.outputFormat(format.get("output"));
                }
                
                builder.name((String) yamlMap.get("name"));
                builder.description((String) yamlMap.get("description"));
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> mappingsList = (List<Map<String, Object>>) yamlMap.get("mappings");
                
                if (mappingsList != null) {
                    List<FieldMappingDTO> fieldMappings = parseMappingList(mappingsList);
                    builder.mappings(fieldMappings);
                }
            }
            
            return builder.build();
                    
        } catch (Exception e) {
            log.error("Failed to parse YAML: {}", e.getMessage());
            throw new RuntimeException("Invalid YAML format: " + e.getMessage());
        }
    }
    
    /**
     * Parse a list of mapping maps into FieldMappingDTO list
     */
    private List<FieldMappingDTO> parseMappingList(List<Map<String, Object>> mappingsList) {
        if (mappingsList == null) {
            return new ArrayList<>();
        }
        
        List<FieldMappingDTO> fieldMappings = new ArrayList<>();
        for (Map<String, Object> mappingMap : mappingsList) {
            // Support both new format (from/to) and legacy format (source/target)
            String sourcePath = (String) mappingMap.get("from");
            if (sourcePath == null) {
                sourcePath = (String) mappingMap.get("source");
            }
            
            String targetPath = (String) mappingMap.get("to");
            if (targetPath == null) {
                targetPath = (String) mappingMap.get("target");
            }
            
            // Support both new format (defaultValue) and legacy format (default)
            String defaultValue = (String) mappingMap.get("defaultValue");
            if (defaultValue == null) {
                defaultValue = (String) mappingMap.get("default");
            }
            
            // Support both new format (transform) and legacy format (transformation)
            String transform = (String) mappingMap.get("transform");
            String transformation = (String) mappingMap.get("transformation");
            
            FieldMappingDTO fieldMapping = FieldMappingDTO.builder()
                    .sourcePath(sourcePath)
                    .targetPath(targetPath)
                    .defaultValue(defaultValue)
                    .transform(transform)
                    .transformation(transformation) // Legacy field
                    .build();
            fieldMappings.add(fieldMapping);
        }
        return fieldMappings;
    }

    /**
     * Generate YAML and write to file in runtime-service/mappings/ directory
     * @param config Mapping configuration
     * @return Path to the written file
     * @throws IOException if file writing fails
     */
    public Path generateAndWriteYaml(MappingConfigDTO config) throws IOException {
        if (config.getRouteId() == null || config.getRouteId().isEmpty()) {
            throw new IllegalArgumentException("Route ID is required to write YAML file");
        }

        String yamlContent = generateYaml(config);
        Path mappingsDir = Paths.get(mappingsBasePath);
        
        // Create directory if it doesn't exist
        if (!Files.exists(mappingsDir)) {
            Files.createDirectories(mappingsDir);
            log.info("Created mappings directory: {}", mappingsDir);
        }

        // Write YAML file
        String fileName = config.getRouteId() + ".yaml";
        Path filePath = mappingsDir.resolve(fileName);
        Files.writeString(filePath, yamlContent);
        
        log.info("Written YAML file to: {}", filePath);
        return filePath;
    }
}

