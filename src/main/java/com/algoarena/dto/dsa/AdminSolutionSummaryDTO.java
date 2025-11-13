// src/main/java/com/algoarena/dto/dsa/AdminSolutionSummaryDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.QuestionLevel;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for admin solution summary list
 * Contains only essential info needed for listing (no full content)
 */
public class AdminSolutionSummaryDTO {
    
    private String id;
    private String questionTitle;
    private String questionId;
    private int imageCount;
    private String codeLanguage;
    private int visualizerCount;
    private String categoryName;
    private QuestionLevel questionLevel;
    private String createdByName;
    private LocalDateTime updatedAt;
    
    // Constructors
    public AdminSolutionSummaryDTO() {}
    
    public AdminSolutionSummaryDTO(String id, String questionTitle, String questionId,
                                  int imageCount, String codeLanguage, int visualizerCount,
                                  String categoryName, QuestionLevel questionLevel,
                                  String createdByName, LocalDateTime updatedAt) {
        this.id = id;
        this.questionTitle = questionTitle;
        this.questionId = questionId;
        this.imageCount = imageCount;
        this.codeLanguage = codeLanguage;
        this.visualizerCount = visualizerCount;
        this.categoryName = categoryName;
        this.questionLevel = questionLevel;
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
    
    public String getQuestionTitle() {
        return questionTitle;
    }
    
    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }
    
    public String getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
    
    public int getImageCount() {
        return imageCount;
    }
    
    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }
    
    public String getCodeLanguage() {
        return codeLanguage;
    }
    
    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }
    
    public int getVisualizerCount() {
        return visualizerCount;
    }
    
    public void setVisualizerCount(int visualizerCount) {
        this.visualizerCount = visualizerCount;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public QuestionLevel getQuestionLevel() {
        return questionLevel;
    }
    
    public void setQuestionLevel(QuestionLevel questionLevel) {
        this.questionLevel = questionLevel;
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
}