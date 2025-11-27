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
                        "/api/questions/**", // Question endpoints (30/min read)
                        "/api/categories/**", // NEW: Category endpoints (30/min read)
                        "/api/solutions/**", // NEW: Solution endpoints (30/min read)
                        "/api/approaches/**", // Approach endpoints (5/min write, 20/min read)
                        "/api/user/me/**", // User endpoints (10/min for mark/unmark)
                        "/api/auth/me", // NEW
                        "/api/auth/refresh");
    }
}