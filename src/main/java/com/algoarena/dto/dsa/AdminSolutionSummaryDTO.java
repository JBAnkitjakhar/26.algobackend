// src/main/java/com/algoarena/dto/dsa/AdminSolutionSummaryDTO.java
package com.algoarena.dto.dsa;

import java.time.LocalDateTime;

public class AdminSolutionSummaryDTO {

    private String id;
    
    private String questionId;
    
    private int imageCount;
    private int visualizerCount;
    private String codeLanguage;
    
    // ✅ NEW FIELDS
    private boolean hasYoutubeLink;
    private boolean hasDriveLink;
    
    private String createdByName;

    private LocalDateTime createdAt; 
    private LocalDateTime updatedAt;

    // Constructors
    public AdminSolutionSummaryDTO() {}

    public AdminSolutionSummaryDTO(String id, String questionId, int imageCount, 
                                  int visualizerCount, String codeLanguage,
                                  boolean hasYoutubeLink, boolean hasDriveLink,
                                  String createdByName, LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
        this.id = id;
        this.questionId = questionId;
        this.imageCount = imageCount;
        this.visualizerCount = visualizerCount;
        this.codeLanguage = codeLanguage;
        this.hasYoutubeLink = hasYoutubeLink;
        this.hasDriveLink = hasDriveLink;
        this.createdByName = createdByName;
        this.createdAt = createdAt;           
        this.updatedAt = updatedAt;
    }

    // Existing Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public int getImageCount() { return imageCount; }
    public void setImageCount(int imageCount) { this.imageCount = imageCount; }

    public int getVisualizerCount() { return visualizerCount; }
    public void setVisualizerCount(int visualizerCount) { this.visualizerCount = visualizerCount; }

    public String getCodeLanguage() { return codeLanguage; }
    public void setCodeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; }

    // ✅ NEW GETTERS/SETTERS
    public boolean isHasYoutubeLink() { return hasYoutubeLink; }
    public void setHasYoutubeLink(boolean hasYoutubeLink) { this.hasYoutubeLink = hasYoutubeLink; }

    public boolean isHasDriveLink() { return hasDriveLink; }
    public void setHasDriveLink(boolean hasDriveLink) { this.hasDriveLink = hasDriveLink; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "AdminSolutionSummaryDTO{" +
                "id='" + id + '\'' +
                ", questionId='" + questionId + '\'' +
                ", imageCount=" + imageCount +
                ", visualizerCount=" + visualizerCount +
                ", codeLanguage='" + codeLanguage + '\'' +
                ", hasYoutubeLink=" + hasYoutubeLink +
                ", hasDriveLink=" + hasDriveLink +
                ", createdByName='" + createdByName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}