// File: src/main/java/com/algoarena/dto/dsa/CategoryMetadataDTO.java
package com.algoarena.dto.dsa;

import java.time.LocalDateTime;

/**
 * Lightweight DTO for category metadata
 * Used in admin forms for category selection
 * Returns: id, name, createdByName, question counts, createdAt, updatedAt
 */
public class CategoryMetadataDTO {
    
    private String id;
    private String name;
    private String createdByName;
    private int easyCount;
    private int mediumCount;
    private int hardCount;
    private int totalQuestions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public CategoryMetadataDTO() {}
    
    public CategoryMetadataDTO(String id, String name, String createdByName, 
                               int easyCount, int mediumCount, int hardCount, 
                               int totalQuestions, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdByName = createdByName;
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
        this.totalQuestions = totalQuestions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public int getEasyCount() {
        return easyCount;
    }
    
    public void setEasyCount(int easyCount) {
        this.easyCount = easyCount;
    }
    
    public int getMediumCount() {
        return mediumCount;
    }
    
    public void setMediumCount(int mediumCount) {
        this.mediumCount = mediumCount;
    }
    
    public int getHardCount() {
        return hardCount;
    }
    
    public void setHardCount(int hardCount) {
        this.hardCount = hardCount;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
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
}