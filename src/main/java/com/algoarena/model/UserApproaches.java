// src/main/java/com/algoarena/model/UserApproaches.java
package com.algoarena.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ONE document per USER containing ALL their approaches
 * Grouped by question ID for efficient queries
 * 
 * LIMITS:
 * - Maximum 3 approaches per question per user
 * - Total combined size of all 3 approaches ≤ 15KB per question
 * 
 * Structure:
 * {
 *   "_id": "userId123",
 *   "userId": "userId123",
 *   "userName": "John Doe",
 *   "approaches": {
 *     "questionId1": [approach1(2KB), approach2(10KB), approach3(3KB)], // Total: 15KB ✅
 *     "questionId2": [approach1(5KB)],
 *     ...
 *   },
 *   "totalApproaches": 4,
 *   "lastUpdated": "2025-11-21T..."
 * }
 */
@Document(collection = "user_approaches")
public class UserApproaches {

    // CONSTANTS
    public static final int MAX_APPROACHES_PER_QUESTION = 3;
    public static final int MAX_COMBINED_SIZE_PER_QUESTION_BYTES = 15 * 1024; // 15KB for ALL 3 approaches combined

    @Id
    private String id; // This is the USER ID (for easy lookup)

    @Indexed
    private String userId; // Same as id (for consistency)
    
    private String userName; // Denormalized for quick access
    
    // Map: questionId -> List of approaches for that question (MAX 3, total ≤ 15KB)
    private Map<String, List<ApproachData>> approaches = new HashMap<>();
    
    private int totalApproaches = 0; // Total count across all questions
    private LocalDateTime lastUpdated;

    // Constructors
    public UserApproaches() {
        this.lastUpdated = LocalDateTime.now();
    }

    public UserApproaches(String userId, String userName) {
        this();
        this.id = userId;
        this.userId = userId;
        this.userName = userName;
    }

    /**
     * Embedded class representing a single approach
     * No separate document - stored inside UserApproaches
     */
    public static class ApproachData {
        private String id; // Unique ID for this specific approach (UUID)
        private String questionId; // Question this approach belongs to
        private String questionTitle; // Denormalized for display
        
        private String textContent; // Explanation text
        private String codeContent; // Code solution
        private String codeLanguage; // Programming language
        
        private int contentSize; // Size of THIS approach in bytes
        
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructors
        public ApproachData() {
            this.id = UUID.randomUUID().toString();
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.codeLanguage = "java"; // DEFAULT: Java
        }

        public ApproachData(String questionId, String questionTitle, String textContent) {
            this();
            this.questionId = questionId;
            this.questionTitle = questionTitle;
            this.textContent = textContent;
            this.contentSize = calculateContentSize();
        }

        // Helper method to calculate content size
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

        // Update content size after any change
        public void updateContentSize() {
            this.contentSize = calculateContentSize();
        }

        // Getters and Setters
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

        public String getQuestionTitle() {
            return questionTitle;
        }

