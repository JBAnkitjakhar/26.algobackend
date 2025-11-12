// src/main/java/com/algoarena/model/CourseDoc.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "course_docs")
public class CourseDoc {

    @Id
    private String id;

    private String title;
    
    @DBRef
    private CourseTopic topic;
    
    // SIMPLE: Rich HTML content from frontend editor (TipTap, Quill, etc.)
    // Includes all formatting, images, code blocks, etc.
    private String content;
    
    // Track image URLs for cleanup when document is deleted
    // Frontend sends these after uploading to Cloudinary
    private List<String> imageUrls;
    
    // Display order within the topic
    private Integer displayOrder;
    
    // Total size in bytes (content + metadata)
    private Long totalSize;
    
    // Maximum size: 5MB
    private static final Long MAX_SIZE = 5 * 1024 * 1024L; // 5MB
    
    @DBRef
    private User createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CourseDoc() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalSize = 0L;
    }

    public CourseDoc(String title, CourseTopic topic, User createdBy) {
        this();
        this.title = title;
        this.topic = topic;
        this.createdBy = createdBy;
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
        this.updatedAt = LocalDateTime.now();
    }

    public CourseTopic getTopic() {
        return topic;
    }

    public void setTopic(CourseTopic topic) {
        this.topic = topic;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    // Helper method to check if size limit is exceeded
    public boolean exceedsSizeLimit() {
        return this.totalSize != null && this.totalSize > MAX_SIZE;
    }

    @Override
    public String toString() {
        return "CourseDoc{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", topic=" + (topic != null ? topic.getName() : "null") +
                ", totalSize=" + totalSize +
                '}';
    }
}