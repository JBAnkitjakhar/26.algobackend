// src/main/java/com/algoarena/dto/admin/LoggedInUserDTO.java
package com.algoarena.dto.admin;

/**
 * DTO for users who logged in today
 * Contains user details for admin overview
 */
public class LoggedInUserDTO {
    private String id;
    private String name;
    private String email;
    private String githubUsername;
    private String image;
    
    // Constructor
    public LoggedInUserDTO() {}
    
    public LoggedInUserDTO(String id, String name, String email, String githubUsername, String image) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.githubUsername = githubUsername;
        this.image = image;
    }
    
    // Getters and setters
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
    
    public String getGithubUsername() {
        return githubUsername;
    }
    
    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    // Helper to get display identifier
    public String getDisplayIdentifier() {
        if (email != null && !email.isEmpty()) {
            return email;
        } else if (githubUsername != null && !githubUsername.isEmpty()) {
            return "@" + githubUsername;
        }
        return name;
    }
}