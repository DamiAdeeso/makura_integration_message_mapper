package com.makura.translator.mapping;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Map;

/**
 * Custom path resolver for building XML structures from path expressions.
 * Handles paths like "target:DebtorAccount/Identification" and builds XML accordingly.
 */
public class PathResolver {

    /**
     * Resolves a path expression and sets the value in the XML document.
     * Path format: "target:Element1/Element2/Element3" or "Element1/Element2" or "source.RootElement.ChildElement"
     * Inherits namespace from parent elements automatically.
     */
    public static void setValueByPath(Document document, String pathExpression, Object value) {
        if (pathExpression == null || value == null) {
            return;
        }

        String path = pathExpression;
        
        // Remove "target:" prefix if present
        if (path.startsWith("target:")) {
            path = path.substring(7);
        }
        
        // Handle dot notation paths like "source.TSQuerySingleResponse.SessionID"
        // Strip "source." prefix and convert dots to proper XML structure
        if (path.startsWith("source.")) {
            path = path.substring(7); // Remove "source." prefix
            // Convert dot notation to forward slash notation for XML structure
            path = path.replace(".", "/");
        }

        String[] parts = path.split("/");
        if (parts.length == 0) {
            return;
        }

        Element current = document.getRootElement();
        if (current == null) {
            // Create root element if document is empty
            current = document.addElement(parts[0]);
        }

        // Navigate/create path
        // Skip first part if it matches the root element name
        int startIndex = 0;
        if (parts.length > 0 && parts[0].equals(current.getName())) {
            startIndex = 1;
        }
        
        for (int i = startIndex; i < parts.length - 1; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) continue;

            Element child = current.element(part);
            if (child == null) {
                // Inherit namespace from parent element if it exists
                org.dom4j.Namespace parentNamespace = current.getNamespace();
                if (parentNamespace != null && !parentNamespace.equals(org.dom4j.Namespace.NO_NAMESPACE)) {
                    // Use QName to create element with same namespace
                    org.dom4j.QName qname = org.dom4j.DocumentHelper.createQName(part, parentNamespace);
                    child = current.addElement(qname);
                } else {
                    child = current.addElement(part);
                }
            }
            current = child;
        }

        // Set value on the last element
        String lastPart = parts[parts.length - 1].trim();
        if (!lastPart.isEmpty()) {
            Element target = current.element(lastPart);
            if (target == null) {
                // Inherit namespace from parent element if it exists
                org.dom4j.Namespace parentNamespace = current.getNamespace();
                if (parentNamespace != null && !parentNamespace.equals(org.dom4j.Namespace.NO_NAMESPACE)) {
                    // Use QName to create element with same namespace
                    org.dom4j.QName qname = org.dom4j.DocumentHelper.createQName(lastPart, parentNamespace);
                    target = current.addElement(qname);
                } else {
                    target = current.addElement(lastPart);
                }
            }
            target.setText(value.toString());
        }
    }

    /**
     * Gets a value from XML document by dot notation path (e.g., "debtorAccount.accountNumber").
     * Note: Paths are relative to the document root element.
     */
    public static String getValueByDotPath(Document document, String path) {
        if (path == null || document == null) {
            return null;
        }

        Element root = document.getRootElement();
        if (root == null) {
            return null;
        }

        String[] parts = path.split("\\.");
        if (parts.length == 0) {
            return null;
        }

        Element current = root;

        // Navigate through XML elements using dot notation
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) continue;
            
            // Try to find element (handles namespaces by checking local name)
            Element child = findElement(current, part);
            if (child == null) {
                return null;
            }
            current = child;
        }

        // Get value from last element
        String lastPart = parts[parts.length - 1].trim();
        if (!lastPart.isEmpty()) {
            Element target = findElement(current, lastPart);
            if (target != null) {
                return target.getTextTrim();
            }
        }

        return null;
    }

    /**
     * Finds an element by name, handling namespaces.
     * dom4j's element() method handles namespaces automatically for default namespaces,
     * but we also check local names as fallback.
     */
    private static Element findElement(Element parent, String name) {
        // First try exact match using dom4j's element() method
        // This handles default namespaces automatically
        Element element = parent.element(name);
        if (element != null) {
            return element;
        }

        // If not found, try to find by local name (handles prefixed namespaces)
        // dom4j's getName() returns local name (without namespace prefix)
        java.util.List<Element> elements = parent.elements();
        for (Element child : elements) {
            String childName = child.getName(); // Local name (e.g., "debtorAccount")
            
            // Check if name matches local name
            if (name.equals(childName)) {
                return child;
            }
        }

        return null;
    }

    /**
     * Gets a value from XML document by path expression (slash notation for target paths).
     */
    public static String getValueByPath(Document document, String pathExpression) {
        if (pathExpression == null || document == null) {
            return null;
        }

        // Remove "target:" prefix if present
        String path = pathExpression.startsWith("target:")
            ? pathExpression.substring(7)
            : pathExpression;

        String[] parts = path.split("/");
        if (parts.length == 0) {
            return null;
        }

        Element current = document.getRootElement();
        if (current == null) {
            return null;
        }

        // Navigate path
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) continue;

            current = current.element(part);
            if (current == null) {
                return null;
            }
        }

        // Get value from last element
        String lastPart = parts[parts.length - 1].trim();
        if (!lastPart.isEmpty()) {
            Element target = current.element(lastPart);
            if (target != null) {
                return target.getTextTrim();
            }
        }

        return null;
    }

    /**
     * Creates a new XML document with root element name.
     */
    public static Document createDocument(String rootElementName) {
        Document document = DocumentHelper.createDocument();
        document.addElement(rootElementName);
        return document;
    }

    /**
     * Extracts value from source object (JSON Map or XML Document) using dot notation path.
     * Also supports constant values with "constant:value" syntax.
     * Generic implementation - no special handling for any field names.
     */
    @SuppressWarnings("unchecked")
    public static Object getValueFromSource(Object source, String path) {
        if (path == null) {
            return null;
        }

        // Check for constant value syntax: "constant:value"
        if (path.startsWith("constant:")) {
            return path.substring(9); // Return the value after "constant:"
        }

        // Check for "source." prefix and remove it (optional)
        if (path.startsWith("source.")) {
            path = path.substring(7);
        }

        if (source == null) {
            return null;
        }

        // If source is a Document (XML/SOAP), use XML path resolution with dot notation
        if (source instanceof Document) {
            return getValueByDotPath((Document) source, path);
        }

        // Navigate path directly for Map/List structures (JSON)
        String[] parts = path.split("\\.");
        Object current = source;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else if (current instanceof java.util.List) {
                try {
                    int index = Integer.parseInt(part);
                    java.util.List<?> list = (java.util.List<?>) current;
                    if (index >= 0 && index < list.size()) {
                        current = list.get(index);
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }
}

