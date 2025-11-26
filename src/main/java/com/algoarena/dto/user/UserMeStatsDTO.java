// src/main/java/com/algoarena/dto/user/UserMeStatsDTO.java
package com.algoarena.dto.user;

import java.time.LocalDateTime;
import java.util.Map;

public class UserMeStatsDTO {
    
    private int totalSolved;
    private Map<String, LocalDateTime> solvedQuestions;  // questionId -> solvedAt
    
    public UserMeStatsDTO() {}
    
    public UserMeStatsDTO(int totalSolved, Map<String, LocalDateTime> solvedQuestions) {
        this.totalSolved = totalSolved;
        this.solvedQuestions = solvedQuestions;
    }
    
    public int getTotalSolved() {
        return totalSolved;
    }
    
    public void setTotalSolved(int totalSolved) {
        this.totalSolved = totalSolved;
    }
    
    public Map<String, LocalDateTime> getSolvedQuestions() {
        return solvedQuestions;
    }
    
    public void setSolvedQuestions(Map<String, LocalDateTime> solvedQuestions) {
        this.solvedQuestions = solvedQuestions;
    }
}