        public void setQuestionTitle(String questionTitle) {
            this.questionTitle = questionTitle;
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

    // Helper methods to manage approaches with validation

    /**
     * Calculate TOTAL size of all approaches for a specific question
     * @param questionId The question ID
     * @return Total size in bytes of all approaches for this question
     */
    public int getTotalSizeForQuestion(String questionId) {
        List<ApproachData> questionApproaches = approaches.get(questionId);
        if (questionApproaches == null) {
            return 0;
        }
        return questionApproaches.stream()
                .mapToInt(ApproachData::getContentSize)
                .sum();
    }

    /**
     * Get remaining bytes available for a question
     * @param questionId The question ID
     * @return Remaining bytes (0 to 15KB)
     */
    public int getRemainingBytesForQuestion(String questionId) {
        int totalSize = getTotalSizeForQuestion(questionId);
        return Math.max(0, MAX_COMBINED_SIZE_PER_QUESTION_BYTES - totalSize);
    }

    /**
     * Get remaining KB available for a question
     * @param questionId The question ID
     * @return Remaining KB (formatted)
     */
    public double getRemainingKBForQuestion(String questionId) {
        return getRemainingBytesForQuestion(questionId) / 1024.0;
    }

    /**
     * Check if user can add more approaches for a specific question
     * @param questionId The question ID to check
     * @return true if user can add more (< 3 approaches), false otherwise
     */
    public boolean canAddApproach(String questionId) {
        List<ApproachData> questionApproaches = approaches.get(questionId);
        return questionApproaches == null || questionApproaches.size() < MAX_APPROACHES_PER_QUESTION;
    }

    /**
     * Get remaining approach slots for a question
     * @param questionId The question ID
     * @return Number of approaches user can still add (0-3)
     */
    public int getRemainingApproachSlots(String questionId) {
        List<ApproachData> questionApproaches = approaches.get(questionId);
        int currentCount = questionApproaches != null ? questionApproaches.size() : 0;
        return MAX_APPROACHES_PER_QUESTION - currentCount;
    }

    /**
     * Check if adding a new approach would exceed the 15KB limit for a question
     * @param questionId The question ID
     * @param newApproachSize Size of the new approach in bytes
     * @return true if it fits within 15KB limit, false otherwise
     */
    public boolean canAddApproachSize(String questionId, int newApproachSize) {
        int currentTotal = getTotalSizeForQuestion(questionId);
        return (currentTotal + newApproachSize) <= MAX_COMBINED_SIZE_PER_QUESTION_BYTES;
    }

    /**
     * Add approach with validation
     * @throws RuntimeException if limits exceeded
     */
    public void addApproach(String questionId, ApproachData approach) {
        // Validation 1: Check 3-approach limit
        if (!canAddApproach(questionId)) {
            throw new RuntimeException("Maximum " + MAX_APPROACHES_PER_QUESTION + 
                                     " approaches allowed per question. Please delete an existing approach first.");
        }

        // Validation 2: Check combined size limit (15KB total for all 3)
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

        // Add approach
        approaches.computeIfAbsent(questionId, k -> new ArrayList<>()).add(approach);
        totalApproaches++;
        lastUpdated = LocalDateTime.now();
    }

    /**
     * Update existing approach with size validation
     */
    public void updateApproach(String approachId, String textContent, String codeContent, String codeLanguage) {
        ApproachData approach = findApproachById(approachId);
        if (approach == null) {
            throw new RuntimeException("Approach not found with id: " + approachId);
        }

        // Store old size
        int oldSize = approach.getContentSize();
        String questionId = approach.getQuestionId();

        // Temporarily update to calculate new size
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

        // Validation: Check if update would exceed 15KB combined limit
        int currentTotal = getTotalSizeForQuestion(questionId);
        int adjustedTotal = currentTotal - oldSize + newSize; // Remove old, add new

        if (adjustedTotal > MAX_COMBINED_SIZE_PER_QUESTION_BYTES) {
            // Rollback changes
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

    /**
     * Remove approach
     */
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

    /**
     * Find specific approach by ID
     */
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

    /**
     * Get all approaches for a specific question
     */
    public List<ApproachData> getApproachesForQuestion(String questionId) {
        return approaches.getOrDefault(questionId, new ArrayList<>());
    }

    /**
     * Get count of approaches for a specific question
     */
    public int getApproachCountForQuestion(String questionId) {
        return approaches.getOrDefault(questionId, new ArrayList<>()).size();
    }

    /**
     * Get all approaches flattened (for "my approaches" list)
     */
    public List<ApproachData> getAllApproachesFlat() {
        List<ApproachData> allApproaches = new ArrayList<>();
        for (List<ApproachData> questionApproaches : approaches.values()) {
            allApproaches.addAll(questionApproaches);
        }
        // Sort by most recent first
        allApproaches.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return allApproaches;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.userId = id; // Keep in sync
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        this.id = userId; // Keep in sync
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
                ", questionsWithApproaches=" + approaches.size() +
                '}';
    }
}
