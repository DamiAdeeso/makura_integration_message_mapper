package com.makura.translator.mapping;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transformation engine for applying transformations to field values.
 * Supports date formatting, string concatenation, status mapping, etc.
 */
public class TransformationEngine {

    private static final Pattern CONCAT_PATTERN = Pattern.compile("concat\\(([^)]+)\\)");
    private static final Pattern FORMAT_DATE_PATTERN = Pattern.compile("formatDateTime\\(([^,]+),\\s*['\"]([^'\"]+)['\"]\\)");
    private static final Pattern SUBSTRING_PATTERN = Pattern.compile("substring\\(([^,]+),\\s*(-?\\d+)\\)");
    private static final Pattern SUBTRACT_DAYS_PATTERN = Pattern.compile("subtractDays\\(([^,]+),\\s*(\\d+)\\)");
    
    // Status code mappings
    private static final Map<String, String> STATUS_CODE_MAP = new HashMap<>();
    static {
        STATUS_CODE_MAP.put("ACSC", "25");  // Accepted Settlement Completed
        STATUS_CODE_MAP.put("ACCP", "00");  // Accepted Customer Profile
        STATUS_CODE_MAP.put("ACSP", "01");  // Accepted Settlement In Process
        STATUS_CODE_MAP.put("RJCT", "99");  // Rejected
        STATUS_CODE_MAP.put("CANC", "98");  // Cancelled
        STATUS_CODE_MAP.put("PDNG", "02");  // Pending
    }

    /**
     * Apply transformation to a value based on transformation expression.
     * 
     * @param value The source value (can be null for transformations that don't need it)
     * @param transformExpression The transformation expression (e.g., "formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ')")
     * @return The transformed value
     */
    public static String applyTransformation(String value, String transformExpression) {
        return applyTransformation(value, transformExpression, null);
    }

    /**
     * Apply transformation to a value based on transformation expression.
     * Can reference other source fields using source.fieldPath syntax.
     * 
     * @param value The source value (can be null for transformations that don't need it)
     * @param transformExpression The transformation expression (e.g., "concat(source.SourceInstitutionCode, formatDateTime(now(), 'yyyyMMddHHmmss'), substring(value, -15))")
     * @param sourceObject The full source object (JSON Map or XML Document) for resolving field references
     * @return The transformed value
     */
    public static String applyTransformation(String value, String transformExpression, Object sourceObject) {
        if (transformExpression == null || transformExpression.trim().isEmpty()) {
            return value;
        }

        String expression = transformExpression.trim();

        // Handle formatDateTime transformations
        if (expression.startsWith("formatDateTime")) {
            return applyFormatDateTime(value, expression, sourceObject);
        }

        // Handle concat transformations
        if (expression.startsWith("concat")) {
            return applyConcat(value, expression, sourceObject);
        }

        // Handle substring transformations
        if (expression.startsWith("substring")) {
            return applySubstring(value, expression, sourceObject);
        }

        // Handle subtractDays transformations
        if (expression.startsWith("subtractDays")) {
            return applySubtractDays(value, expression);
        }

        // Handle status mapping
        if (expression.startsWith("mapStatusToResponseCode")) {
            return applyStatusMapping(value, expression);
        }

        // Handle extractSessionId
        if (expression.startsWith("extractSessionId")) {
            return applyExtractSessionId(value, expression);
        }

        // If no transformation matches, return original value
        return value;
    }
    
    /**
     * Resolve a field reference from the source object.
     * Supports source.fieldPath syntax to reference other fields in the source.
     * 
     * @param reference The field reference (e.g., "source.SourceInstitutionCode" or "value")
     * @param currentValue The current field value (used when reference is "value")
     * @param sourceObject The full source object for resolving references
     * @return The resolved value, or null if not found
     */
    private static String resolveFieldReference(String reference, String currentValue, Object sourceObject) {
        if (reference == null || reference.trim().isEmpty()) {
            return currentValue;
        }
        
        reference = reference.trim();
        
        // If it's "value", return the current value
        if ("value".equals(reference)) {
            return currentValue;
        }
        
        // If it starts with "source.", resolve from source object
        if (reference.startsWith("source.")) {
            if (sourceObject == null) {
                return null;
            }
            // Keep the full reference including "source." for PathResolver
            Object resolved = PathResolver.getValueFromSource(sourceObject, reference);
            return resolved != null ? resolved.toString() : null;
        }
        
        // If it's a quoted string, return the literal value
        if ((reference.startsWith("'") && reference.endsWith("'")) || 
            (reference.startsWith("\"") && reference.endsWith("\""))) {
            return reference.substring(1, reference.length() - 1);
        }
        
        // Default: return as-is (could be a variable name we don't recognize)
        return reference;
    }

