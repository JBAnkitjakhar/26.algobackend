// src/main/java/com/algoarena/config/SecurityConfig.java
// COMPLETE FILE - Replace your entire SecurityConfig.java with this

package com.algoarena.config;

import com.algoarena.security.JwtAuthenticationFilter;
import com.algoarena.security.OAuth2SuccessHandler;
import com.algoarena.security.OAuth2FailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private OAuth2FailureHandler oAuth2FailureHandler;

    @Autowired
    private AppConfig appConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS (No authentication required)
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/health",
                                "/status",
                                "/ping",
                                "/healthz",
                                "/actuator/**",
                                "/error")
                        .permitAll()

                        // ============================================
                        // COURSE ENDPOINTS - ADMIN CREATE/UPDATE/DELETE FIRST
                        // ============================================
                        
                        // COURSE IMAGES - ADMIN ONLY
                        .requestMatchers(HttpMethod.POST, "/courses/images").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/images").hasAnyRole("ADMIN", "SUPERADMIN")
                        
                        // COURSE TOPICS - ADMIN ONLY
                        .requestMatchers(HttpMethod.POST, "/courses/topics").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/topics/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/topics/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        
                        // COURSE DOCS - ADMIN ONLY
                        .requestMatchers(HttpMethod.POST, "/courses/docs").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/docs/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/docs/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        
                        // COURSE READ ACCESS - ALL AUTHENTICATED USERS
                        .requestMatchers(HttpMethod.GET, "/courses/**").authenticated()

                        // ============================================
                        // EXISTING DSA ENDPOINTS
                        // ============================================
                        
                        // AUTHENTICATED USER ENDPOINTS - READ ACCESS
                        .requestMatchers(HttpMethod.GET,
                                "/questions/summary",
                                "/questions",
                                "/questions/{id}",
                                "/categories/with-progress",
                                "/categories",
                                "/categories/{id}",
                                "/categories/{id}/stats",
                                "/categories/{id}/progress",
                                "/solutions/question/*",
                                "/solutions/{id}",
                                "/approaches/**",
                                "/compiler/**",
                                "/users/progress",
                                "/users/progress/recent",
                                "/files/solutions/*/visualizers",
                                "/files/visualizers/**"
                        ).authenticated()

                        // USER PROGRESS UPDATE ENDPOINTS
                        .requestMatchers(HttpMethod.PUT, "/questions/*/progress").authenticated()
                        .requestMatchers(HttpMethod.POST, "/questions/*/progress").authenticated()

                        // USER APPROACH MANAGEMENT ENDPOINTS
                        .requestMatchers(HttpMethod.POST, "/approaches/question/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/approaches/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/approaches/*").authenticated()

                        // ADMIN-ONLY ENDPOINTS
                        .requestMatchers(
                                "/admin/**",
                                "/questions/stats",
                                "/questions/search",
                                "/solutions/question/*/create",
                                "/solutions/*/update",
                                "/solutions/*/delete",
                                "/files/images/**",
                                "/files/visualizers/*/upload",
                                "/files/visualizers/*/delete",
                                "/files/visualizers/*/metadata",
                                "/files/visualizers/*/download"
                        ).hasAnyRole("ADMIN", "SUPERADMIN")

                        // ADMIN CREATE/UPDATE/DELETE OPERATIONS
                        .requestMatchers(HttpMethod.POST, "/questions", "/categories", "/solutions")
                        .hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/questions/*", "/categories/*", "/solutions/*")
                        .hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/questions/*", "/categories/*", "/solutions/*")
                        .hasAnyRole("ADMIN", "SUPERADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated())
                        
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"Authentication required\",\"message\":\"Please provide valid JWT token\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"Access denied\",\"message\":\"You don't have permission to access this resource\"}");
                        }))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*"))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String origins = appConfig.getCors().getAllowedOrigins();
        if (origins != null && !origins.trim().isEmpty()) {
            List<String> allowedOrigins = Arrays.asList(origins.split(","));
            configuration.setAllowedOriginPatterns(allowedOrigins);
        } else {
            configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        }

        String methods = appConfig.getCors().getAllowedMethods();
        if (methods != null && !methods.trim().isEmpty()) {
            configuration.setAllowedMethods(Arrays.asList(methods.split(",")));
        } else {
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(appConfig.getCors().isAllowCredentials());
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}