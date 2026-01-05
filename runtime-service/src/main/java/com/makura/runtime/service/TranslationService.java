package com.makura.runtime.service;

import com.makura.runtime.metrics.TranslationMetrics;
import com.makura.runtime.model.Route;
import com.makura.runtime.repository.RouteRepository;
import com.makura.translator.*;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Main translation service orchestrating the translation flow
 * Now delegates encryption and forwarding to the callable-jar
 */
@Slf4j
@Service
public class TranslationService {

    private final RouteService routeService;
    private final com.makura.translator.Translator translator;
    private final TranslationMetrics metrics;
    private final com.makura.runtime.mapping.MappingLoader cachedMappingLoader;

    public TranslationService(
            RouteService routeService,
            TranslationMetrics metrics,
            com.makura.runtime.mapping.MappingLoader cachedMappingLoader,
            @Value("${makura.runtime.mappings.base-path:./mappings}") String mappingsBasePath,
            @Value("${makura.runtime.encryption.keys-path:./keys}") String encryptionKeysPath) {
        this.routeService = routeService;
        this.cachedMappingLoader = cachedMappingLoader;
        this.metrics = metrics;
        
        // Create fully-featured Translator with encryption and forwarding support
        this.translator = new com.makura.translator.TranslatorBuilder()
            .withMappingsPath(mappingsBasePath)
            .withEncryption(encryptionKeysPath)
            .withForwarding()
            .build();
    }

