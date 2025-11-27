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
    private final Map<String, Bucket> questionReadCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> categoryReadCache = new ConcurrentHashMap<>(); // NEW
    private final Map<String, Bucket> solutionReadCache = new ConcurrentHashMap<>(); // NEW
    private final Map<String, Bucket> authCache = new ConcurrentHashMap<>();

    public Bucket resolveAuthBucket(String userId) {
        return authCache.computeIfAbsent(userId, k -> createAuthBucket());
    }

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

    public Bucket resolveQuestionReadBucket(String userId) {
        return questionReadCache.computeIfAbsent(userId, k -> createQuestionReadBucket());
    }

    // NEW: Category read endpoints (30/min)
    public Bucket resolveCategoryReadBucket(String userId) {
        return categoryReadCache.computeIfAbsent(userId, k -> createCategoryReadBucket());
    }

    // NEW: Solution read endpoints (30/min)
    public Bucket resolveSolutionReadBucket(String userId) {
        return solutionReadCache.computeIfAbsent(userId, k -> createSolutionReadBucket());
    }

    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // 10 requests per minute for writes (mark/unmark, etc.)
    private Bucket createWriteBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // 60 requests per minute for generic reads
    private Bucket createReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(60)
                .refillIntervally(60, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // 5 requests per minute for approach writes
    private Bucket createApproachWriteBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // 20 requests per minute for approach reads
    private Bucket createApproachReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // 30 requests per minute for question reads
    private Bucket createQuestionReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // NEW: 30 requests per minute for category reads
    private Bucket createCategoryReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // NEW: 30 requests per minute for solution reads
    private Bucket createSolutionReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}