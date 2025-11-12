// src/main/java/com/algoarena/service/auth/CustomUserDetailsService.java
package com.algoarena.service.auth;

import com.algoarena.model.User;
import com.algoarena.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);
        
        // CRITICAL FIX: Try multiple lookup strategies since getUsername() can return:
        // 1. email (if available)
        // 2. githubUsername (if no email)  
        // 3. id (fallback)
        
        User user = null;
        
        // Strategy 1: Try loading by ID first (MongoDB IDs have specific format)
        if (username.length() == 24) { // MongoDB ObjectId length
            user = userRepository.findById(username).orElse(null);
            if (user != null) {
                logger.debug("User found by ID: {}", username);
                return user;
            }
        }
        
        // Strategy 2: Try loading by email
        user = userRepository.findByEmail(username).orElse(null);
        if (user != null) {
            logger.debug("User found by email: {}", username);
            return user;
        }
        
        // Strategy 3: Try loading by GitHub username
        user = userRepository.findByGithubUsername(username).orElse(null);
        if (user != null) {
            logger.debug("User found by GitHub username: {}", username);
            return user;
        }
        
        // If nothing found, throw exception
        logger.error("User not found with username/email/id: {}", username);
        throw new UsernameNotFoundException("User not found with identifier: " + username);
    }

    // Additional helper method to load user by ID explicitly
    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        return user;
    }
    
    // Additional helper method to load user by email explicitly
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return user;
    }
}