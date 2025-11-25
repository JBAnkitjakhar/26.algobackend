// src/main/java/com/algoarena/dto/dsa/SolutionDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.Solution;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

public class SolutionDTO {

    private String id;

    // ✅ SIMPLIFIED: Only questionId (no title!)
    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Content is required")
    @Size(min = 20, message = "Content must be at least 20 characters")
    private String content;

    @Pattern(regexp = "^(https?://)?(drive\\.google\\.com|docs\\.google\\.com).*$|^$", 
             message = "Drive link must be a valid Google Drive URL")
    private String driveLink;

    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be).*$|^$", 
             message = "YouTube link must be a valid YouTube URL")
    private String youtubeLink;

    private List<String> imageUrls;
    private List<String> visualizerFileIds;
    private CodeSnippetDTO codeSnippet;

    // ✅ SIMPLIFIED: Only creator name (no ID!)
    private String createdByName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Inner DTO for code snippets
    public static class CodeSnippetDTO {
        private String language;
        private String code;
        private String description;

        public CodeSnippetDTO() {}

        public CodeSnippetDTO(Solution.CodeSnippet codeSnippet) {
            if (codeSnippet != null) {
                this.language = codeSnippet.getLanguage();
                this.code = codeSnippet.getCode();
                this.description = codeSnippet.getDescription();
            }
        }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Constructors
    public SolutionDTO() {}

    public SolutionDTO(Solution solution) {
        this.id = solution.getId();
        this.questionId = solution.getQuestionId();
        this.content = solution.getContent();
        this.driveLink = solution.getDriveLink();
        this.youtubeLink = solution.getYoutubeLink();
        this.imageUrls = solution.getImageUrls();
        this.visualizerFileIds = solution.getVisualizerFileIds();
        this.codeSnippet = solution.getCodeSnippet() != null ? 
                          new CodeSnippetDTO(solution.getCodeSnippet()) : null;
        this.createdByName = solution.getCreatedByName();
        this.createdAt = solution.getCreatedAt();
        this.updatedAt = solution.getUpdatedAt();
    }

    public static SolutionDTO fromEntity(Solution solution) {
        return new SolutionDTO(solution);
    }

    // Helper methods
    public boolean hasValidDriveLink() {
        return driveLink != null && driveLink.trim().length() > 0 && 
               driveLink.contains("drive.google.com");
    }

    public boolean hasValidYoutubeLink() {
        return youtubeLink != null && youtubeLink.trim().length() > 0 && 
               (youtubeLink.contains("youtube.com") || youtubeLink.contains("youtu.be"));
    }

    public String getYoutubeVideoId() {
        if (!hasValidYoutubeLink()) return null;
        try {
            String url = youtubeLink;
            if (url.contains("youtu.be/")) {
                return url.substring(url.lastIndexOf("/") + 1).split("\\?")[0];
            }
            if (url.contains("watch?v=")) {
                return url.substring(url.indexOf("watch?v=") + 8).split("&")[0];
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getYoutubeEmbedUrl() {
        String videoId = getYoutubeVideoId();
        return videoId != null ? "https://www.youtube.com/embed/" + videoId : null;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDriveLink() { return driveLink; }
    public void setDriveLink(String driveLink) { this.driveLink = driveLink; }

    public String getYoutubeLink() { return youtubeLink; }
    public void setYoutubeLink(String youtubeLink) { this.youtubeLink = youtubeLink; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getVisualizerFileIds() { return visualizerFileIds; }
    public void setVisualizerFileIds(List<String> visualizerFileIds) { this.visualizerFileIds = visualizerFileIds; }

    public CodeSnippetDTO getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(CodeSnippetDTO codeSnippet) { this.codeSnippet = codeSnippet; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}