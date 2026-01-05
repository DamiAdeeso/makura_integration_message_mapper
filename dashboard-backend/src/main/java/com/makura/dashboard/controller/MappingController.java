package com.makura.dashboard.controller;

import com.makura.dashboard.dto.MappingConfigDTO;
import com.makura.dashboard.service.YamlGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.makura.dashboard.security.RequiresPermission;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for managing mappings and YAML generation
 */
@Slf4j
@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
@Tag(name = "Mappings", description = "Mapping and YAML generation management")
@SecurityRequirement(name = "bearerAuth")
public class MappingController {

    private final YamlGenerationService yamlGenerationService;

    @PostMapping("/generate-yaml")
    @RequiresPermission("mappings:generate")
    @Operation(summary = "Generate YAML from mapping config", description = "Generate YAML mapping file from configuration")
    public ResponseEntity<Map<String, String>> generateYaml(@Valid @RequestBody MappingConfigDTO config) {
        log.info("Generating YAML for route: {}", config.getRouteId());
        
        String yamlContent = yamlGenerationService.generateYaml(config);
        
        Map<String, String> response = new HashMap<>();
        response.put("yaml", yamlContent);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-yaml")
    @RequiresPermission("mappings:view")
    @Operation(summary = "Validate YAML content", description = "Validate YAML mapping file format")
    public ResponseEntity<Map<String, Object>> validateYaml(@RequestBody Map<String, String> request) {
        String yamlContent = request.get("yaml");
        
        if (yamlContent == null || yamlContent.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "YAML content is required"
            ));
        }
         
        boolean isValid = yamlGenerationService.validateYaml(yamlContent);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ? "YAML is valid" : "YAML validation failed");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/parse-yaml")
    @RequiresPermission("mappings:view")
    @Operation(summary = "Parse YAML to config", description = "Parse YAML mapping file into configuration object")
    public ResponseEntity<MappingConfigDTO> parseYaml(@RequestBody Map<String, String> request) {
        String yamlContent = request.get("yaml");
        
        if (yamlContent == null || yamlContent.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            MappingConfigDTO config = yamlGenerationService.parseYaml(yamlContent);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Failed to parse YAML: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/download-yaml")
    @RequiresPermission("mappings:generate")
    @Operation(summary = "Download YAML file", description = "Generate and download YAML mapping file")
    public ResponseEntity<String> downloadYaml(@Valid @RequestBody MappingConfigDTO config) {
        log.info("Generating YAML file for download: {}", config.getRouteId());
        
        String yamlContent = yamlGenerationService.generateYaml(config);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-yaml"))
                .header("Content-Disposition", "attachment; filename=\"" + config.getRouteId() + ".yaml\"")
                .body(yamlContent);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Error in MappingController: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

