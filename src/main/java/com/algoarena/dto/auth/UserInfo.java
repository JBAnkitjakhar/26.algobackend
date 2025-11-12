// src/main/java/com/algoarena/dto/auth/UserInfo.java
package com.algoarena.dto.auth;

import com.algoarena.model.User;
import com.algoarena.model.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in JSON response
public class UserInfo {
    private String id;
    private String name;
    private String email; // Can be null for GitHub users with private email
    private String image;
    private String githubUsername; // For displaying GitHub username
    private UserRole role;
    private LocalDateTime createdAt;

    // Constructors
    public UserInfo() {}

    public UserInfo(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail(); // Can be null
        this.image = user.getImage();
        this.githubUsername = user.getGithubUsername();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public boolean isAdmin() {
        return role == UserRole.ADMIN || role == UserRole.SUPERADMIN;
    }

    public boolean isSuperAdmin() {
        return role == UserRole.SUPERADMIN;
    }
    
    // NEW: Get display identifier (email or username)
    public String getDisplayIdentifier() {
        if (email != null && !email.isEmpty()) {
            return email;
        } else if (githubUsername != null && !githubUsername.isEmpty()) {
            return "@" + githubUsername;
        }
        return name;
    }
}