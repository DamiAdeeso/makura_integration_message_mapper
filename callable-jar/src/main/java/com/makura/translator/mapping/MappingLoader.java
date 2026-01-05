package com.makura.translator.mapping;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads YAML mapping configurations from filesystem.
 * Standalone implementation without Spring dependencies.
 */
public class MappingLoader {

    private final String mappingsBasePath;
    private final Yaml yaml;

    public MappingLoader(String mappingsBasePath) {
        this.mappingsBasePath = mappingsBasePath != null ? mappingsBasePath : "./mappings";
        this.yaml = new Yaml();
    }

    /**
     * Load mapping configuration for a route
     */
    public MappingConfig loadMappingConfig(String routeId) throws MappingLoadException {
        try {
            Path basePath = Paths.get(mappingsBasePath);
            
            // If relative path, resolve from current working directory
            if (!basePath.isAbsolute()) {
                basePath = Paths.get(System.getProperty("user.dir")).resolve(mappingsBasePath);
            }
            
            Path mappingPath = basePath.resolve(routeId + ".yaml");
            File mappingFile = mappingPath.toFile();

            if (!mappingFile.exists()) {
                throw new MappingLoadException("Mapping file not found for routeId: " + routeId + 
                    ". Searched at: " + mappingPath.toAbsolutePath() + 
                    ". Current working directory: " + System.getProperty("user.dir") +
                    ". Base path: " + mappingsBasePath);
            }

            try (InputStream inputStream = new FileInputStream(mappingFile)) {
                MappingConfig config = yaml.loadAs(inputStream, MappingConfig.class);
                if (config == null) {
                    throw new MappingLoadException("Failed to parse YAML for routeId: " + routeId);
                }
                config.setRouteId(routeId);
                return config;
            }
        } catch (FileNotFoundException e) {
            Path basePath = Paths.get(mappingsBasePath);
            if (!basePath.isAbsolute()) {
                basePath = Paths.get(System.getProperty("user.dir")).resolve(mappingsBasePath);
            }
            Path mappingPath = basePath.resolve(routeId + ".yaml");
            throw new MappingLoadException("Mapping file not found for routeId: " + routeId + 
                ". Searched at: " + mappingPath.toAbsolutePath() + 
                ". Current working directory: " + System.getProperty("user.dir") +
                ". Base path: " + mappingsBasePath, e);
        } catch (MappingLoadException e) {
            // Re-throw our own exceptions as-is
            throw e;
        } catch (Exception e) {
            Path basePath = Paths.get(mappingsBasePath);
            if (!basePath.isAbsolute()) {
                basePath = Paths.get(System.getProperty("user.dir")).resolve(mappingsBasePath);
            }
            Path mappingPath = basePath.resolve(routeId + ".yaml");
            throw new MappingLoadException("Error loading mapping config for routeId: " + routeId + 
                ". Searched at: " + mappingPath.toAbsolutePath() + 
                ". Current working directory: " + System.getProperty("user.dir") +
                ". Base path: " + mappingsBasePath + 
                ". Original error: " + e.getMessage(), e);
        }
    }

    public static class MappingLoadException extends Exception {
        public MappingLoadException(String message) {
            super(message);
        }

        public MappingLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

