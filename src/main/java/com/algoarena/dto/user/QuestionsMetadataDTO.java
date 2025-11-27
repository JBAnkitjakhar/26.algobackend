// src/main/java/com/algoarena/dto/user/QuestionsMetadataDTO.java
package com.algoarena.dto.user;

import com.algoarena.model.QuestionLevel;
import java.util.Map;

public class QuestionsMetadataDTO {
    
    private Map<String, QuestionMetadata> questions;
    
    public static class QuestionMetadata {
        private String id;
        private String title;
        private QuestionLevel level;
        private String categoryId; // ONLY ID
        
        public QuestionMetadata() {}
        
        public QuestionMetadata(String id, String title, QuestionLevel level, String categoryId) {
            this.id = id;
            this.title = title;
            this.level = level;
            this.categoryId = categoryId;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public QuestionLevel getLevel() { return level; }
        public void setLevel(QuestionLevel level) { this.level = level; }
        
        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    }
    
    public QuestionsMetadataDTO() {}
    
    public QuestionsMetadataDTO(Map<String, QuestionMetadata> questions) {
        this.questions = questions;
    }
    
    public Map<String, QuestionMetadata> getQuestions() { return questions; }
    public void setQuestions(Map<String, QuestionMetadata> questions) { this.questions = questions; }
}