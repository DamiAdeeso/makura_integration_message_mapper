package com.makura.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for metrics data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsDTO {
    private String routeId;
    private Long totalRequests;
    private Long successfulRequests;
    private Long failedRequests;
    private Double averageDuration;
    private Double minDuration;
    private Double maxDuration;
    private Map<String, Object> additionalMetrics;
}



