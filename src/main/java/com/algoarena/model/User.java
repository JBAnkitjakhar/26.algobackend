// src/main/java/com/algoarena/model/User.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;

    private String name;

    // CHANGED: Email is now optional - can be null for GitHub users with private email
    // IMPORTANT: Keep @Indexed but NOT unique=true since null emails are allowed
    @Indexed(unique = false, sparse = true)
    private String email;

    private String image;

    // CHANGED: Add index for Google ID for faster lookups
    @Indexed(unique = true, sparse = true)
    private String googleId;

    // CHANGED: Add index for GitHub ID for faster lookups
    @Indexed(unique = true, sparse = true)
    private String githubId;
    
    // NEW FIELD: Store GitHub username for users without email
    @Indexed(unique = false)
    private String githubUsername;

    private UserRole role = UserRole.USER;

    // ✅ CHANGED: Added index for admin overview "users logged in today" query
    @Indexed(name = "lastLogin_idx")
    private LocalDateTime lastLogin;

    // ✅ CHANGED: Added index for admin overview "new users" query
    @Indexed(name = "createdAt_idx")
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return null; // OAuth2 only, no password
    }

    @Override
    public String getUsername() {
        // CHANGED: Use email if available, otherwise use githubUsername, otherwise use ID
        if (email != null && !email.isEmpty()) {
            return email;
        } else if (githubUsername != null && !githubUsername.isEmpty()) {
            return githubUsername;
        }
        return id; // Fallback to ID
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
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
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        this.updatedAt = LocalDateTime.now();
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
        this.updatedAt = LocalDateTime.now();
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean isAdmin() {
        return role == UserRole.ADMIN || role == UserRole.SUPERADMIN;
    }

    public boolean isSuperAdmin() {
        return role == UserRole.SUPERADMIN;
    }

    // NEW: Helper to get display identifier (email or username)
    public String getDisplayIdentifier() {
        if (email != null && !email.isEmpty()) {
            return email;
        } else if (githubUsername != null && !githubUsername.isEmpty()) {
            return "@" + githubUsername;
        }
        return name != null ? name : id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", githubUsername='" + githubUsername + '\'' +
                ", role=" + role +
                ", lastLogin=" + lastLogin +
                ", createdAt=" + createdAt +
                '}';
    }
}