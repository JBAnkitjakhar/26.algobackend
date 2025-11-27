// src/main/java/com/algoarena/model/CourseDoc.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "course_docs")
public class CourseDoc {

    @Id
    private String id;

    private String title;
    
    // INDEXED: For fast topic-based queries
    @Indexed
    private String topicId;
    
    private String content;
    private List<String> imageUrls;
    private Integer displayOrder;
    private Long totalSize;
    
    private static final Long MAX_SIZE = 5 * 1024 * 1024L; // 5MB
    
    private String createdById;
    private String createdByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;

    // Constructors
    public CourseDoc() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalSize = 0L;
    }

    public CourseDoc(String title, String topicId, User createdBy) {
        this();
        this.title = title;
        this.topicId = topicId;
        if (createdBy != null) {
            this.createdById = createdBy.getId();
            this.createdByName = createdBy.getName();
        }
    }

    // Getters and Setters (same as before)
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
        this.updatedAt = LocalDateTime.now();
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
        this.updatedAt = LocalDateTime.now();
    }

    public static Long getMaxSize() {
        return MAX_SIZE;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean exceedsSizeLimit() {
        return this.totalSize != null && this.totalSize > MAX_SIZE;
    }

    @Override
    public String toString() {
        return "CourseDoc{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", topicId='" + topicId + '\'' +
                ", totalSize=" + totalSize +
                '}';
    }
}