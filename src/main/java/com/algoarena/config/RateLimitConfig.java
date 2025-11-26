// src/main/java/com/algoarena/config/RateLimitConfig.java
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
    private final Map<String, Bucket> approachWriteCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> approachReadCache = new ConcurrentHashMap<>();
    
    public Bucket resolveWriteBucket(String userId) {
        return writeCache.computeIfAbsent(userId, k -> createWriteBucket());
    }
    
    public Bucket resolveReadBucket(String userId) {
        return readCache.computeIfAbsent(userId, k -> createReadBucket());
    }

    public Bucket resolveApproachWriteBucket(String userId) {
        return approachWriteCache.computeIfAbsent(userId, k -> createApproachWriteBucket());
    }

    public Bucket resolveApproachReadBucket(String userId) {
        return approachReadCache.computeIfAbsent(userId, k -> createApproachReadBucket());
    }
    
    private Bucket createWriteBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    private Bucket createReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(60)
                .refillIntervally(60, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createApproachWriteBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createApproachReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, Duration.ofMinutes(1))
                .build();
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}