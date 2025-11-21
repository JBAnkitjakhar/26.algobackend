// src/main/java/com/algoarena/dto/user/CategoryProgressDTO.java

package com.algoarena.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public class CategoryProgressDTO {
    
    private String categoryId;
    private List<SolvedQuestionItem> solvedQuestions;
    private int totalSolved;
    
    public static class SolvedQuestionItem {
        private String questionId;
        private LocalDateTime solvedAt;
        
        public SolvedQuestionItem() {}
        
        public SolvedQuestionItem(String questionId, LocalDateTime solvedAt) {
            this.questionId = questionId;
            this.solvedAt = solvedAt;
        }
        
        public String getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }
        
        public LocalDateTime getSolvedAt() {
            return solvedAt;
        }
        
        public void setSolvedAt(LocalDateTime solvedAt) {
            this.solvedAt = solvedAt;
        }
    }
    
    public CategoryProgressDTO() {}
    
    public CategoryProgressDTO(String categoryId, List<SolvedQuestionItem> solvedQuestions, int totalSolved) {
        this.categoryId = categoryId;
        this.solvedQuestions = solvedQuestions;
        this.totalSolved = totalSolved;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    public List<SolvedQuestionItem> getSolvedQuestions() {
        return solvedQuestions;
    }
    
    public void setSolvedQuestions(List<SolvedQuestionItem> solvedQuestions) {
        this.solvedQuestions = solvedQuestions;
    }
    
    public int getTotalSolved() {
        return totalSolved;
    }
    
    public void setTotalSolved(int totalSolved) {
        this.totalSolved = totalSolved;
    }
}