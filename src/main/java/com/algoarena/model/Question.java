// src/main/java/com/algoarena/model/Question.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "questions")
public class Question {

    @Id
    private String id;

    @Version // For optimistic locking and thread safety
    private Long version;

    private String title;
    private String statement;
    
    private List<String> imageUrls;
    private String imageFolderUrl; // Backward compatibility
    
    private List<CodeSnippet> codeSnippets;

    // ISOLATED: Only store categoryId, not the full category object
    @Indexed
    private String categoryId;

    private QuestionLevel level;
    
    private Integer displayOrder;

    // ISOLATED: Store creator info directly, not DBRef
    private String createdById;
    private String createdByName;

    @Indexed(name = "createdAt_idx")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class CodeSnippet {
        private String language;
        private String code;
        private String description;

        public CodeSnippet() {}

        public CodeSnippet(String language, String code, String description) {
            this.language = language;
            this.code = code;
            this.description = description;
        }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public Question() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatement() { return statement; }
    public void setStatement(String statement) {
        this.statement = statement;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        this.updatedAt = LocalDateTime.now();
    }

    public String getImageFolderUrl() { return imageFolderUrl; }
    public void setImageFolderUrl(String imageFolderUrl) {
        this.imageFolderUrl = imageFolderUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public List<CodeSnippet> getCodeSnippets() { return codeSnippets; }
    public void setCodeSnippets(List<CodeSnippet> codeSnippets) {
        this.codeSnippets = codeSnippets;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        this.updatedAt = LocalDateTime.now();
    }

    public QuestionLevel getLevel() { return level; }
    public void setLevel(QuestionLevel level) {
        this.level = level;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCreatedById() { return createdById; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", level=" + level +
                ", version=" + version +
                '}';
    }
}