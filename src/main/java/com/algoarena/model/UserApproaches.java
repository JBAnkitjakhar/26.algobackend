// src/main/java/com/algoarena/model/UserApproaches.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(collection = "user_approaches")
public class UserApproaches {

    public static final int MAX_APPROACHES_PER_QUESTION = 3;
    public static final int MAX_COMBINED_SIZE_PER_QUESTION_BYTES = 15 * 1024;

    @Id
    private String id;

    @Version
    private Long version;

    @Indexed
    private String userId;
    
    private String userName;
    
    private Map<String, List<ApproachData>> approaches = new HashMap<>();
    
    private int totalApproaches = 0;
    private LocalDateTime lastUpdated;

    public UserApproaches() {
        this.lastUpdated = LocalDateTime.now();
    }

    public UserApproaches(String userId, String userName) {
        this();
        this.id = userId;
        this.userId = userId;
        this.userName = userName;
    }

    public static class ApproachData {
        private String id;
        private String questionId;
        
        private String textContent;
        private String codeContent;
        private String codeLanguage;
        
        private int contentSize;
        
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ApproachData() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.codeLanguage = "java";
        }

        public ApproachData(String questionId, String textContent) {
            this();
            this.questionId = questionId;
            this.textContent = textContent;
            this.contentSize = calculateContentSize();
        }

        public int calculateContentSize() {
            int size = 0;
            if (textContent != null) {
                size += textContent.getBytes().length;
            }
            if (codeContent != null) {
                size += codeContent.getBytes().length;
            }
            return size;
        }

