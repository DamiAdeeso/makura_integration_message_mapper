package com.makura.translator.mapping;

import com.makura.translator.parser.InputParser;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core mapping engine that applies YAML mappings to transform messages
 */
public class MappingEngine {

    private final InputParser inputParser;

    public MappingEngine() {
        this.inputParser = new InputParser();
    }

    /**
     * Transform inbound message to target format XML using mapping config
     */
    public String transformToTarget(String inboundContent, MappingConfig mappingConfig) throws MappingException {
        try {
            // Parse inbound content
            Object parsedInput = inputParser.parse(inboundContent, mappingConfig.getInboundFormat());
            
            // Create XML document with optional namespace from config
            Document targetDocument = createXmlDocument(mappingConfig);
            
            // Apply request mappings
            if (mappingConfig.getMappings() != null && mappingConfig.getMappings().getRequest() != null) {
                applyMappings(parsedInput, targetDocument, mappingConfig.getMappings().getRequest());
            }
            
            // Convert document to XML string
            return documentToString(targetDocument, mappingConfig);
        } catch (InputParser.ParseException e) {
            throw new MappingException("Failed to parse inbound content", e);
        } catch (Exception e) {
            throw new MappingException("Failed to transform to target format", e);
        }
    }
    
    /**
     * Create XML document with optional namespace from config
     */
    private Document createXmlDocument(MappingConfig mappingConfig) {
        Document document = DocumentHelper.createDocument();
        
        // Determine root element name (default to "Document" if not specified)
        String rootElementName = mappingConfig.getRootElementName() != null 
            ? mappingConfig.getRootElementName() 
            : "Document";
        
        // Check if namespace is configured
        if (mappingConfig.getNamespace() != null && mappingConfig.getNamespace().getUri() != null) {
            String namespaceUri = mappingConfig.getNamespace().getUri();
            String namespacePrefix = mappingConfig.getNamespace().getPrefix() != null 
                ? mappingConfig.getNamespace().getPrefix() 
                : ""; // Default namespace if prefix not specified
            
            Namespace ns = Namespace.get(namespacePrefix, namespaceUri);
            QName rootQName = DocumentHelper.createQName(rootElementName, ns);
            org.dom4j.Element root = document.addElement(rootQName);
            
            // If rootElementPrefix is specified and different from namespace prefix, add it as additional namespace
            String rootElementPrefix = mappingConfig.getNamespace().getRootElementPrefix();
            if (rootElementPrefix != null && !rootElementPrefix.equals(namespacePrefix)) {
                root.add(Namespace.get(rootElementPrefix, namespaceUri));
            }
        } else {
            // No namespace - create simple root element
            document.addElement(rootElementName);
        }
        
        return document;
    }

    /**
     * Transform target format XML response back to source format
     */
    public String transformFromTarget(String targetContent, MappingConfig mappingConfig, String targetFormat) throws MappingException {
        try {
            // Parse target format XML
            Document targetDocument = inputParser.parseXml(targetContent);
            
            // Create source document based on format
            if ("JSON".equalsIgnoreCase(targetFormat)) {
                return transformTargetToJson(targetDocument, mappingConfig);
            } else {
                // For XML/SOAP, create XML structure
                // Determine root element name from first mapping's target path
                String rootElementName = "Response"; // Default
                if (mappingConfig.getMappings() != null && mappingConfig.getMappings().getResponse() != null 
                    && !mappingConfig.getMappings().getResponse().isEmpty()) {
                    String firstTargetPath = mappingConfig.getMappings().getResponse().get(0).getTo();
                    // Extract root element from path like "source.TSQuerySingleResponse.SessionID"
                    if (firstTargetPath != null && firstTargetPath.startsWith("source.")) {
                        String[] parts = firstTargetPath.substring(7).split("\\."); // Remove "source." prefix
                        if (parts.length > 0) {
                            rootElementName = parts[0]; // Use first part as root element
                        }
                    }
                }
                Document sourceDocument = PathResolver.createDocument(rootElementName);
                if (mappingConfig.getMappings() != null && mappingConfig.getMappings().getResponse() != null) {
                    applyResponseMappings(targetDocument, sourceDocument, mappingConfig.getMappings().getResponse());
                }
                return documentToString(sourceDocument, mappingConfig);
            }
        } catch (InputParser.ParseException e) {
            throw new MappingException("Failed to parse target content", e);
        } catch (Exception e) {
            throw new MappingException("Failed to transform from target format", e);
        }
    }

    private void applyMappings(Object source, Document target, List<MappingConfig.FieldMapping> mappings) {
        for (MappingConfig.FieldMapping mapping : mappings) {
            try {
                Object sourceValue = PathResolver.getValueFromSource(source, mapping.getFrom());
                
                // Use default value if source is null
                if (sourceValue == null && mapping.getDefaultValue() != null) {
                    sourceValue = mapping.getDefaultValue();
                }
                
                // If there's a transformation, allow it to run even if sourceValue is null or a placeholder
                // (transformations can use now() or other functions that don't need the source value)
                // Pass source object so transformations can reference other fields using source.fieldPath syntax
                if (mapping.getTransform() != null && !mapping.getTransform().trim().isEmpty()) {
                    String valueStr = sourceValue != null ? sourceValue.toString() : null;
                    String finalValue = TransformationEngine.applyTransformation(valueStr, mapping.getTransform(), source);
                    PathResolver.setValueByPath(target, mapping.getTo(), finalValue);
                } else if (sourceValue != null) {
                    // No transformation, use source value directly
                    String finalValue = sourceValue.toString();
                    PathResolver.setValueByPath(target, mapping.getTo(), finalValue);
                }
                // Silently skip null values - they may be optional fields
            } catch (Exception e) {
                // Log warning but continue with other mappings
                // Individual mapping failures shouldn't stop the entire transformation
            }
        }
    }

