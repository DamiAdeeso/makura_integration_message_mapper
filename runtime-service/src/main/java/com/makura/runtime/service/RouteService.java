package com.makura.runtime.service;

import com.makura.runtime.model.Route;
import com.makura.runtime.repository.RouteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for Route management with caching support
 */
@Slf4j
@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Get route by ID with caching
     */
    @Cacheable(value = "routes", key = "#routeId", unless = "#result == null")
    public Route getActiveRoute(String routeId) {
        return routeRepository.findByRouteIdAndActiveTrue(routeId)
            .orElseThrow(() -> new TranslationService.RouteNotFoundException("Route not found or inactive: " + routeId));
    }

    /**
     * Evict route from cache (called when route is updated)
     */
    @CacheEvict(value = "routes", key = "#routeId")
    public void evictRouteCache(String routeId) {
        log.debug("Route cache evicted for routeId: {}", routeId);
    }

    /**
     * Evict all routes from cache
     */
    @CacheEvict(value = "routes", allEntries = true)
    public void evictAllRoutesCache() {
        log.debug("All routes cache evicted");
    }
}


