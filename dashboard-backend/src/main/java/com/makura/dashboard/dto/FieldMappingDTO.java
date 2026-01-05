package com.makura.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a single field mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMappingDTO {
    private String sourcePath; // Also supports "constant:value" syntax
    private String targetPath;
    private String defaultValue; // Default value if source is null
    private String transformation; // Legacy field name
    private String transform; // Transformation expression (e.g., "formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ')")
}

