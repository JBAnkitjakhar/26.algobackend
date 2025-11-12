// src/main/java/com/algoarena/dto/course/CourseDocDTO.java
package com.algoarena.dto.course;

import com.algoarena.model.CourseDoc;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class CourseDocDTO {

    private String id;

    @NotBlank(message = "Document title is required")
    @Size(min = 3, max = 200, message = "Document title must be between 3 and 200 characters")
    private String title;

    @NotNull(message = "Topic is required")
    private String topicId;
    
    private String topicName;
    
    // Rich HTML content from frontend editor
    // Only included when fetching single document (not in listing)
    private String content;
    
    // Image URLs for tracking and cleanup
    private List<String> imageUrls;
    
    private Integer displayOrder;
    private Long totalSize;
    
    private String createdByName;
    private String createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CourseDocDTO() {}

    // Constructor WITHOUT content (for listing)
    public CourseDocDTO(CourseDoc doc, boolean includeContent) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.topicId = doc.getTopic() != null ? doc.getTopic().getId() : null;
        this.topicName = doc.getTopic() != null ? doc.getTopic().getName() : null;
        this.displayOrder = doc.getDisplayOrder();
        this.totalSize = doc.getTotalSize();
        this.imageUrls = doc.getImageUrls();
        
        // Only include content if requested (for single doc view)
        if (includeContent) {
            this.content = doc.getContent();
        }
        
        this.createdByName = doc.getCreatedBy() != null ? doc.getCreatedBy().getName() : null;
        this.createdById = doc.getCreatedBy() != null ? doc.getCreatedBy().getId() : null;
        this.createdAt = doc.getCreatedAt();
        this.updatedAt = doc.getUpdatedAt();
    }

    // Static factory methods
    public static CourseDocDTO fromEntity(CourseDoc doc) {
        return new CourseDocDTO(doc, false); // Without content
    }

    public static CourseDocDTO fromEntityWithContent(CourseDoc doc) {
        return new CourseDocDTO(doc, true); // With full content
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

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
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