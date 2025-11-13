// src/main/java/com/algoarena/dto/user/QuestionsMetadataDTO.java
package com.algoarena.dto.user;

import com.algoarena.model.QuestionLevel;
import java.util.Map;

/**
 * Lightweight metadata for all questions
 * Used for dropdown selectors and displaying question titles
 */
public class QuestionsMetadataDTO {
    
    // Map of question ID to metadata
    private Map<String, QuestionMetadata> questions;
    
    /**
     * Nested class for individual question metadata
     */
    public static class QuestionMetadata {
        private String id;
        private String title;
        private QuestionLevel level;
        
        // Constructor
        public QuestionMetadata() {}
        
        public QuestionMetadata(String id, String title, QuestionLevel level) {
            this.id = id;
            this.title = title;
            this.level = level;
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
        
        public QuestionLevel getLevel() {
            return level;
        }
        
        public void setLevel(QuestionLevel level) {
            this.level = level;
        }
    }
    
    // Main class constructors
    public QuestionsMetadataDTO() {}
    
    public QuestionsMetadataDTO(Map<String, QuestionMetadata> questions) {
        this.questions = questions;
    }
    
    // Main class getters and setters
    public Map<String, QuestionMetadata> getQuestions() {
        return questions;
    }
    
    public void setQuestions(Map<String, QuestionMetadata> questions) {
        this.questions = questions;
    }
}