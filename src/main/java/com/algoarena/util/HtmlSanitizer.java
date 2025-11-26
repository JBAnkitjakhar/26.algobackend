// src/main/java/com/algoarena/util/HtmlSanitizer.java
package com.algoarena.util;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {
    
    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS);
    
    /**
     * Sanitize text content (removes all scripts, dangerous HTML)
     */
    public String sanitizeText(String input) {
        if (input == null) {
            return null;
        }
        return POLICY.sanitize(input);
    }
    
    /**
     * For code content - just escape HTML entities
     * Don't remove anything because it's meant to be CODE
     */
    public String sanitizeCode(String input) {
        if (input == null) {
            return null;
        }
        // Just escape HTML entities - don't remove anything
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}