package com.makura.translator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a source message from the originating system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceMessage {
    private String content;
    private String format; // JSON, SOAP, XML, PROPRIETARY_XML
    
    /**
     * Convenience constructor with content only (format will be null)
     */
    public SourceMessage(String content) {
        this.content = content;
        this.format = null;
    }
}

