// src/main/java/com/algoarena/model/ApproachMeta.java // on hold
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "approachmeta")
public class ApproachMeta {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId;
    
    private Map<String, List<ApproachInfo>> questionApproaches = new HashMap<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    
    public static class ApproachInfo {
        private String approachId;
        private String questionTitle;
        private LocalDateTime createdAt;
        private int size;
        private String codeLang;
        
        public ApproachInfo() {}
        
        public ApproachInfo(String approachId, String questionTitle, LocalDateTime createdAt, 
                          int size, String codeLang) {
            this.approachId = approachId;
            this.questionTitle = questionTitle;
            this.createdAt = createdAt;
            this.size = size;
            this.codeLang = codeLang;
        }
        
        public String getApproachId() {
            return approachId;
        }
        
        public void setApproachId(String approachId) {
            this.approachId = approachId;
        }
        
        public String getQuestionTitle() {
            return questionTitle;
        }
        
        public void setQuestionTitle(String questionTitle) {
            this.questionTitle = questionTitle;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
        
        public int getSize() {
            return size;
        }
        
        public void setSize(int size) {
            this.size = size;
        }
        
        public String getCodeLang() {
            return codeLang;
        }
        
        public void setCodeLang(String codeLang) {
            this.codeLang = codeLang;
        }
    }
    
    public ApproachMeta() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public ApproachMeta(String userId) {
        this.id = userId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public void addApproach(String questionId, String approachId, String questionTitle, 
                           int size, String codeLang) {
        List<ApproachInfo> approaches = questionApproaches.computeIfAbsent(questionId, k -> new ArrayList<>());
        
        if (approaches.size() >= 3) {
            throw new RuntimeException("Maximum 3 approaches allowed per question");
        }
        
        ApproachInfo info = new ApproachInfo(approachId, questionTitle, LocalDateTime.now(), size, codeLang);
        approaches.add(info);
        
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public void removeApproach(String questionId, String approachId) {
        List<ApproachInfo> approaches = questionApproaches.get(questionId);
        if (approaches != null) {
            approaches.removeIf(info -> info.getApproachId().equals(approachId));
            
            if (approaches.isEmpty()) {
                questionApproaches.remove(questionId);
            }
            
            this.lastUpdatedAt = LocalDateTime.now();
        }
    }
    
    public int getApproachCount(String questionId) {
        List<ApproachInfo> approaches = questionApproaches.get(questionId);
        return approaches != null ? approaches.size() : 0;
    }
    
    public boolean canAddApproach(String questionId) {
        return getApproachCount(questionId) < 3;
    }
    
    public List<ApproachInfo> getApproachesForQuestion(String questionId) {
        return questionApproaches.getOrDefault(questionId, new ArrayList<>());
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
    
    public Map<String, List<ApproachInfo>> getQuestionApproaches() {
        return questionApproaches;
    }
    
    public void setQuestionApproaches(Map<String, List<ApproachInfo>> questionApproaches) {
        this.questionApproaches = questionApproaches;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }
    
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}