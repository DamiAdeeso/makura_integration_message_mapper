package com.makura.runtime.config;

import com.makura.runtime.mapping.MappingLoader;
import com.makura.runtime.model.Route;
import com.makura.runtime.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Preloads all mapping configurations at startup.
 * Fails startup if any active route has an invalid mapping.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MappingPreloader implements ApplicationRunner {

    private final RouteRepository routeRepository;
    private final MappingLoader mappingLoader;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Preloading mapping configurations for all active routes...");
        
        List<Route> activeRoutes = routeRepository.findAll().stream()
            .filter(route -> Boolean.TRUE.equals(route.getActive()))
            .toList();
        
        if (activeRoutes.isEmpty()) {
            log.warn("No active routes found in database. Skipping mapping preload.");
            return;
        }
        
        int successCount = 0;
        for (Route route : activeRoutes) {
            try {
                mappingLoader.loadMappingConfig(route.getRouteId());
                log.debug("Successfully preloaded mapping for routeId: {}", route.getRouteId());
                successCount++;
            } catch (MappingLoader.MappingLoadException e) {
                log.error("Failed to preload mapping for routeId: {}. Error: {}", 
                    route.getRouteId(), e.getMessage(), e);
                throw new RuntimeException(
                    String.format("Failed to preload mapping for routeId: %s. Service cannot start.", 
                        route.getRouteId()), e);
            }
        }
        
        log.info("Successfully preloaded {} mapping configuration(s) at startup", successCount);
    }
}