    private void applyResponseMappings(Document source, Document target, List<MappingConfig.FieldMapping> mappings) {
        for (MappingConfig.FieldMapping mapping : mappings) {
            try {
                String sourceValue = null;
                
                // Handle constants and target paths differently
                if (mapping.getFrom() != null && mapping.getFrom().startsWith("constant:")) {
                    // Constant value - extract directly
                    sourceValue = mapping.getFrom().substring(9);
                } else if (mapping.getFrom() != null) {
                    // Target path - use getValueByPath (handles target: prefix internally)
                    sourceValue = PathResolver.getValueByPath(source, mapping.getFrom());
                }
                
                // Use default value if source is null
                if (sourceValue == null && mapping.getDefaultValue() != null) {
                    sourceValue = mapping.getDefaultValue();
                }
                
                if (sourceValue != null) {
                    // Apply transformation if specified
                    String finalValue;
                    if (mapping.getTransform() != null && !mapping.getTransform().trim().isEmpty()) {
                        finalValue = TransformationEngine.applyTransformation(sourceValue, mapping.getTransform(), source);
                    } else {
                        finalValue = sourceValue;
                    }
                    
                    PathResolver.setValueByPath(target, mapping.getTo(), finalValue);
                }
            } catch (Exception e) {
                // Log warning but continue with other mappings
            }
        }
    }

    private String transformTargetToJson(Document targetDocument, MappingConfig mappingConfig) {
        if (mappingConfig.getMappings() == null || mappingConfig.getMappings().getResponse() == null) {
            return "{}";
        }

        Map<String, Object> jsonMap = new HashMap<>();
        for (MappingConfig.FieldMapping mapping : mappingConfig.getMappings().getResponse()) {
            String value = PathResolver.getValueByPath(targetDocument, mapping.getFrom());
            
            // Use default value if source is null
            if (value == null && mapping.getDefaultValue() != null) {
                value = mapping.getDefaultValue();
            }
            
            if (value != null) {
                // Apply transformation if specified
                String finalValue;
                if (mapping.getTransform() != null && !mapping.getTransform().trim().isEmpty()) {
                    finalValue = TransformationEngine.applyTransformation(value, mapping.getTransform(), targetDocument);
                } else {
                    finalValue = value;
                }
                
                setJsonValue(jsonMap, mapping.getTo(), finalValue);
            }
        }

        try {
            // Reuse static ObjectMapper instance for better performance
            return com.makura.translator.parser.InputParser.OBJECT_MAPPER.writeValueAsString(jsonMap);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private void setJsonValue(Map<String, Object> map, String path, String value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = map;
        
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new HashMap<>());
        }
        
        current.put(parts[parts.length - 1], value);
    }

    private String documentToString(Document document, MappingConfig mappingConfig) {
        try {
            StringWriter sw = new StringWriter();
            // Use compact format instead of pretty print for better performance
            OutputFormat format = OutputFormat.createCompactFormat();
            format.setEncoding("UTF-8");
            format.setSuppressDeclaration(false);
            XMLWriter writer = new XMLWriter(sw, format);
            writer.write(document);
            writer.close();
            String xml = sw.toString();
            
            // Post-process namespace prefix on root element if rootElementPrefix is specified
            // Optimize string replacements by checking if namespace processing is needed
            if (mappingConfig.getNamespace() != null 
                && mappingConfig.getNamespace().getRootElementPrefix() != null
                && mappingConfig.getNamespace().getUri() != null) {
                String rootElementPrefix = mappingConfig.getNamespace().getRootElementPrefix();
                String namespaceUri = mappingConfig.getNamespace().getUri();
                
                // Use StringBuilder for more efficient string manipulation
                if (xml.contains("xmlns=\"" + namespaceUri + "\"")) {
                    // Replace default namespace declaration with prefixed namespace on root
                    xml = xml.replaceFirst("xmlns=\"" + java.util.regex.Pattern.quote(namespaceUri) + "\"", 
                                          "xmlns:" + rootElementPrefix + "=\"" + namespaceUri + "\"");
                }
                // Replace root element tag with prefixed version (use compiled pattern for efficiency)
                if (xml.contains("<Document")) {
                    xml = xml.replaceFirst("<Document([^>]*)>", "<" + rootElementPrefix + ":Document$1>");
                    xml = xml.replaceFirst("</Document>", "</" + rootElementPrefix + ":Document>");
                }
            }
            
            return xml;
        } catch (Exception e) {
            return document.asXML();
        }
    }

    public static class MappingException extends Exception {
        public MappingException(String message) {
            super(message);
        }

        public MappingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

