// src/main/java/com/algoarena/dto/user/UserMeStatsDTO.java
package com.algoarena.dto.user;

import com.algoarena.model.QuestionLevel;
import java.time.LocalDateTime;
import java.util.List;

public class UserMeStatsDTO {
    
    private StatsOverview stats;
    private PaginatedSolvedQuestions solvedQuestions;
    
    public static class StatsOverview {
        private int totalSolved;
        private int easySolved;
        private int mediumSolved;
        private int hardSolved;
        private LocalDateTime lastSolvedAt;
        
        public StatsOverview() {}
        
        public StatsOverview(int totalSolved, int easySolved, int mediumSolved, int hardSolved, LocalDateTime lastSolvedAt) {
            this.totalSolved = totalSolved;
            this.easySolved = easySolved;
            this.mediumSolved = mediumSolved;
            this.hardSolved = hardSolved;
            this.lastSolvedAt = lastSolvedAt;
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
    }
    
    public static class SolvedQuestionInfo {
        private String questionId;
        private String title;
        private String category;
        private QuestionLevel level;
        private LocalDateTime solvedAt;
        
        public SolvedQuestionInfo() {}
        
        public SolvedQuestionInfo(String questionId, String title, String category, QuestionLevel level, LocalDateTime solvedAt) {
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
    
    public static class PaginatedSolvedQuestions {
        private List<SolvedQuestionInfo> questions;
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        
        public PaginatedSolvedQuestions() {}
        
        public PaginatedSolvedQuestions(List<SolvedQuestionInfo> questions, int currentPage, int pageSize, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            this.questions = questions;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }
        
        public List<SolvedQuestionInfo> getQuestions() {
            return questions;
        }
        
        public void setQuestions(List<SolvedQuestionInfo> questions) {
            this.questions = questions;
        }
        
        public int getCurrentPage() {
            return currentPage;
        }
        
        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
        
        public int getPageSize() {
            return pageSize;
        }
        
        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public boolean isHasNext() {
            return hasNext;
        }
        
        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }
        
        public boolean isHasPrevious() {
            return hasPrevious;
        }
        
        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }
    
    public UserMeStatsDTO() {}
    
    public UserMeStatsDTO(StatsOverview stats, PaginatedSolvedQuestions solvedQuestions) {
        this.stats = stats;
        this.solvedQuestions = solvedQuestions;
    }
    
    public StatsOverview getStats() {
        return stats;
    }
    
    public void setStats(StatsOverview stats) {
        this.stats = stats;
    }
    
    public PaginatedSolvedQuestions getSolvedQuestions() {
        return solvedQuestions;
    }
    
    public void setSolvedQuestions(PaginatedSolvedQuestions solvedQuestions) {
        this.solvedQuestions = solvedQuestions;
    }
}