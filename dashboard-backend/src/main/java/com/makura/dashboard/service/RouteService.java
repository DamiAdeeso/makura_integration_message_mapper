package com.makura.dashboard.service;

import com.makura.dashboard.dto.CreateRouteRequest;
import com.makura.dashboard.dto.RouteDTO;
import com.makura.dashboard.dto.UpdateRouteRequest;
import com.makura.dashboard.runtime.model.Route;
import com.makura.dashboard.runtime.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing route configurations
 * Writes directly to makura_runtime database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;

    /**
     * Get all routes
     */
    public List<RouteDTO> getAllRoutes() {
        return routeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get route by ID
     */
    public RouteDTO getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));
        return toDTO(route);
    }

    /**
     * Get route by routeId (string identifier)
     */
    public RouteDTO getRouteByRouteId(String routeId) {
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with routeId: " + routeId));
        return toDTO(route);
    }

    /**
     * Create a new route
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public RouteDTO createRoute(CreateRouteRequest request) {
        // Check if routeId already exists
        if (routeRepository.existsByRouteId(request.getRouteId())) {
            throw new RuntimeException("Route with routeId '" + request.getRouteId() + "' already exists");
        }

        Route route = Route.builder()
                .routeId(request.getRouteId())
                .mode(Route.RouteMode.valueOf(request.getMode()))
                .inboundFormat(Route.InboundFormat.valueOf(request.getInboundFormat()))
                .outboundFormat(Route.OutboundFormat.valueOf(request.getOutboundFormat()))
                .endpoint(request.getEndpoint())
                .encryptionType(request.getEncryptionType() != null ? 
                        Route.EncryptionType.valueOf(request.getEncryptionType()) : 
                        Route.EncryptionType.NONE)
                .encryptionKeyRef(request.getEncryptionKeyRef())
                .yamlProfilePath(request.getRouteId() + ".yaml") // Set YAML file path
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Route saved = routeRepository.save(route);
        log.info("Created new route in runtime database: {}", saved.getRouteId());
        return toDTO(saved);
    }

    /**
     * Update an existing route
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public RouteDTO updateRoute(Long id, UpdateRouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));

        // Update only non-null fields
        if (request.getMode() != null) {
            route.setMode(Route.RouteMode.valueOf(request.getMode()));
        }
        if (request.getInboundFormat() != null) {
            route.setInboundFormat(Route.InboundFormat.valueOf(request.getInboundFormat()));
        }
        if (request.getOutboundFormat() != null) {
            route.setOutboundFormat(Route.OutboundFormat.valueOf(request.getOutboundFormat()));
        }
        if (request.getEndpoint() != null) {
            route.setEndpoint(request.getEndpoint());
        }
        if (request.getEncryptionType() != null) {
            route.setEncryptionType(Route.EncryptionType.valueOf(request.getEncryptionType()));
        }
        if (request.getEncryptionKeyRef() != null) {
            route.setEncryptionKeyRef(request.getEncryptionKeyRef());
        }
        if (request.getActive() != null) {
            route.setActive(request.getActive());
        }

        Route updated = routeRepository.save(route);
        log.info("Updated route in runtime database: {}", updated.getRouteId());
        return toDTO(updated);
    }

    /**
     * Delete a route
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public void deleteRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));
        
        routeRepository.delete(route);
        log.info("Deleted route from runtime database: {}", route.getRouteId());
    }

    /**
     * Get all active routes
     */
    public List<RouteDTO> getActiveRoutes() {
        return routeRepository.findByActive(true)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Toggle route active status
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public RouteDTO toggleRouteStatus(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));
        
        route.setActive(!route.getActive());
        
        Route updated = routeRepository.save(route);
        log.info("Toggled route {} status to: {}", updated.getRouteId(), updated.getActive());
        return toDTO(updated);
    }

    /**
     * Publish a route (mark it as ready for use)
     * Note: Runtime Route doesn't have published field, so this just ensures route is active
     */
    @Transactional(transactionManager = "runtimeTransactionManager")
    public RouteDTO publishRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));
        
        route.setActive(true);
        
        Route updated = routeRepository.save(route);
        log.info("Published route: {}", updated.getRouteId());
        return toDTO(updated);
    }

    /**
     * Convert Route entity to RouteDTO
     */
    private RouteDTO toDTO(Route route) {
        return RouteDTO.builder()
                .id(route.getId())
                .routeId(route.getRouteId())
                .name(route.getRouteId()) // Use routeId as name since runtime doesn't have name field
                .description(null) // Runtime doesn't store description
                .mode(route.getMode().name())
                .inboundFormat(route.getInboundFormat().name())
                .outboundFormat(route.getOutboundFormat().name())
                .endpoint(route.getEndpoint())
                .encryptionType(route.getEncryptionType() != null ? route.getEncryptionType().name() : null)
                .encryptionKeyRef(route.getEncryptionKeyRef())
                .yamlContent(null) // YAML is stored in files, not DB
                .yamlVersion(null) // Runtime doesn't track version
                .active(route.getActive())
                .published(route.getActive()) // Use active as published indicator
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .publishedAt(route.getActive() ? route.getUpdatedAt() : null)
                .build();
    }
}
