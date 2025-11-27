// src/main/java/com/algoarena/interceptor/RateLimitInterceptor.java
package com.algoarena.interceptor;

import com.algoarena.config.RateLimitConfig;
import com.algoarena.exception.RateLimitExceededException;
import com.algoarena.model.User;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        User user = (User) authentication.getPrincipal();
        String userId = user.getId();
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        Bucket bucket = determineBucket(userId, requestURI, method);

        if (bucket == null) {
            return true; // No rate limiting for this endpoint
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            throw new RateLimitExceededException();
        }
    }

    private Bucket determineBucket(String userId, String requestURI, String method) {

        // ========================================
        // AUTH ENDPOINTS (20/min)
        // ========================================
        if (requestURI.startsWith("/api/auth/me") ||
                requestURI.startsWith("/api/auth/refresh")) {
            return rateLimitConfig.resolveAuthBucket(userId);
        }

        // ========================================
        // COURSE ENDPOINTS (30/min) - NEW
        // ========================================
        // Course read endpoints: topics, docs
        if (method.equals("GET") && requestURI.startsWith("/api/courses")) {
            return rateLimitConfig.resolveCourseReadBucket(userId);
        }

        // ========================================
        // SPECIFIC READ ENDPOINTS (30/min each)
        // ========================================

        // Question read endpoints
        if (method.equals("GET") &&
                (requestURI.matches("/api/questions/[^/]+") ||
                        requestURI.equals("/api/questions/metadata"))) {
            return rateLimitConfig.resolveQuestionReadBucket(userId);
        }

        // Category read endpoints
        if (method.equals("GET") && requestURI.startsWith("/api/categories")) {
            return rateLimitConfig.resolveCategoryReadBucket(userId);
        }

        // Solution read endpoints
        if (method.equals("GET") && requestURI.startsWith("/api/solutions")) {
            return rateLimitConfig.resolveSolutionReadBucket(userId);
        }

        // ========================================
        // APPROACH ENDPOINTS
        // ========================================

        // Approach write endpoints (5/min)
        if ((method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) &&
                requestURI.contains("/api/approaches")) {
            return rateLimitConfig.resolveApproachWriteBucket(userId);
        }

        // Approach read endpoints (20/min)
        if (method.equals("GET") && requestURI.contains("/api/approaches")) {
            return rateLimitConfig.resolveApproachReadBucket(userId);
        }

        // ========================================
        // USER MARK/UNMARK OPERATIONS (10/min)
        // ========================================
        if ((method.equals("POST") || method.equals("DELETE")) &&
                requestURI.contains("/api/user/me/")) {
            return rateLimitConfig.resolveWriteBucket(userId);
        }

        // ========================================
        // GENERIC FALLBACKS
        // ========================================

        // Generic write endpoints (10/min)
        if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
            return rateLimitConfig.resolveWriteBucket(userId);
        }

        // Generic read endpoints (60/min)
        if (method.equals("GET")) {
            return rateLimitConfig.resolveReadBucket(userId);
        }

        return null; // No rate limiting
    }
}