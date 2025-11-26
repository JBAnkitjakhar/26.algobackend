// File: src/main/java/com/algoarena/model/Category.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @Indexed
    private Integer displayOrder;

    // Question IDs grouped by level
    private List<String> easyQuestionIds = new ArrayList<>();
    private List<String> mediumQuestionIds = new ArrayList<>();
    private List<String> hardQuestionIds = new ArrayList<>();

    // Counts
    private int easyCount = 0;
    private int mediumCount = 0;
    private int hardCount = 0;
    private int totalQuestions = 0;

    // Creator info (denormalized) - placed before timestamps
    private String createdById;
    private String createdByName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Category() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Category(String name, String createdByName, String createdById) {
        this();
        this.name = name;
        this.createdByName = createdByName;
        this.createdById = createdById;
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
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getEasyQuestionIds() {
        return easyQuestionIds;
    }

    public void setEasyQuestionIds(List<String> easyQuestionIds) {
        this.easyQuestionIds = easyQuestionIds != null ? easyQuestionIds : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getMediumQuestionIds() {
        return mediumQuestionIds;
    }

    public void setMediumQuestionIds(List<String> mediumQuestionIds) {
        this.mediumQuestionIds = mediumQuestionIds != null ? mediumQuestionIds : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getHardQuestionIds() {
        return hardQuestionIds;
    }

    public void setHardQuestionIds(List<String> hardQuestionIds) {
        this.hardQuestionIds = hardQuestionIds != null ? hardQuestionIds : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public int getEasyCount() {
        return easyCount;
    }

    public void setEasyCount(int easyCount) {
        this.easyCount = easyCount;
        this.updatedAt = LocalDateTime.now();
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public void setMediumCount(int mediumCount) {
        this.mediumCount = mediumCount;
        this.updatedAt = LocalDateTime.now();
    }

    public int getHardCount() {
        return hardCount;
    }

    public void setHardCount(int hardCount) {
        this.hardCount = hardCount;
        this.updatedAt = LocalDateTime.now();
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
        this.updatedAt = LocalDateTime.now();
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

    // Helper methods
    public void addQuestionId(String questionId, QuestionLevel level) {
        switch (level) {
            case EASY:
                if (!this.easyQuestionIds.contains(questionId)) {
                    this.easyQuestionIds.add(questionId);
                }
                break;
            case MEDIUM:
                if (!this.mediumQuestionIds.contains(questionId)) {
                    this.mediumQuestionIds.add(questionId);
                }
                break;
            case HARD:
                if (!this.hardQuestionIds.contains(questionId)) {
                    this.hardQuestionIds.add(questionId);
                }
                break;
        }
        recalculateCounts();
    }

    public void removeQuestionId(String questionId, QuestionLevel level) {
        switch (level) {
            case EASY:
                this.easyQuestionIds.remove(questionId);
                break;
            case MEDIUM:
                this.mediumQuestionIds.remove(questionId);
                break;
            case HARD:
                this.hardQuestionIds.remove(questionId);
                break;
        }
        recalculateCounts();
    }

    public void recalculateCounts() {
        this.easyCount = this.easyQuestionIds.size();
        this.mediumCount = this.mediumQuestionIds.size();
        this.hardCount = this.hardQuestionIds.size();
        this.totalQuestions = this.easyCount + this.mediumCount + this.hardCount;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayOrder=" + displayOrder +
                ", totalQuestions=" + totalQuestions +
                ", easy=" + easyCount +
                ", medium=" + mediumCount +
                ", hard=" + hardCount +
                ", createdBy='" + createdByName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}