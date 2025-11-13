// src/main/java/com/algoarena/dto/user/GlobalCategoryInfoDTO.java
package com.algoarena.dto.user;

import java.util.List;
import java.util.Map;

/**
 * Global category information (same for all users)
 * Contains category details + questions grouped by level + displayOrder
 */
public class GlobalCategoryInfoDTO {
    
    // Map of category ID to category info
    private Map<String, CategoryInfo> categories;
    
    /**
     * Nested class for individual category information
     */
    public static class CategoryInfo {
        private String id;
        private String name;
        private int totalQuestions;
        private int easyCount;
        private int mediumCount;
        private int hardCount;
        
        // Question IDs ordered by displayOrder
        private List<String> easyQuestionIds;
        private List<String> mediumQuestionIds;
        private List<String> hardQuestionIds;
        
        // Constructor
        public CategoryInfo() {}
        
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
        
        public int getTotalQuestions() {
            return totalQuestions;
        }
        
        public void setTotalQuestions(int totalQuestions) {
            this.totalQuestions = totalQuestions;
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
    }
    
    // Main class constructors
    public GlobalCategoryInfoDTO() {}
    
    public GlobalCategoryInfoDTO(Map<String, CategoryInfo> categories) {
        this.categories = categories;
    }
    
    // Main class getters and setters
    public Map<String, CategoryInfo> getCategories() {
        return categories;
    }
    
    public void setCategories(Map<String, CategoryInfo> categories) {
        this.categories = categories;
    }
}