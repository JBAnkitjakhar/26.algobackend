// src/main/java/com/algoarena/dto/dsa/AdminSolutionSummaryDTO.java
package com.algoarena.dto.dsa;

import java.time.LocalDateTime;

/**
 * ✅ SIMPLIFIED: Lightweight DTO for admin solution summary
 * Removed: questionTitle, categoryName, questionLevel (not stored in Solution anymore)
 * Kept: Only data directly available in Solution document
 */
public class AdminSolutionSummaryDTO {

    private String id;
    
    private String questionId;
    
    private int imageCount;
    private int visualizerCount;
    private String codeLanguage;
    
    private String createdByName;

    private LocalDateTime createdAt; 
    private LocalDateTime updatedAt;

    // Constructors
    public AdminSolutionSummaryDTO() {}

    public AdminSolutionSummaryDTO(String id, String questionId, int imageCount, 
                                  int visualizerCount, String codeLanguage,
                                  String createdByName, LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
        this.id = id;
        this.questionId = questionId;
        this.imageCount = imageCount;
        this.visualizerCount = visualizerCount;
        this.codeLanguage = codeLanguage;
        this.createdByName = createdByName;
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

    public int getVisualizerCount() {
        return visualizerCount;
    }

    public void setVisualizerCount(int visualizerCount) {
        this.visualizerCount = visualizerCount;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    // ✅ ADD THIS GETTER/SETTER
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AdminSolutionSummaryDTO{" +
                "id='" + id + '\'' +
                ", questionId='" + questionId + '\'' +
                ", imageCount=" + imageCount +
                ", visualizerCount=" + visualizerCount +
                ", codeLanguage='" + codeLanguage + '\'' +
                ", createdByName='" + createdByName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}