    /**
     * Translate inbound request to target format and optionally forward
     */
    @Transactional(readOnly = true)
    public TranslationResult translateRequest(String routeId, String inboundContent, String correlationId) {
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        log.info("[{}] Processing translation request for routeId: {}", correlationId, routeId);

        metrics.recordTranslationRequest(routeId);
        Timer.Sample timer = metrics.startTimer();
        long startTimeNanos = System.nanoTime();

        try {
            // Load route configuration from cache (with DB fallback)
            Route route = routeService.getActiveRoute(routeId);

            // Load mapping config to get auth key (using cached MappingLoader)
            com.makura.translator.mapping.MappingConfig mappingConfig = 
                cachedMappingLoader.loadMappingConfig(routeId);

            // Prepare source message
            SourceMessage sourceMessage = new SourceMessage(inboundContent, route.getInboundFormat().name());

            // Check if we need advanced features (encryption/forwarding)
            boolean needsAdvancedFeatures = 
                (route.getEncryptionType() != null && route.getEncryptionType() != Route.EncryptionType.NONE) ||
                (route.getMode() == Route.RouteMode.ACTIVE && route.getEndpoint() != null);

            if (needsAdvancedFeatures) {
                // Use advanced API with options
                TranslationOptions.TranslationOptionsBuilder optionsBuilder = TranslationOptions.builder()
                    .routeId(routeId);

                // Configure encryption if needed
                if (route.getEncryptionType() != null && route.getEncryptionType() != Route.EncryptionType.NONE) {
                    optionsBuilder
                        .encrypt(true)
                        .encryptionType(
                            route.getEncryptionType() == Route.EncryptionType.AES 
                                ? TranslationOptions.EncryptionType.AES 
                                : TranslationOptions.EncryptionType.PGP
                        )
                        .encryptionKeyRef(route.getEncryptionKeyRef());
                }

                // Configure forwarding if ACTIVE mode
                if (route.getMode() == Route.RouteMode.ACTIVE && route.getEndpoint() != null) {
                    log.info("[{}] Forwarding target message to endpoint: {}", correlationId, route.getEndpoint());
                    optionsBuilder
                        .forward(true)
                        .endpoint(route.getEndpoint())
                        .forwardingApiKey(mappingConfig.getAuth() != null ? mappingConfig.getAuth().getKey() : null);
                }

                // Execute translation with options
                com.makura.translator.TranslationResult callableResult = 
                    translator.translateWithOptions(sourceMessage, optionsBuilder.build());

                // If forwarded, translate the response back to source format
                if (callableResult.isForwarded() && callableResult.getForwardingResponse() != null) {
                    com.makura.translator.TargetMessage responseTargetMessage = new com.makura.translator.TargetMessage(callableResult.getForwardingResponse());
                    SourceMessage responseSource = translator.translateResponse(responseTargetMessage, routeId);

                    long durationNanos = System.nanoTime() - startTimeNanos;
                    double durationMs = durationNanos / 1_000_000.0;
                    log.info("[{}] Translation completed successfully for routeId: {} in {}ms",
                        correlationId, routeId, String.format("%.2f", durationMs));
                    metrics.recordTranslationSuccess(routeId);
                    metrics.recordDuration(timer, routeId);
                    return TranslationResult.success(responseSource.getContent(), correlationId);
                } else {
                    // PASSIVE mode or no response - return the target message
                    long durationNanos = System.nanoTime() - startTimeNanos;
                    double durationMs = durationNanos / 1_000_000.0;
                    log.info("[{}] Translation completed successfully for routeId: {} in {}ms (PASSIVE mode)",
                        correlationId, routeId, String.format("%.2f", durationMs));
                    metrics.recordTranslationSuccess(routeId);
                    metrics.recordDuration(timer, routeId);
                    return TranslationResult.success(callableResult.getTargetMessage(), correlationId);
                }
            } else {
                // Simple translation without advanced features
                com.makura.translator.TargetMessage targetMessage = translator.translateRequest(sourceMessage, routeId);

                long durationNanos = System.nanoTime() - startTimeNanos;
                double durationMs = durationNanos / 1_000_000.0;
                log.info("[{}] Translation completed successfully for routeId: {} in {}ms",
                    correlationId, routeId, String.format("%.2f", durationMs));
                metrics.recordTranslationSuccess(routeId);
                metrics.recordDuration(timer, routeId);
                return TranslationResult.success(targetMessage.getContent(), correlationId);
            }

        } catch (RouteNotFoundException e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Route not found: {} (took {}ms)", correlationId, routeId, String.format("%.2f", durationMs));
            metrics.recordTranslationError(routeId, "ROUTE_NOT_FOUND");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Route not found: " + routeId, correlationId);
        } catch (com.makura.runtime.mapping.MappingLoader.MappingLoadException e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Mapping load error for routeId: {}. Details: {} (took {}ms)", 
                correlationId, routeId, e.getMessage(), String.format("%.2f", durationMs), e);
            metrics.recordTranslationError(routeId, "MAPPING_LOAD_ERROR");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Mapping configuration error: " + e.getMessage(), correlationId);
        } catch (com.makura.translator.Translator.TranslationException e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Translation error for routeId: {} (took {}ms)", correlationId, routeId, String.format("%.2f", durationMs), e);
            metrics.recordTranslationError(routeId, "TRANSLATION_ERROR");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Translation failed: " + e.getMessage(), correlationId);
        } catch (Exception e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Unexpected error during translation for routeId: {} (took {}ms)", 
                correlationId, routeId, String.format("%.2f", durationMs), e);
            metrics.recordTranslationError(routeId, "UNEXPECTED_ERROR");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Unexpected error: " + e.getMessage(), correlationId);
        }
    }

    /**
     * Translate target format response back to source format (for PASSIVE mode routes)
     */
    @Transactional(readOnly = true)
    public TranslationResult translateResponse(String routeId, String targetContent, String correlationId) {
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        log.info("[{}] Processing response translation for routeId: {}", correlationId, routeId);

        metrics.recordTranslationRequest(routeId);
        Timer.Sample timer = metrics.startTimer();
        long startTimeNanos = System.nanoTime();

        try {
            // Load route configuration from cache (with DB fallback)
            Route route = routeService.getActiveRoute(routeId);

            // Transform target format response back to source format
            com.makura.translator.TargetMessage targetMessage = new com.makura.translator.TargetMessage(targetContent);
            SourceMessage sourceMessage = translator.translateResponse(targetMessage, routeId);

            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.info("[{}] Response translation completed successfully for routeId: {} in {}ms",
                correlationId, routeId, String.format("%.2f", durationMs));
            metrics.recordTranslationSuccess(routeId);
            metrics.recordDuration(timer, routeId);
            return TranslationResult.success(sourceMessage.getContent(), correlationId);

        } catch (RouteNotFoundException e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Route not found: {} (took {}ms)", correlationId, routeId, String.format("%.2f", durationMs));
            metrics.recordTranslationError(routeId, "ROUTE_NOT_FOUND");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Route not found: " + routeId, correlationId);
        } catch (com.makura.runtime.mapping.MappingLoader.MappingLoadException e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Mapping load error for routeId: {}. Details: {} (took {}ms)", 
                correlationId, routeId, e.getMessage(), String.format("%.2f", durationMs), e);
            metrics.recordTranslationError(routeId, "MAPPING_LOAD_ERROR");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Mapping configuration error: " + e.getMessage(), correlationId);
        } catch (com.makura.translator.Translator.TranslationException e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Response translation error for routeId: {} (took {}ms)", correlationId, routeId, String.format("%.2f", durationMs), e);
            metrics.recordTranslationError(routeId, "TRANSLATION_ERROR");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Response translation failed: " + e.getMessage(), correlationId);
        } catch (Exception e) {
            long durationNanos = System.nanoTime() - startTimeNanos;
            double durationMs = durationNanos / 1_000_000.0;
            log.error("[{}] Unexpected error during response translation for routeId: {} (took {}ms)", 
                correlationId, routeId, String.format("%.2f", durationMs), e);
            metrics.recordTranslationError(routeId, "UNEXPECTED_ERROR");
            metrics.recordDuration(timer, routeId);
            return TranslationResult.error("Unexpected error: " + e.getMessage(), correlationId);
        }
    }

    /**
     * Result of translation operation
     */
    public static class TranslationResult {
        private final boolean success;
        private final String message;
        private final String correlationId;

        private TranslationResult(boolean success, String message, String correlationId) {
            this.success = success;
            this.message = message;
            this.correlationId = correlationId;
        }

        public static TranslationResult success(String message, String correlationId) {
            return new TranslationResult(true, message, correlationId);
        }

        public static TranslationResult error(String message, String correlationId) {
            return new TranslationResult(false, message, correlationId);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getCorrelationId() {
            return correlationId;
        }
    }

    public static class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(String message) {
            super(message);
        }
    }
}
