// src/main/java/com/algoarena/dto/dsa/QuestionDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.Question;
import com.algoarena.model.QuestionLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionDTO {

    private String id;
    private Long version;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Statement is required")
    @Size(min = 20, max = 15000, message = "Statement must be between 20 and 15000 characters")
    private String statement;

    private List<String> imageUrls;
    private String imageFolderUrl;
    private List<CodeSnippetDTO> codeSnippets;

    @NotNull(message = "Category is required")
    private String categoryId; // ONLY ID - frontend maps to name

    @NotNull(message = "Level is required")
    private QuestionLevel level;
    
    private Integer displayOrder;

    private String createdByName;
    private String createdById;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class CodeSnippetDTO {
        private String language;
        private String code;
        private String description;

        public CodeSnippetDTO() {}

        public CodeSnippetDTO(Question.CodeSnippet codeSnippet) {
            this.language = codeSnippet.getLanguage();
            this.code = codeSnippet.getCode();
            this.description = codeSnippet.getDescription();
        }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public QuestionDTO() {}

    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.version = question.getVersion();
        this.title = question.getTitle();
        this.statement = question.getStatement();
        this.imageUrls = question.getImageUrls();
        this.imageFolderUrl = question.getImageFolderUrl();
        
        if (question.getCodeSnippets() != null) {
            this.codeSnippets = question.getCodeSnippets().stream()
                    .map(CodeSnippetDTO::new)
                    .toList();
        }
        
        this.categoryId = question.getCategoryId();
        this.level = question.getLevel();
        this.displayOrder = question.getDisplayOrder();
        this.createdByName = question.getCreatedByName();
        this.createdById = question.getCreatedById();
        this.createdAt = question.getCreatedAt();
        this.updatedAt = question.getUpdatedAt();
    }

    public static QuestionDTO fromEntity(Question question) {
        return new QuestionDTO(question);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getImageFolderUrl() { return imageFolderUrl; }
    public void setImageFolderUrl(String imageFolderUrl) { this.imageFolderUrl = imageFolderUrl; }

    public List<CodeSnippetDTO> getCodeSnippets() { return codeSnippets; }
    public void setCodeSnippets(List<CodeSnippetDTO> codeSnippets) { this.codeSnippets = codeSnippets; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public QuestionLevel getLevel() { return level; }
    public void setLevel(QuestionLevel level) { this.level = level; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getCreatedById() { return createdById; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}