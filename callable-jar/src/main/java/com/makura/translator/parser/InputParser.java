package com.makura.translator.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * Parser for different inbound formats (JSON, SOAP, XML)
 */
public class InputParser {

    // Reuse static ObjectMapper instance for better performance
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ObjectMapper objectMapper;

    public InputParser() {
        this.objectMapper = OBJECT_MAPPER;
    }

    /**
     * Parse JSON input to a Map structure
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseJson(String jsonContent) throws ParseException {
        try {
            return objectMapper.readValue(jsonContent, Map.class);
        } catch (Exception e) {
            throw new ParseException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Parse SOAP input to XML Document
     */
    public Document parseSoap(String soapContent) throws ParseException {
        try {
            SOAPMessage soapMessage = MessageFactory.newInstance()
                .createMessage(null, new ByteArrayInputStream(soapContent.getBytes()));
            
            // Extract SOAP body content as XML string (preserving structure)
            jakarta.xml.soap.SOAPBody soapBody = soapMessage.getSOAPBody();
            
            // Get the first child element of the SOAP body (the actual payload)
            // Skip text nodes (whitespace) and find the actual element
            if (soapBody.hasChildNodes()) {
                org.w3c.dom.Node child = soapBody.getFirstChild();
                // Skip text nodes and find the first element node
                while (child != null && child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    child = child.getNextSibling();
                }
                
                if (child != null && child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    // Convert the element to XML string
                    org.w3c.dom.Element element = (org.w3c.dom.Element) child;
                    
                    javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
                    javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(element);
                    java.io.StringWriter writer = new java.io.StringWriter();
                    javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(writer);
                    transformer.transform(source, result);
                    String xmlContent = writer.toString();
                    
                    return DocumentHelper.parseText(xmlContent);
                }
            }
            
            // Fallback: try to parse the entire SOAP message as XML
            return DocumentHelper.parseText(soapContent);
        } catch (Exception e) {
            throw new ParseException("Failed to parse SOAP: " + e.getMessage(), e);
        }
    }

    /**
     * Parse XML input to Document
     */
    public Document parseXml(String xmlContent) throws ParseException {
        try {
            return DocumentHelper.parseText(xmlContent);
        } catch (Exception e) {
            throw new ParseException("Failed to parse XML: " + e.getMessage(), e);
        }
    }

    /**
     * Parse input based on format type
     */
    public Object parse(String content, String format) throws ParseException {
        return switch (format.toUpperCase()) {
            case "JSON" -> parseJson(content);
            case "SOAP" -> parseSoap(content);
            case "XML", "PROPRIETARY_XML" -> parseXml(content);
            default -> throw new ParseException("Unsupported format: " + format);
        };
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