        public void updateContentSize() {
            this.contentSize = calculateContentSize();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public String getTextContent() {
            return textContent;
        }

        public void setTextContent(String textContent) {
            this.textContent = textContent;
            this.contentSize = calculateContentSize();
            this.updatedAt = LocalDateTime.now();
        }

        public String getCodeContent() {
            return codeContent;
        }

        public void setCodeContent(String codeContent) {
            this.codeContent = codeContent;
            this.contentSize = calculateContentSize();
            this.updatedAt = LocalDateTime.now();
        }

        public String getCodeLanguage() {
            return codeLanguage;
        }

        public void setCodeLanguage(String codeLanguage) {
            this.codeLanguage = codeLanguage;
            this.updatedAt = LocalDateTime.now();
        }

        public int getContentSize() {
            return contentSize;
        }

        public void setContentSize(int contentSize) {
            this.contentSize = contentSize;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        @Override
        public String toString() {
            return "ApproachData{" +
                    "id='" + id + '\'' +
                    ", questionId='" + questionId + '\'' +
                    ", contentSize=" + contentSize +
                    '}';
        }
    }

    public int getTotalSizeForQuestion(String questionId) {
        List<ApproachData> questionApproaches = approaches.get(questionId);
        if (questionApproaches == null) {
            return 0;
        }
        return questionApproaches.stream()
                .mapToInt(ApproachData::getContentSize)
                .sum();
    }

    public int getRemainingBytesForQuestion(String questionId) {
        int totalSize = getTotalSizeForQuestion(questionId);
        return Math.max(0, MAX_COMBINED_SIZE_PER_QUESTION_BYTES - totalSize);
    }

    public double getRemainingKBForQuestion(String questionId) {
        return getRemainingBytesForQuestion(questionId) / 1024.0;
    }

    public boolean canAddApproach(String questionId) {
        List<ApproachData> questionApproaches = approaches.get(questionId);
        return questionApproaches == null || questionApproaches.size() < MAX_APPROACHES_PER_QUESTION;
    }

    public int getRemainingApproachSlots(String questionId) {
        List<ApproachData> questionApproaches = approaches.get(questionId);
        int currentCount = questionApproaches != null ? questionApproaches.size() : 0;
        return MAX_APPROACHES_PER_QUESTION - currentCount;
    }

    public boolean canAddApproachSize(String questionId, int newApproachSize) {
        int currentTotal = getTotalSizeForQuestion(questionId);
        return (currentTotal + newApproachSize) <= MAX_COMBINED_SIZE_PER_QUESTION_BYTES;
    }

    public void addApproach(String questionId, ApproachData approach) {
        if (!canAddApproach(questionId)) {
            throw new RuntimeException("Maximum " + MAX_APPROACHES_PER_QUESTION + 
                                     " approaches allowed per question. Please delete an existing approach first.");
        }

        int currentTotal = getTotalSizeForQuestion(questionId);
        int newTotal = currentTotal + approach.getContentSize();
        
        if (newTotal > MAX_COMBINED_SIZE_PER_QUESTION_BYTES) {
            double remainingKB = (MAX_COMBINED_SIZE_PER_QUESTION_BYTES - currentTotal) / 1024.0;
            double attemptedKB = approach.getContentSize() / 1024.0;
            throw new RuntimeException(
                String.format("Combined size limit exceeded! You have %.2f KB remaining for this question, " +
                            "but this approach is %.2f KB. Total limit is 15 KB across all 3 approaches.",
                            remainingKB, attemptedKB)
            );
        }

        approaches.computeIfAbsent(questionId, k -> new ArrayList<>()).add(approach);
        totalApproaches++;
        lastUpdated = LocalDateTime.now();
    }

    public void updateApproach(String approachId, String textContent, String codeContent, String codeLanguage) {
        ApproachData approach = findApproachById(approachId);
        if (approach == null) {
            throw new RuntimeException("Approach not found with id: " + approachId);
        }

        int oldSize = approach.getContentSize();
        String questionId = approach.getQuestionId();

        String oldText = approach.getTextContent();
        String oldCode = approach.getCodeContent();
        
        if (textContent != null) {
            approach.setTextContent(textContent);
        }
        if (codeContent != null) {
            approach.setCodeContent(codeContent);
        }
        if (codeLanguage != null) {
            approach.setCodeLanguage(codeLanguage);
        }

        int newSize = approach.getContentSize();

        int currentTotal = getTotalSizeForQuestion(questionId);
        int adjustedTotal = currentTotal - oldSize + newSize;

        if (adjustedTotal > MAX_COMBINED_SIZE_PER_QUESTION_BYTES) {
            approach.setTextContent(oldText);
            approach.setCodeContent(oldCode);
            approach.updateContentSize();
            
            double remainingKB = (MAX_COMBINED_SIZE_PER_QUESTION_BYTES - (currentTotal - oldSize)) / 1024.0;
            throw new RuntimeException(
                String.format("Update would exceed 15 KB combined limit! You have %.2f KB remaining for this question.",
                            remainingKB)
            );
        }

        approach.setUpdatedAt(LocalDateTime.now());
        lastUpdated = LocalDateTime.now();
    }

    public void removeApproach(String questionId, String approachId) {
        List<ApproachData> questionApproaches = approaches.get(questionId);
        if (questionApproaches != null) {
            boolean removed = questionApproaches.removeIf(a -> a.getId().equals(approachId));
            if (removed) {
                if (questionApproaches.isEmpty()) {
                    approaches.remove(questionId);
                }
                totalApproaches--;
                lastUpdated = LocalDateTime.now();
            }
        }
    }

    public ApproachData findApproachById(String approachId) {
        for (List<ApproachData> questionApproaches : approaches.values()) {
            for (ApproachData approach : questionApproaches) {
                if (approach.getId().equals(approachId)) {
                    return approach;
                }
            }
        }
        return null;
    }

    public List<ApproachData> getApproachesForQuestion(String questionId) {
        return approaches.getOrDefault(questionId, new ArrayList<>());
    }

    public int getApproachCountForQuestion(String questionId) {
        return approaches.getOrDefault(questionId, new ArrayList<>()).size();
    }

    public List<ApproachData> getAllApproachesFlat() {
        List<ApproachData> allApproaches = new ArrayList<>();
        for (List<ApproachData> questionApproaches : approaches.values()) {
            allApproaches.addAll(questionApproaches);
        }
        allApproaches.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return allApproaches;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.userId = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        this.id = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Map<String, List<ApproachData>> getApproaches() {
        return approaches;
    }

    public void setApproaches(Map<String, List<ApproachData>> approaches) {
        this.approaches = approaches;
    }

    public int getTotalApproaches() {
        return totalApproaches;
    }

    public void setTotalApproaches(int totalApproaches) {
        this.totalApproaches = totalApproaches;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "UserApproaches{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", totalApproaches=" + totalApproaches +
                ", version=" + version +
                ", questionsWithApproaches=" + approaches.size() +
                '}';
    }
}