// src/main/java/com/algoarena/model/UserProgress.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "userprogress")
public class UserProgress {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId;
    
    private Map<String, SolvedQuestion> solvedQuestions = new HashMap<>();
    
    private int totalSolved = 0;
    private int easySolved = 0;
    private int mediumSolved = 0;
    private int hardSolved = 0;
    
    private LocalDateTime lastSolvedAt;
    private LocalDateTime createdAt;

    public static class SolvedQuestion {
        private String questionId;
        private String title;
        private String category;
        private QuestionLevel level;
        private LocalDateTime solvedAt;
        
        public SolvedQuestion() {}
        
        public SolvedQuestion(String questionId, String title, String category, 
                            QuestionLevel level, LocalDateTime solvedAt) {
            this.questionId = questionId;
            this.title = title;
            this.category = category;
            this.level = level;
            this.solvedAt = solvedAt;
        }
        
        public String getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public QuestionLevel getLevel() {
            return level;
        }
        
        public void setLevel(QuestionLevel level) {
            this.level = level;
        }
        
        public LocalDateTime getSolvedAt() {
            return solvedAt;
        }
        
        public void setSolvedAt(LocalDateTime solvedAt) {
            this.solvedAt = solvedAt;
        }
    }
    
    public UserProgress() {
        this.createdAt = LocalDateTime.now();
    }
    
    public UserProgress(String userId) {
        this.id = userId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
    
    public void addSolvedQuestion(String questionId, String title, String category, QuestionLevel level) {
        if (!solvedQuestions.containsKey(questionId)) {
            SolvedQuestion solved = new SolvedQuestion(questionId, title, category, level, LocalDateTime.now());
            solvedQuestions.put(questionId, solved);
            
            totalSolved++;
            switch (level) {
                case EASY:
                    easySolved++;
                    break;
                case MEDIUM:
                    mediumSolved++;
                    break;
                case HARD:
                    hardSolved++;
                    break;
            }
            
            lastSolvedAt = LocalDateTime.now();
        }
    }
    
    public boolean isQuestionSolved(String questionId) {
        return solvedQuestions.containsKey(questionId);
    }
    
    public SolvedQuestion getSolvedQuestion(String questionId) {
        return solvedQuestions.get(questionId);
    }
    
    public void removeSolvedQuestion(String questionId) {
        SolvedQuestion removed = solvedQuestions.remove(questionId);
        if (removed != null) {
            totalSolved--;
            switch (removed.getLevel()) {
                case EASY:
                    easySolved--;
                    break;
                case MEDIUM:
                    mediumSolved--;
                    break;
                case HARD:
                    hardSolved--;
                    break;
            }
        }
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
        this.id = userId;
    }
    
    public Map<String, SolvedQuestion> getSolvedQuestions() {
        return solvedQuestions;
    }
    
    public void setSolvedQuestions(Map<String, SolvedQuestion> solvedQuestions) {
        this.solvedQuestions = solvedQuestions;
    }
    
    public int getTotalSolved() {
        return totalSolved;
    }
    
    public void setTotalSolved(int totalSolved) {
        this.totalSolved = totalSolved;
    }
    
    public int getEasySolved() {
        return easySolved;
    }
    
    public void setEasySolved(int easySolved) {
        this.easySolved = easySolved;
    }
    
    public int getMediumSolved() {
        return mediumSolved;
    }
    
    public void setMediumSolved(int mediumSolved) {
        this.mediumSolved = mediumSolved;
    }
    
    public int getHardSolved() {
        return hardSolved;
    }
    
    public void setHardSolved(int hardSolved) {
        this.hardSolved = hardSolved;
    }
    
    public LocalDateTime getLastSolvedAt() {
        return lastSolvedAt;
    }
    
    public void setLastSolvedAt(LocalDateTime lastSolvedAt) {
        this.lastSolvedAt = lastSolvedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}