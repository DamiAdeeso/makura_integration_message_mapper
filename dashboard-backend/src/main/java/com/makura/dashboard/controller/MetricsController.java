package com.makura.dashboard.controller;

import com.makura.dashboard.dto.MetricsDTO;
import com.makura.dashboard.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for metrics and monitoring
 */
@Slf4j
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Metrics and monitoring")
@SecurityRequirement(name = "bearerAuth")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping
    @Operation(summary = "Get all metrics", description = "Retrieve all metrics from runtime-service")
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        Map<String, Object> metrics = metricsService.getAllMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "Get metrics for route", description = "Retrieve metrics for a specific route")
    public ResponseEntity<MetricsDTO> getRouteMetrics(@PathVariable String routeId) {
        MetricsDTO metrics = metricsService.getMetricsForRoute(routeId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get metrics summary", description = "Retrieve aggregated metrics summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> summary = metricsService.getMetricsSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/health")
    @Operation(summary = "Get health status", description = "Retrieve health status from runtime-service")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = metricsService.getHealthStatus();
        
        String status = (String) health.get("status");
        if ("UP".equals(status)) {
            return ResponseEntity.ok(health);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Error in MetricsController: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}



