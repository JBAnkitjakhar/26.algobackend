// src/main/java/com/algoarena/config/WebConfig.java
package com.algoarena.config;

import com.algoarena.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/api/courses/**",      // NEW: Course endpoints (30/min read)
                        "/api/questions/**",    // Question endpoints (30/min read)
                        "/api/categories/**",   // Category endpoints (30/min read)
                        "/api/solutions/**",    // Solution endpoints (30/min read)
                        "/api/approaches/**",   // Approach endpoints (5/min write, 20/min read)
                        "/api/user/me/**",      // User endpoints (10/min for mark/unmark)
                        "/api/auth/me",         // Auth endpoints (20/min)
                        "/api/auth/refresh");
    }
}
// ```

// ---

// ## 📊 How Rate Limiting Works Behind the Scenes

// ### **Token Bucket Algorithm** (Bucket4j Implementation)
// ```
// USER1's Bucket (in RAM)          USER2's Bucket (in RAM)
// ┌─────────────────┐              ┌─────────────────┐
// │ Capacity: 30    │              │ Capacity: 30    │
// │ Tokens: 28 🪙   │              │ Tokens: 30 🪙   │
// │ Refill: 30/min  │              │ Refill: 30/min  │
// └─────────────────┘              └─────────────────┘