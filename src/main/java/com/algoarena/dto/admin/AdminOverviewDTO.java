// src/main/java/com/algoarena/dto/admin/AdminOverviewDTO.java
package com.algoarena.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for admin overview dashboard
 * Contains all statistics needed for the admin overview page
 */
public class AdminOverviewDTO {
    
    // Basic counts
    private long totalUsers;
    private long totalCategories;
    private long totalQuestions;
    private long totalSolutions;
    private long totalUserApproaches;
    private List<LoggedInUserDTO> usersLoggedInTodayDetails;

    // Today's activity
    private long usersLoggedInToday;
    
    // Last 7 days activity
    private long questionsLast7Days;
    private long solutionsLast7Days;
    private long newUsersLast7Days;
    
    // System health
    private SystemHealthStatus systemHealth;
    
    // Timestamp
    private LocalDateTime generatedAt;
    
    /**
     * Nested class for system health status
     */
    public static class SystemHealthStatus {
        private boolean databaseConnected;
        private String databaseStatus;
        // ❌ REMOVED: appVersion field
        // private String appVersion;
        
        // Constructor
        public SystemHealthStatus() {
            this.databaseConnected = true;
            this.databaseStatus = "Connected";
        }
        
        public SystemHealthStatus(boolean databaseConnected, String databaseStatus) {
            this.databaseConnected = databaseConnected;
            this.databaseStatus = databaseStatus;
        }
        
        // Getters and Setters
        public boolean isDatabaseConnected() {
            return databaseConnected;
        }
        
        public void setDatabaseConnected(boolean databaseConnected) {
            this.databaseConnected = databaseConnected;
        }
        
        public String getDatabaseStatus() {
            return databaseStatus;
        }
        
        public void setDatabaseStatus(String databaseStatus) {
            this.databaseStatus = databaseStatus;
        }
    }
    
    // Constructors
    public AdminOverviewDTO() {
        this.generatedAt = LocalDateTime.now();
        this.systemHealth = new SystemHealthStatus();
    }
    
    // Builder pattern for easier construction
    public static class Builder {
        private AdminOverviewDTO dto = new AdminOverviewDTO();
        
        public Builder totalUsers(long totalUsers) {
            dto.totalUsers = totalUsers;
            return this;
        }
        
        public Builder totalCategories(long totalCategories) {
            dto.totalCategories = totalCategories;
            return this;
        }
        
        public Builder totalQuestions(long totalQuestions) {
            dto.totalQuestions = totalQuestions;
            return this;
        }
        
        public Builder totalSolutions(long totalSolutions) {
            dto.totalSolutions = totalSolutions;
            return this;
        }
        
        public Builder totalUserApproaches(long totalUserApproaches) {
            dto.totalUserApproaches = totalUserApproaches;
            return this;
        }
        
        public Builder usersLoggedInTodayDetails(List<LoggedInUserDTO> users) {
            dto.usersLoggedInTodayDetails = users;
            return this;
        }
        
        public Builder usersLoggedInToday(long usersLoggedInToday) {
            dto.usersLoggedInToday = usersLoggedInToday;
            return this;
        }
        
        public Builder questionsLast7Days(long questionsLast7Days) {
            dto.questionsLast7Days = questionsLast7Days;
            return this;
        }
        
        public Builder solutionsLast7Days(long solutionsLast7Days) {
            dto.solutionsLast7Days = solutionsLast7Days;
            return this;
        }
        
        public Builder newUsersLast7Days(long newUsersLast7Days) {
            dto.newUsersLast7Days = newUsersLast7Days;
            return this;
        }
        
        public Builder systemHealth(SystemHealthStatus systemHealth) {
            dto.systemHealth = systemHealth;
            return this;
        }
        
        public AdminOverviewDTO build() {
            return dto;
        }
    }
    
    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getTotalCategories() {
        return totalCategories;
    }
    
    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }
    
    public long getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public long getTotalSolutions() {
        return totalSolutions;
    }
    
    public void setTotalSolutions(long totalSolutions) {
        this.totalSolutions = totalSolutions;
    }
    
    public long getTotalUserApproaches() {
        return totalUserApproaches;
    }
    
    public void setTotalUserApproaches(long totalUserApproaches) {
        this.totalUserApproaches = totalUserApproaches;
    }
    
    public List<LoggedInUserDTO> getUsersLoggedInTodayDetails() {
        return usersLoggedInTodayDetails;
    }
    
    public void setUsersLoggedInTodayDetails(List<LoggedInUserDTO> usersLoggedInTodayDetails) {
        this.usersLoggedInTodayDetails = usersLoggedInTodayDetails;
    }
    
    public long getUsersLoggedInToday() {
        return usersLoggedInToday;
    }
    
    public void setUsersLoggedInToday(long usersLoggedInToday) {
        this.usersLoggedInToday = usersLoggedInToday;
    }
    
    public long getQuestionsLast7Days() {
        return questionsLast7Days;
    }
    
    public void setQuestionsLast7Days(long questionsLast7Days) {
        this.questionsLast7Days = questionsLast7Days;
    }
    
    public long getSolutionsLast7Days() {
        return solutionsLast7Days;
    }
    
    public void setSolutionsLast7Days(long solutionsLast7Days) {
        this.solutionsLast7Days = solutionsLast7Days;
    }
    
    public long getNewUsersLast7Days() {
        return newUsersLast7Days;
    }
    
    public void setNewUsersLast7Days(long newUsersLast7Days) {
        this.newUsersLast7Days = newUsersLast7Days;
    }
    
    public SystemHealthStatus getSystemHealth() {
        return systemHealth;
    }
    
    public void setSystemHealth(SystemHealthStatus systemHealth) {
        this.systemHealth = systemHealth;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}