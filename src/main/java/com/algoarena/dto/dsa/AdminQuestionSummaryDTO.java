// src/main/java/com/algoarena/dto/dsa/AdminQuestionSummaryDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.QuestionLevel;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for admin question summary list
 * Contains only essential info needed for listing (no full content)
 */
public class AdminQuestionSummaryDTO {
    
    private String id;
    private String title;
    private QuestionLevel level;
    private String categoryName;
    private Integer displayOrder;  // NEW: Display order field
    private int imageCount;
    private boolean hasCodeSnippets;
    private String createdByName;
    private LocalDateTime updatedAt;
    
    // Dynamic counts (fetched from repositories)
    private int solutionCount;
    // REMOVED: approachCount and solvedByCount
    
    // Constructors
    public AdminQuestionSummaryDTO() {}
    
    public AdminQuestionSummaryDTO(String id, String title, QuestionLevel level, 
                                  String categoryName, Integer displayOrder, int imageCount, 
                                  boolean hasCodeSnippets, String createdByName, 
                                  LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.level = level;
        this.categoryName = categoryName;
        this.displayOrder = displayOrder;
        this.imageCount = imageCount;
        this.hasCodeSnippets = hasCodeSnippets;
        this.createdByName = createdByName;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public QuestionLevel getLevel() {
        return level;
    }
    
    public void setLevel(QuestionLevel level) {
        this.level = level;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public int getImageCount() {
        return imageCount;
    }
    
    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }
    
    public boolean isHasCodeSnippets() {
        return hasCodeSnippets;
    }
    
    public void setHasCodeSnippets(boolean hasCodeSnippets) {
        this.hasCodeSnippets = hasCodeSnippets;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public int getSolutionCount() {
        return solutionCount;
    }
    
    public void setSolutionCount(int solutionCount) {
        this.solutionCount = solutionCount;
    }
}