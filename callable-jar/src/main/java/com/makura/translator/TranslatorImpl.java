package com.makura.translator;

import com.makura.translator.encryption.EncryptionService;
import com.makura.translator.forwarding.HttpForwardingClient;
import com.makura.translator.mapping.MappingConfig;
import com.makura.translator.mapping.MappingEngine;
import com.makura.translator.mapping.MappingLoader;

/**
 * Implementation of Translator interface.
 * Core translation logic that can be embedded in any Java application.
 * Supports optional encryption and HTTP forwarding.
 */
public class TranslatorImpl implements Translator {

    private final MappingLoader mappingLoader;
    private final MappingEngine mappingEngine;
    private final EncryptionService encryptionService;
    private final HttpForwardingClient forwardingClient;

    /**
     * Constructor with default mappings path (./mappings)
     */
    public TranslatorImpl() {
        this("./mappings");
    }

    /**
     * Constructor with custom mappings path
     * 
     * @param mappingsBasePath Base path for YAML mapping files
     */
    public TranslatorImpl(String mappingsBasePath) {
        this(mappingsBasePath, null, null);
    }

    /**
     * Constructor with all optional features
     * 
     * @param mappingsBasePath Base path for YAML mapping files
     * @param encryptionService Optional encryption service
     * @param forwardingClient Optional HTTP forwarding client
     */
    public TranslatorImpl(String mappingsBasePath, EncryptionService encryptionService, HttpForwardingClient forwardingClient) {
        this.mappingLoader = new MappingLoader(mappingsBasePath);
        this.mappingEngine = new MappingEngine();
        this.encryptionService = encryptionService;
        this.forwardingClient = forwardingClient;
    }

    @Override
    public TargetMessage translateRequest(SourceMessage request, String routeId) throws TranslationException {
        try {
            // Load mapping configuration
            MappingConfig mappingConfig = mappingLoader.loadMappingConfig(routeId);
            
            // Transform to target format
            String targetContent = mappingEngine.transformToTarget(request.getContent(), mappingConfig);
            
            return new TargetMessage(targetContent);
        } catch (MappingLoader.MappingLoadException e) {
            throw new TranslationException("Failed to load mapping for routeId: " + routeId, e);
        } catch (MappingEngine.MappingException e) {
            throw new TranslationException("Failed to translate request for routeId: " + routeId, e);
        } catch (Exception e) {
            throw new TranslationException("Unexpected error during translation: " + e.getMessage(), e);
        }
    }

    @Override
    public SourceMessage translateResponse(TargetMessage response, String routeId) throws TranslationException {
        try {
            // Load mapping configuration
            MappingConfig mappingConfig = mappingLoader.loadMappingConfig(routeId);
            
            // Determine target format (default to JSON if not specified)
            String targetFormat = mappingConfig.getInboundFormat() != null 
                ? mappingConfig.getInboundFormat() 
                : "JSON";
            
            // Transform from target format
            String sourceContent = mappingEngine.transformFromTarget(
                response.getContent(), 
                mappingConfig, 
                targetFormat
            );
            
            return new SourceMessage(sourceContent, targetFormat);
        } catch (MappingLoader.MappingLoadException e) {
            throw new TranslationException("Failed to load mapping for routeId: " + routeId, e);
        } catch (MappingEngine.MappingException e) {
            throw new TranslationException("Failed to translate response for routeId: " + routeId, e);
        } catch (Exception e) {
            throw new TranslationException("Unexpected error during translation: " + e.getMessage(), e);
        }
    }

    @Override
    public TranslationResult translateWithOptions(SourceMessage request, TranslationOptions options) throws TranslationException {
        try {
            // Load mapping configuration
            MappingConfig mappingConfig = mappingLoader.loadMappingConfig(options.getRouteId());
            
            // Transform to target format
            String targetContent = mappingEngine.transformToTarget(request.getContent(), mappingConfig);
            
            // Apply encryption if requested
            if (options.isEncrypt()) {
                if (encryptionService == null) {
                    throw new TranslationException("Encryption requested but EncryptionService not configured");
                }
                
                if (options.getEncryptionType() == TranslationOptions.EncryptionType.AES) {
                    targetContent = encryptionService.encryptAes(targetContent, options.getEncryptionKeyRef());
                } else if (options.getEncryptionType() == TranslationOptions.EncryptionType.PGP) {
                    targetContent = encryptionService.encryptPgp(targetContent, options.getEncryptionKeyRef());
                }
            }
            
            // Forward if requested
            if (options.isForward()) {
                if (forwardingClient == null) {
                    throw new TranslationException("Forwarding requested but HttpForwardingClient not configured");
                }
                
                String forwardingResponse = forwardingClient.forward(
                    options.getEndpoint(), 
                    targetContent, 
                    options.getForwardingApiKey()
                );
                
                return TranslationResult.withForwarding(targetContent, forwardingResponse);
            }
            
            return TranslationResult.withoutForwarding(targetContent);
            
        } catch (MappingLoader.MappingLoadException e) {
            throw new TranslationException("Failed to load mapping for routeId: " + options.getRouteId(), e);
        } catch (MappingEngine.MappingException e) {
            throw new TranslationException("Failed to translate request for routeId: " + options.getRouteId(), e);
        } catch (EncryptionService.EncryptionException e) {
            throw new TranslationException("Encryption failed: " + e.getMessage(), e);
        } catch (HttpForwardingClient.ForwardingException e) {
            throw new TranslationException("Forwarding failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new TranslationException("Unexpected error during translation: " + e.getMessage(), e);
        }
    }
}


