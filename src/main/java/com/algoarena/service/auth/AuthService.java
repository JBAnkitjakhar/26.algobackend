// src/main/java/com/algoarena/service/auth/AuthService.java
package com.algoarena.service.auth;

import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import com.algoarena.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    public User processOAuth2User(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        logger.info("=== Processing OAuth2 User ===");
        logger.info("Provider: {}", registrationId);
        logger.info("Attributes: {}", attributes);

        String providerId = extractProviderId(attributes, registrationId);
        String email = extractEmail(attributes, registrationId);
        String username = extractUsername(attributes, registrationId);
        String name = extractName(attributes, registrationId);
        String image = extractImage(attributes, registrationId);

        logger.info("Extracted - ProviderId: {}, Email: {}, Username: {}, Name: {}",
                providerId, email, username, name);

        // Validate that we have at least providerId
        if (providerId == null || providerId.trim().isEmpty()) {
            throw new RuntimeException("Provider ID is required but not found");
        }

        // Find existing user by provider ID (most reliable)
        User existingUser = findExistingUserByProviderId(registrationId, providerId);

        if (existingUser != null) {
            logger.info("User exists (ID: {}), updating info", existingUser.getId());
            return updateExistingUser(existingUser, email, name, image, username, providerId, registrationId);
        } else {
            logger.info("Creating new user for provider: {}", registrationId);
            return createNewUser(email, name, image, username, providerId, registrationId);
        }
    }

    /**
     * Find existing user by provider ID (most reliable identifier)
     */
    private User findExistingUserByProviderId(String registrationId, String providerId) {
        if ("google".equals(registrationId)) {
            return userRepository.findByGoogleId(providerId).orElse(null);
        } else if ("github".equals(registrationId)) {
            return userRepository.findByGithubId(providerId).orElse(null);
        }
        return null;
    }

    private User updateExistingUser(User user, String email, String name, String image,
            String username, String providerId, String registrationId) {
        boolean updated = false;

        // Track last login time
        user.setLastLogin(LocalDateTime.now());
        updated = true;
        logger.info("Updated lastLogin for user: {}", user.getId());

        // Update email if we got a real one and it's different (or if user didn't have
        // one before)
        if (email != null && !email.trim().isEmpty()) {
            if (user.getEmail() == null || !email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
                logger.info("Updated email to: {}", email);
            }
        }

        // Update name if different
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            updated = true;
        }

        // Update image if different
        if (image != null && !image.equals(user.getImage())) {
            user.setImage(image);
            updated = true;
        }

        // Update GitHub username if it's a GitHub login
        if ("github".equals(registrationId) && username != null) {
            if (user.getGithubUsername() == null || !username.equals(user.getGithubUsername())) {
                user.setGithubUsername(username);
                updated = true;
            }
        }

        // Update provider ID if not set (shouldn't happen, but just in case)
        if ("google".equals(registrationId) && (user.getGoogleId() == null || !providerId.equals(user.getGoogleId()))) {
            user.setGoogleId(providerId);
            updated = true;
        } else if ("github".equals(registrationId)
                && (user.getGithubId() == null || !providerId.equals(user.getGithubId()))) {
            user.setGithubId(providerId);
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            logger.info("Saving updated user: {}", user.getId());
            return userRepository.save(user);
        }

        logger.info("No updates needed for user: {}", user.getId());
        return user;
    }

    private User createNewUser(String email, String name, String image, String username,
            String providerId, String registrationId) {
        User newUser = new User();

        // Email is optional - only set if available
        if (email != null && !email.trim().isEmpty()) {
            newUser.setEmail(email);
            logger.info("Setting email: {}", email);
        } else {
            logger.info("No email available (private), will use username for identification");
        }

        // Set name (fallback to username if no name provided)
        if (name != null && !name.trim().isEmpty()) {
            newUser.setName(name);
        } else if (username != null && !username.trim().isEmpty()) {
            newUser.setName(username);
        } else {
            newUser.setName("User " + providerId);
        }

        newUser.setImage(image);

        // Store provider-specific data
        if ("google".equals(registrationId)) {
            newUser.setGoogleId(providerId);
        } else if ("github".equals(registrationId)) {
            newUser.setGithubId(providerId);
            newUser.setGithubUsername(username); // Always store GitHub username
            logger.info("Storing GitHub username: {}", username);
        }

        // Set role - check if this is the first user (make them superadmin)
        newUser.setRole(determineUserRole(email));

        // Set initial login time
        newUser.setLastLogin(LocalDateTime.now());

        logger.info("Creating new user - Name: {}, Email: {}, Username: {}",
                newUser.getName(), newUser.getEmail(), newUser.getGithubUsername());

        return userRepository.save(newUser);
    }

    private UserRole determineUserRole(String email) {
        // If this is the first user or specific email, make them superadmin
        long userCount = userRepository.countAllUsers();

        if (userCount == 0 || "ankitjakharabc@gmail.com".equals(email)) {
            return UserRole.SUPERADMIN;
        }

        return UserRole.USER;
    }

    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        String email = null;

        switch (registrationId) {
            case "google":
                email = (String) attributes.get("email");
                break;
            case "github":
                // GitHub email might be null if user has it private
                email = (String) attributes.get("email");
                if (email == null || email.trim().isEmpty()) {
                    logger.info("GitHub email is private/null - user will not have email stored");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported registration ID: " + registrationId);
        }

        return email;
    }

    private String extractUsername(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                // Google doesn't provide username, could extract from email
                String email = (String) attributes.get("email");
                return email != null ? email.split("@")[0] : null;
            case "github":
                return (String) attributes.get("login"); // GitHub username
            default:
                return null;
        }
    }

    private String extractName(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                // GitHub provides 'name' and 'login' fields
                String name = (String) attributes.get("name");
                if (name == null || name.trim().isEmpty()) {
                    name = (String) attributes.get("login"); // Fallback to username
                }
                return name;
            default:
                return "Unknown User";
        }
    }

    private String extractImage(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("picture");
            case "github":
                return (String) attributes.get("avatar_url");
            default:
                return null;
        }
    }

    private String extractProviderId(Map<String, Object> attributes, String registrationId) {
        switch (registrationId) {
            case "google":
                return (String) attributes.get("sub");
            case "github":
                Object id = attributes.get("id");
                return id != null ? String.valueOf(id) : null;
            default:
                throw new IllegalArgumentException("Unsupported registration ID: " + registrationId);
        }
    }

    // Helper method to get current authenticated user by email
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    // NEW: Helper method to get user by provider ID
    public User getUserByProviderId(String providerId, String provider) {
        if ("google".equals(provider)) {
            return userRepository.findByGoogleId(providerId)
                    .orElseThrow(() -> new RuntimeException("User not found with Google ID: " + providerId));
        } else if ("github".equals(provider)) {
            return userRepository.findByGithubId(providerId)
                    .orElseThrow(() -> new RuntimeException("User not found with GitHub ID: " + providerId));
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }
}