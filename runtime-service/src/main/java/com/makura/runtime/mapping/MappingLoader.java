package com.makura.runtime.mapping;

import com.makura.translator.mapping.MappingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Wrapper around callable-jar's MappingLoader that adds Spring caching
 */
@Slf4j
@Component
public class MappingLoader {

    private final com.makura.translator.mapping.MappingLoader delegate;

    public MappingLoader(@Value("${makura.runtime.mappings.base-path:./mappings}") String mappingsBasePath) {
        this.delegate = new com.makura.translator.mapping.MappingLoader(mappingsBasePath);
    }

    @Cacheable(value = "mappingConfigs", key = "#routeId")
    public MappingConfig loadMappingConfig(String routeId) {
        try {
            return delegate.loadMappingConfig(routeId);
        } catch (com.makura.translator.mapping.MappingLoader.MappingLoadException e) {
            log.error("Error loading mapping config for routeId: {}. Details: {}", routeId, e.getMessage(), e);
            throw new MappingLoadException(e.getMessage(), e);
        }
    }

    @CacheEvict(value = "mappingConfigs", key = "#routeId")
    public void refreshMappingCache(String routeId) {
        log.info("Cache refresh requested for routeId: {}", routeId);
    }

    public static class MappingLoadException extends RuntimeException {
        public MappingLoadException(String message) {
            super(message);
        }

        public MappingLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

