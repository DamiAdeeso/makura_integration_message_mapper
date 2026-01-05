package com.makura.dashboard.controller;

import com.makura.dashboard.dto.CreateRouteRequest;
import com.makura.dashboard.dto.RouteDTO;
import com.makura.dashboard.dto.UpdateRouteRequest;
import com.makura.dashboard.security.RequiresPermission;
import com.makura.dashboard.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing route configurations
 * Uses permission-based access control
 */
@Slf4j
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "Route configuration management")
@SecurityRequirement(name = "bearerAuth")
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    @RequiresPermission("routes:view")
    @Operation(summary = "Get all routes", description = "Retrieve all route configurations")
    public ResponseEntity<List<RouteDTO>> getAllRoutes(
            @RequestParam(required = false) Boolean activeOnly) {
        
        List<RouteDTO> routes = activeOnly != null && activeOnly
                ? routeService.getActiveRoutes()
                : routeService.getAllRoutes();
        
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{id}")
    @RequiresPermission("routes:view")
    @Operation(summary = "Get route by ID", description = "Retrieve a specific route by its database ID")
    public ResponseEntity<RouteDTO> getRouteById(@PathVariable Long id) {
        RouteDTO route = routeService.getRouteById(id);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/route/{routeId}")
    @RequiresPermission("routes:view")
    @Operation(summary = "Get route by Route ID", description = "Retrieve a specific route by its route identifier")
    public ResponseEntity<RouteDTO> getRouteByRouteId(@PathVariable String routeId) {
        RouteDTO route = routeService.getRouteByRouteId(routeId);
        return ResponseEntity.ok(route);
    }

    @PostMapping
    @RequiresPermission("routes:create")
    @Operation(summary = "Create route", description = "Create a new route configuration")
    public ResponseEntity<RouteDTO> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        log.info("Creating new route: {}", request.getRouteId());
        RouteDTO created = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @RequiresPermission("routes:update")
    @Operation(summary = "Update route", description = "Update an existing route configuration")
    public ResponseEntity<RouteDTO> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRouteRequest request) {
        
        log.info("Updating route with id: {}", id);
        RouteDTO updated = routeService.updateRoute(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("routes:delete")
    @Operation(summary = "Delete route", description = "Delete a route configuration")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        log.info("Deleting route with id: {}", id);
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    @RequiresPermission("routes:toggle")
    @Operation(summary = "Toggle route status", description = "Enable or disable a route")
    public ResponseEntity<RouteDTO> toggleRouteStatus(@PathVariable Long id) {
        log.info("Toggling status for route with id: {}", id);
        RouteDTO updated = routeService.toggleRouteStatus(id);
        return ResponseEntity.ok(updated);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Error in RouteController: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

