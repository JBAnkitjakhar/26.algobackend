// src/main/java/com/algoarena/dto/dsa/ApproachDetailDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.UserApproaches.ApproachData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Full DTO with all content (textContent + codeContent)
 * Used when viewing a single approach or creating/updating
 */
public class ApproachDetailDTO {

    private String id;
    private String questionId;
    private String questionTitle;
    private String userId;
    private String userName;

    @NotBlank(message = "Text content is required")
    @Size(min = 10, message = "Text content must be at least 10 characters")
    private String textContent;

    private String codeContent;

    @Size(max = 50, message = "Code language must not exceed 50 characters")
    private String codeLanguage;

    private int contentSize;
    private double contentSizeKB;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ApproachDetailDTO() {}

    public ApproachDetailDTO(ApproachData data, String userId, String userName) {
        this.id = data.getId();
        this.questionId = data.getQuestionId();
        this.questionTitle = data.getQuestionTitle();
        this.userId = userId;
        this.userName = userName;
        this.textContent = data.getTextContent();
        this.codeContent = data.getCodeContent();
        this.codeLanguage = data.getCodeLanguage();
        this.contentSize = data.getContentSize();
        this.contentSizeKB = data.getContentSize() / 1024.0;
        this.createdAt = data.getCreatedAt();
        this.updatedAt = data.getUpdatedAt();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getCodeContent() {
        return codeContent;
    }

    public void setCodeContent(String codeContent) {
        this.codeContent = codeContent;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    public int getContentSize() {
        return contentSize;
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
    }

    public double getContentSizeKB() {
        return contentSizeKB;
    }

    public void setContentSizeKB(double contentSizeKB) {
        this.contentSizeKB = contentSizeKB;
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
}
// ```

// ---

// ## 📁 **FILE STRUCTURE:**
// ```
// src/main/java/com/algoarena/
// ├── dto/
// │   └── dsa/
// │       ├── ApproachMetadataDTO.java  ← Lightweight (no content)
// │       └── ApproachDetailDTO.java    ← Full content
// ├── model/
// │   └── UserApproaches.java           ← Already provided
// ├── repository/
// │   └── UserApproachesRepository.java ← Already provided
// ├── service/
// │   └── dsa/
// │       └── ApproachService.java      ← Already provided
// └── controller/
//     └── dsa/
//         └── ApproachController.java   ← Already provided