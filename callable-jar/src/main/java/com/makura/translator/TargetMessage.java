package com.makura.translator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a target format message (typically XML).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetMessage {
    private String content; // XML content
}


