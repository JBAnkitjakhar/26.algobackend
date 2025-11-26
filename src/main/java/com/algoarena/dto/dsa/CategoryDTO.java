// File: src/main/java/com/algoarena/dto/dsa/CategoryDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category DTO with embedded question IDs
 * Used for API responses with complete category information
 */
public class CategoryDTO {

    private String id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;

    private Integer displayOrder;
    
    // Question IDs by level
    private List<String> easyQuestionIds = new ArrayList<>();
    private List<String> mediumQuestionIds = new ArrayList<>();
    private List<String> hardQuestionIds = new ArrayList<>();
    
    // Counts
    private int easyCount;
    private int mediumCount;
    private int hardCount;
    private int totalQuestions;
    
    // Metadata - UPDATED: Denormalized creator fields
    private String createdByName;
    private String createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CategoryDTO() {}

    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.displayOrder = category.getDisplayOrder();
        this.easyQuestionIds = new ArrayList<>(category.getEasyQuestionIds());
        this.mediumQuestionIds = new ArrayList<>(category.getMediumQuestionIds());
        this.hardQuestionIds = new ArrayList<>(category.getHardQuestionIds());
        this.easyCount = category.getEasyCount();
        this.mediumCount = category.getMediumCount();
        this.hardCount = category.getHardCount();
        this.totalQuestions = category.getTotalQuestions();
        this.createdByName = category.getCreatedByName();
        this.createdById = category.getCreatedById();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }

    // Static factory method
    public static CategoryDTO fromEntity(Category category) {
        return new CategoryDTO(category);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<String> getEasyQuestionIds() {
        return easyQuestionIds;
    }

    public void setEasyQuestionIds(List<String> easyQuestionIds) {
        this.easyQuestionIds = easyQuestionIds;
    }

    public List<String> getMediumQuestionIds() {
        return mediumQuestionIds;
    }

    public void setMediumQuestionIds(List<String> mediumQuestionIds) {
        this.mediumQuestionIds = mediumQuestionIds;
    }

    public List<String> getHardQuestionIds() {
        return hardQuestionIds;
    }

    public void setHardQuestionIds(List<String> hardQuestionIds) {
        this.hardQuestionIds = hardQuestionIds;
    }

    public int getEasyCount() {
        return easyCount;
    }

    public void setEasyCount(int easyCount) {
        this.easyCount = easyCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(int mediumCount) {
        this.mediumCount = mediumCount;
    }

    public int getHardCount() {
        return hardCount;
    }

    public void setHardCount(int hardCount) {
        this.hardCount = hardCount;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
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