    /**
     * Format date/time: formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ')
     * or formatDateTime(value, 'yyyy-MM-dd')
     */
    private static String applyFormatDateTime(String value, String expression, Object sourceObject) {
        Matcher matcher = FORMAT_DATE_PATTERN.matcher(expression);
        if (matcher.find()) {
            String dateSource = matcher.group(1).trim();
            String format = matcher.group(2);

            LocalDateTime dateTime;
            if ("now()".equals(dateSource)) {
                dateTime = LocalDateTime.now(ZoneOffset.UTC);
            } else {
                // Try to parse the value as a date, or use current time
                dateTime = LocalDateTime.now(ZoneOffset.UTC);
            }

            // Handle common date format patterns - quote literal T and Z characters for DateTimeFormatter
            String normalizedFormat = format;
            // Quote T between date and time parts (e.g., yyyy-MM-ddTHH becomes yyyy-MM-dd'T'HH)
            if (normalizedFormat.contains("THH") || normalizedFormat.contains("Tmm") || normalizedFormat.contains("Tss")) {
                normalizedFormat = normalizedFormat.replace("T", "'T'");
            }
            // Quote Z at the end for UTC timezone indicator
            if (normalizedFormat.endsWith("Z") && !normalizedFormat.endsWith("'Z'")) {
                normalizedFormat = normalizedFormat.substring(0, normalizedFormat.length() - 1) + "'Z'";
            } else if (normalizedFormat.endsWith("z") && !normalizedFormat.endsWith("'z'")) {
                normalizedFormat = normalizedFormat.substring(0, normalizedFormat.length() - 1) + "'Z'";
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(normalizedFormat);
            
            // If format includes timezone indicator (Z), format with offset
            if (format.endsWith("Z") || format.endsWith("z")) {
                return dateTime.atOffset(ZoneOffset.UTC).format(formatter);
            }
            return dateTime.format(formatter);
        }
        return value != null ? value : "";
    }

    /**
     * Concatenate strings: concat('prefix', value, 'suffix', source.OtherField)
     * Supports field references using source.fieldPath syntax
     */
    private static String applyConcat(String value, String expression, Object sourceObject) {
        Matcher matcher = CONCAT_PATTERN.matcher(expression);
        if (matcher.find()) {
            String args = matcher.group(1);
            // Split by comma, but respect quoted strings
            String[] parts = splitRespectingQuotes(args);
            
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("'") && part.endsWith("'")) {
                    // Literal string
                    result.append(part.substring(1, part.length() - 1));
                } else if (part.startsWith("\"") && part.endsWith("\"")) {
                    // Literal string with double quotes
                    result.append(part.substring(1, part.length() - 1));
                } else if ("now()".equals(part)) {
                    // Current timestamp
                    result.append(LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
                } else if (part.startsWith("formatDateTime")) {
                    // Nested formatDateTime
                    result.append(applyFormatDateTime(value, part, sourceObject));
                } else if (part.startsWith("substring")) {
                    // Nested substring
                    result.append(applySubstring(value, part, sourceObject));
                } else {
                    // Resolve field reference (could be "value", "source.FieldPath", or other)
                    String resolved = resolveFieldReference(part, value, sourceObject);
                    if (resolved != null) {
                        result.append(resolved);
                    }
                }
            }
            return result.toString();
        }
        return value;
    }

    /**
     * Extract substring: substring(value, -15) or substring(source.Field, 0)
     * Supports field references using source.fieldPath syntax
     */
    private static String applySubstring(String value, String expression, Object sourceObject) {
        Matcher matcher = SUBSTRING_PATTERN.matcher(expression);
        if (matcher.find()) {
            String source = matcher.group(1).trim();
            int index = Integer.parseInt(matcher.group(2));

            // Resolve the source value (could be "value", "source.FieldPath", etc.)
            String sourceValue = resolveFieldReference(source, value, sourceObject);
            if (sourceValue == null) {
                return value;
            }

            if (index < 0) {
                // Negative index: take last N characters
                int length = Math.abs(index);
                if (sourceValue.length() > length) {
                    return sourceValue.substring(sourceValue.length() - length);
                }
                return sourceValue;
            } else {
                // Positive index: take from index to end
                if (sourceValue.length() > index) {
                    return sourceValue.substring(index);
                }
                return sourceValue;
            }
        }
        return value;
    }

    /**
     * Subtract days from date: subtractDays(now(), 6)
     * Note: subtractDays should be wrapped in formatDateTime for custom formatting
     */
    private static String applySubtractDays(String value, String expression) {
        Matcher matcher = SUBTRACT_DAYS_PATTERN.matcher(expression);
        if (matcher.find()) {
            String dateSource = matcher.group(1).trim();
            int days = Integer.parseInt(matcher.group(2));

            LocalDateTime dateTime;
            if ("now()".equals(dateSource)) {
                dateTime = LocalDateTime.now(ZoneOffset.UTC).minusDays(days);
            } else {
                dateTime = LocalDateTime.now(ZoneOffset.UTC).minusDays(days);
            }

            // Default format for subtractDays is ISO 8601 with proper timezone handling
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            return dateTime.atOffset(ZoneOffset.UTC).format(formatter);
        }
        return value != null ? value : "";
    }

    /**
     * Map status code: mapStatusToResponseCode(GrpSts)
     */
    private static String applyStatusMapping(String value, String expression) {
        if (value == null) {
            return "99"; // Default to error
        }
        return STATUS_CODE_MAP.getOrDefault(value, "99");
    }

    /**
     * Extract session ID from message ID: extractSessionId(MsgId)
     */
    private static String applyExtractSessionId(String value, String expression) {
        if (value == null) {
            return null;
        }
        // For now, return the value as-is. In a real scenario, you might extract
        // a specific part of the message ID
        return value;
    }

    /**
     * Split string by comma, respecting quoted strings
     */
    private static String[] splitRespectingQuotes(String input) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        char quoteChar = '\0';
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if ((c == '\'' || c == '"') && (i == 0 || input.charAt(i - 1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                    current.append(c);
                } else if (c == quoteChar) {
                    inQuotes = false;
                    quoteChar = '\0';
                    current.append(c);
                } else {
                    current.append(c);
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result.toArray(new String[0]);
    }
}


