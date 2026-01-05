package com.makura.translator;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Result of a translation operation
 * Contains the translated target message and optional response from forwarding
 */
@Data
@AllArgsConstructor
public class TranslationResult {
    
    /**
     * The translated target format message
     */
    private String targetMessage;
    
    /**
     * Response from downstream system (if forwarded)
     */
    private String forwardingResponse;
    
    /**
     * Whether the message was successfully forwarded
     */
    private boolean forwarded;
    
    /**
     * Create a result without forwarding
     */
    public static TranslationResult withoutForwarding(String targetMessage) {
        return new TranslationResult(targetMessage, null, false);
    }
    
    /**
     * Create a result with forwarding response
     */
    public static TranslationResult withForwarding(String targetMessage, String forwardingResponse) {
        return new TranslationResult(targetMessage, forwardingResponse, true);
    }
}



