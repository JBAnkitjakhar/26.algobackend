// src/main/java/com/algoarena/dto/user/UserMeStatsDTO.java
package com.algoarena.dto.user;

import com.algoarena.model.QuestionLevel;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete user statistics for ME page
 * Returns all stats + recent activity in one API call
 */
public class UserMeStatsDTO {
    
    // Overall stats
    private int totalQuestions;
    private int totalSolved;
    private double progressPercentage;
    
    // Progress by level
    private LevelStatsDTO progressByLevel;
    
    // Recent activity (last 7 days)
    private int recentSolvedCount;
    
    // Recent solved questions (paginated, newest first)
    private List<RecentSolvedQuestionDTO> recentSolvedQuestions;
    
    // Nested class for level-wise stats
    public static class LevelStatsDTO {
        private int easyTotal;
        private int easySolved;
        private int mediumTotal;
        private int mediumSolved;
        private int hardTotal;
        private int hardSolved;
        
        // Constructors
        public LevelStatsDTO() {}
        
        public LevelStatsDTO(int easyTotal, int easySolved, int mediumTotal, 
                           int mediumSolved, int hardTotal, int hardSolved) {
            this.easyTotal = easyTotal;
            this.easySolved = easySolved;
            this.mediumTotal = mediumTotal;
            this.mediumSolved = mediumSolved;
            this.hardTotal = hardTotal;
            this.hardSolved = hardSolved;
        }
        
        // Getters and Setters
        public int getEasyTotal() { return easyTotal; }
        public void setEasyTotal(int easyTotal) { this.easyTotal = easyTotal; }
        
        public int getEasySolved() { return easySolved; }
        public void setEasySolved(int easySolved) { this.easySolved = easySolved; }
        
        public int getMediumTotal() { return mediumTotal; }
        public void setMediumTotal(int mediumTotal) { this.mediumTotal = mediumTotal; }
        
        public int getMediumSolved() { return mediumSolved; }
        public void setMediumSolved(int mediumSolved) { this.mediumSolved = mediumSolved; }
        
        public int getHardTotal() { return hardTotal; }
        public void setHardTotal(int hardTotal) { this.hardTotal = hardTotal; }
        
        public int getHardSolved() { return hardSolved; }
        public void setHardSolved(int hardSolved) { this.hardSolved = hardSolved; }
    }
    
    // Nested class for recent solved question
    public static class RecentSolvedQuestionDTO {
        private String questionId;
        private String title;
        private String category;
        private QuestionLevel level;
        private LocalDateTime solvedAt;
        private int approachCount;
        
        // Constructors
        public RecentSolvedQuestionDTO() {}
        
        public RecentSolvedQuestionDTO(String questionId, String title, String category,
                                      QuestionLevel level, LocalDateTime solvedAt, int approachCount) {
            this.questionId = questionId;
            this.title = title;
            this.category = category;
            this.level = level;
            this.solvedAt = solvedAt;
            this.approachCount = approachCount;
        }
        
        // Getters and Setters
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public QuestionLevel getLevel() { return level; }
        public void setLevel(QuestionLevel level) { this.level = level; }
        
        public LocalDateTime getSolvedAt() { return solvedAt; }
        public void setSolvedAt(LocalDateTime solvedAt) { this.solvedAt = solvedAt; }
        
        public int getApproachCount() { return approachCount; }
        public void setApproachCount(int approachCount) { this.approachCount = approachCount; }
    }
    
    // Main class constructors
    public UserMeStatsDTO() {}
    
    // Main class getters and setters
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public int getTotalSolved() { return totalSolved; }
    public void setTotalSolved(int totalSolved) { this.totalSolved = totalSolved; }
    
    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    
    public LevelStatsDTO getProgressByLevel() { return progressByLevel; }
    public void setProgressByLevel(LevelStatsDTO progressByLevel) { this.progressByLevel = progressByLevel; }
    
    public int getRecentSolvedCount() { return recentSolvedCount; }
    public void setRecentSolvedCount(int recentSolvedCount) { this.recentSolvedCount = recentSolvedCount; }
    
    public List<RecentSolvedQuestionDTO> getRecentSolvedQuestions() { return recentSolvedQuestions; }
    public void setRecentSolvedQuestions(List<RecentSolvedQuestionDTO> recentSolvedQuestions) { 
        this.recentSolvedQuestions = recentSolvedQuestions; 
    }
}