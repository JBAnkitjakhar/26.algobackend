// File: src/main/java/com/algoarena/config/RateLimitConfig.java
package com.algoarena.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {
    
    private final Map<String, Bucket> writeCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> readCache = new ConcurrentHashMap<>();
    
    /**
     * Get write operation bucket (strict limit)
     * Limit: 10 requests per minute
     */
    public Bucket resolveWriteBucket(String userId) {
        return writeCache.computeIfAbsent(userId, k -> createWriteBucket());
    }
    
    /**
     * Get read operation bucket (lenient limit)
     * Limit: 60 requests per minute
     */
    public Bucket resolveReadBucket(String userId) {
        return readCache.computeIfAbsent(userId, k -> createReadBucket());
    }
    
    private Bucket createWriteBucket() {
        // Strict: 10 requests per minute for writes
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    private Bucket createReadBucket() {
        // Lenient: 60 requests per minute for reads
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}