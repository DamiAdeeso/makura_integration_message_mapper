package com.makura.translator;

/**
 * Core translation interface for message translation.
 * This interface can be embedded in existing Java applications.
 */
public interface Translator {
    
    /**
     * Translates a source message to target format.
     * 
     * @param request The source message (can be JSON string, XML string, etc.)
     * @param routeId The route identifier to determine mapping configuration
     * @return Target format message as XML string
     * @throws TranslationException if translation fails
     */
    TargetMessage translateRequest(SourceMessage request, String routeId) throws TranslationException;
    
    /**
     * Translates a target format response back to source format.
     * 
     * @param response The target format response message
     * @param routeId The route identifier to determine mapping configuration
     * @return Source message in the expected format
     * @throws TranslationException if translation fails
     */
    SourceMessage translateResponse(TargetMessage response, String routeId) throws TranslationException;
    
    /**
     * Translates a source message to target format with advanced options.
     * Supports encryption and HTTP forwarding.
     * 
     * @param request The source message
     * @param options Translation options (encryption, forwarding, etc.)
     * @return Translation result including response if forwarded
     * @throws TranslationException if translation fails
     */
    TranslationResult translateWithOptions(SourceMessage request, TranslationOptions options) throws TranslationException;
    
    /**
     * Exception thrown when translation fails
     */
    class TranslationException extends Exception {
        public TranslationException(String message) {
            super(message);
        }
        
        public TranslationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


