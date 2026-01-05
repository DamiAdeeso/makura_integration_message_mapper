package com.makura.translator;

import com.makura.translator.encryption.EncryptionService;
import com.makura.translator.forwarding.HttpForwardingClient;

/**
 * Builder for creating Translator instances with optional features
 */
public class TranslatorBuilder {
    
    private String mappingsPath = "./mappings";
    private String encryptionKeysPath;
    private boolean encryptionEnabled = false;
    private boolean forwardingEnabled = false;
    private int connectTimeout = 5000;
    private int readTimeout = 30000;
    
    /**
     * Set the path to YAML mapping files
     */
    public TranslatorBuilder withMappingsPath(String mappingsPath) {
        this.mappingsPath = mappingsPath;
        return this;
    }
    
    /**
     * Enable encryption support
     */
    public TranslatorBuilder withEncryption(String encryptionKeysPath) {
        this.encryptionEnabled = true;
        this.encryptionKeysPath = encryptionKeysPath;
        return this;
    }
    
    /**
     * Enable HTTP forwarding support
     */
    public TranslatorBuilder withForwarding() {
        this.forwardingEnabled = true;
        return this;
    }
    
    /**
     * Set HTTP client timeouts
     */
    public TranslatorBuilder withTimeouts(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        return this;
    }
    
    /**
     * Build the Translator instance
     */
    public Translator build() {
        EncryptionService encryptionService = null;
        if (encryptionEnabled) {
            if (encryptionKeysPath == null) {
                throw new IllegalStateException("Encryption keys path must be specified when encryption is enabled");
            }
            encryptionService = new EncryptionService(encryptionKeysPath);
        }
        
        HttpForwardingClient forwardingClient = null;
        if (forwardingEnabled) {
            forwardingClient = new HttpForwardingClient(connectTimeout, readTimeout);
        }
        
        return new TranslatorImpl(mappingsPath, encryptionService, forwardingClient);
    }
}


