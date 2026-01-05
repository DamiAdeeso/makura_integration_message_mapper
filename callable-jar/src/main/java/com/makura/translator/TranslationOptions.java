package com.makura.translator;

import lombok.Builder;
import lombok.Data;

/**
 * Options for translation operations
 * Allows configuring encryption, forwarding, and other settings per-request
 */
@Data
@Builder
public class TranslationOptions {
    
    /**
     * Route identifier
     */
    private String routeId;
    
    /**
     * Enable encryption (AES or PGP)
     */
    @Builder.Default
    private boolean encrypt = false;
    
    /**
     * Encryption type (AES or PGP)
     */
    private EncryptionType encryptionType;
    
    /**
     * Encryption key reference
     */
    private String encryptionKeyRef;
    
    /**
     * Enable HTTP forwarding
     */
    @Builder.Default
    private boolean forward = false;
    
    /**
     * Endpoint URL for forwarding
     */
    private String endpoint;
    
    /**
     * API key for forwarding request
     */
    private String forwardingApiKey;
    
    /**
     * Connect timeout for HTTP forwarding (milliseconds)
     */
    @Builder.Default
    private int connectTimeout = 5000;
    
    /**
     * Read timeout for HTTP forwarding (milliseconds)
     */
    @Builder.Default
    private int readTimeout = 30000;
    
    public enum EncryptionType {
        AES, PGP
    }
}




