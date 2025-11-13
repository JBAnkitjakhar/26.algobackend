// src/main/java/com/algoarena/dto/user/UserProgressMapDTO.java
package com.algoarena.dto.user;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * User's progress map - contains only solved questions
 * Used to quickly check which questions user has solved
 */
public class UserProgressMapDTO {
    
    // Map of question ID to progress info (only solved questions)
    private Map<String, QuestionProgress> solvedQuestions;
    
    /**
     * Nested class for individual question progress
     */
    public static class QuestionProgress {
        private LocalDateTime solvedAt;
        private int approachCount;
        
        // Constructor
        public QuestionProgress() {}
        
        public QuestionProgress(LocalDateTime solvedAt, int approachCount) {
            this.solvedAt = solvedAt;
            this.approachCount = approachCount;
        }
        
        // Getters and Setters
        public LocalDateTime getSolvedAt() {
            return solvedAt;
        }
        
        public void setSolvedAt(LocalDateTime solvedAt) {
            this.solvedAt = solvedAt;
        }
        
        public int getApproachCount() {
            return approachCount;
        }
        
        public void setApproachCount(int approachCount) {
            this.approachCount = approachCount;
        }
    }
    
    // Main class constructors
    public UserProgressMapDTO() {}
    
    public UserProgressMapDTO(Map<String, QuestionProgress> solvedQuestions) {
        this.solvedQuestions = solvedQuestions;
    }
    
    // Main class getters and setters
    public Map<String, QuestionProgress> getSolvedQuestions() {
        return solvedQuestions;
    }
    
    public void setSolvedQuestions(Map<String, QuestionProgress> solvedQuestions) {
        this.solvedQuestions = solvedQuestions;
    }
}