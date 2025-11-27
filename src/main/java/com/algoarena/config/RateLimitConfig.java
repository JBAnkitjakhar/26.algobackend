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
    private final Map<String, Bucket> categoryReadCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> solutionReadCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authCache = new ConcurrentHashMap<>();
    
    // NEW: Course endpoints rate limiting
    private final Map<String, Bucket> courseReadCache = new ConcurrentHashMap<>();

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

    public Bucket resolveCategoryReadBucket(String userId) {
        return categoryReadCache.computeIfAbsent(userId, k -> createCategoryReadBucket());
    }

    public Bucket resolveSolutionReadBucket(String userId) {
        return solutionReadCache.computeIfAbsent(userId, k -> createSolutionReadBucket());
    }

    // NEW: Course read endpoints (30/min)
    public Bucket resolveCourseReadBucket(String userId) {
        return courseReadCache.computeIfAbsent(userId, k -> createCourseReadBucket());
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

    private Bucket createQuestionReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createCategoryReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createSolutionReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    // NEW: 30 requests per minute for course reads
    private Bucket createCourseReadBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}