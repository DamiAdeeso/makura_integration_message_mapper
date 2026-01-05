package com.makura.dashboard.service;

import com.makura.dashboard.dto.MetricsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for fetching and aggregating metrics from runtime-service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final WebClient.Builder webClientBuilder;

    @Value("${makura.runtime.base-url:http://localhost:8080}")
    private String runtimeServiceUrl;

    /**
     * Get all metrics from runtime-service
     */
    public Map<String, Object> getAllMetrics() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(runtimeServiceUrl).build();
            
            // Fetch Prometheus metrics endpoint
            String metricsResponse = webClient.get()
                    .uri("/actuator/prometheus")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Parse and aggregate metrics
            Map<String, Object> metrics = parsePrometheusMetrics(metricsResponse);
            
            log.info("Successfully fetched metrics from runtime-service");
            return metrics;
            
        } catch (Exception e) {
            log.error("Failed to fetch metrics from runtime-service: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get metrics for a specific route
     */
    public MetricsDTO getMetricsForRoute(String routeId) {
        try {
            Map<String, Object> allMetrics = getAllMetrics();
            
            // Extract route-specific metrics
            // This is a simplified version - in production, you'd parse the actual Prometheus metrics
            return MetricsDTO.builder()
                    .routeId(routeId)
                    .totalRequests(0L)
                    .successfulRequests(0L)
                    .failedRequests(0L)
                    .averageDuration(0.0)
                    .minDuration(0.0)
                    .maxDuration(0.0)
                    .additionalMetrics(allMetrics)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to get metrics for route {}: {}", routeId, e.getMessage());
            throw new RuntimeException("Failed to fetch route metrics: " + e.getMessage());
        }
    }

    /**
     * Get aggregated metrics summary
     */
    public Map<String, Object> getMetricsSummary() {
        try {
            Map<String, Object> allMetrics = getAllMetrics();
            Map<String, Object> summary = new HashMap<>();
            
            // Extract key metrics from Prometheus data
            double totalRequests = getMetricValue(allMetrics, "makura_translation_requests_total");
            double successfulRequests = getMetricValue(allMetrics, "makura_translation_success_total");
            double failedRequests = getMetricValue(allMetrics, "makura_translation_errors_total");
            double totalDuration = getMetricValue(allMetrics, "makura_translation_duration_seconds_sum");
            double durationCount = getMetricValue(allMetrics, "makura_translation_duration_seconds_count");
            
            // Calculate derived metrics
            double successRate = totalRequests > 0 ? (successfulRequests / totalRequests) * 100 : 0.0;
            double avgResponseTime = durationCount > 0 ? (totalDuration / durationCount) * 1000 : 0.0; // Convert to ms
            
            summary.put("totalRequests", (int) totalRequests);
            summary.put("successCount", (int) successfulRequests);
            summary.put("failedCount", (int) failedRequests);
            summary.put("pendingCount", 0); // Not tracked yet
            summary.put("successRate", Math.round(successRate * 100.0) / 100.0);
            summary.put("avgResponseTime", Math.round(avgResponseTime));
            
            // Add mock route data for now (would need actual route-specific metrics)
            summary.put("topRoutes", List.of(
                createRouteSummary("CREDIT_TRANSFER_PACS008", totalRequests * 0.4, successRate, avgResponseTime),
                createRouteSummary("PAYMENT_STATUS_PACS002", totalRequests * 0.3, successRate, avgResponseTime),
                createRouteSummary("ACCOUNT_STATEMENT_CAMT053", totalRequests * 0.2, successRate, avgResponseTime),
                createRouteSummary("BALANCE_REPORT_CAMT052", totalRequests * 0.1, successRate, avgResponseTime)
            ));
            
            log.info("Generated metrics summary: {} total requests", totalRequests);
            return summary;
            
        } catch (Exception e) {
            log.error("Failed to generate metrics summary: {}", e.getMessage(), e);
            return createEmptySummary();
        }
    }
    
    private Map<String, Object> createRouteSummary(String routeId, double requests, double successRate, double avgTime) {
        Map<String, Object> route = new HashMap<>();
        route.put("routeId", routeId);
        route.put("count", (int) requests);
        route.put("successRate", successRate);
        route.put("avgResponseTime", avgTime);
        route.put("p95ResponseTime", avgTime * 1.5); // Mock p95 as 1.5x average
        return route;
    }
    
    private Map<String, Object> createEmptySummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRequests", 0);
        summary.put("successCount", 0);
        summary.put("failedCount", 0);
        summary.put("pendingCount", 0);
        summary.put("successRate", 0.0);
        summary.put("avgResponseTime", 0.0);
        summary.put("topRoutes", List.of());
        return summary;
    }
    
    private double getMetricValue(Map<String, Object> metrics, String metricName) {
        Object value = metrics.get(metricName);
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse metric {}: {}", metricName, value);
            return 0.0;
        }
    }

    /**
     * Get health status from runtime-service
     */
    public Map<String, Object> getHealthStatus() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(runtimeServiceUrl).build();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> healthResponse = webClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            log.info("Successfully fetched health status from runtime-service");
            return healthResponse != null ? healthResponse : new HashMap<>();
            
        } catch (Exception e) {
            log.error("Failed to fetch health status: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Parse Prometheus metrics format
     * This is a simplified parser - in production, use a proper Prometheus client library
     */
    private Map<String, Object> parsePrometheusMetrics(String metricsText) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (metricsText == null || metricsText.isEmpty()) {
            return metrics;
        }
        
        try {
            String[] lines = metricsText.split("\n");
            
            for (String line : lines) {
                // Skip comments and empty lines
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                
                // Parse metric line (simple approach)
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    String metricName = parts[0];
                    String value = parts[1];
                    
                    // Extract metric name and labels
                    if (metricName.contains("{")) {
                        String name = metricName.substring(0, metricName.indexOf("{"));
                        metrics.put(name, value);
                    } else {
                        metrics.put(metricName, value);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing Prometheus metrics: {}", e.getMessage());
        }
        
        return metrics;
    }
}

