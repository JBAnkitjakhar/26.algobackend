// src/main/java/com/algoarena/dto/dsa/ApproachDetailDTO.java
package com.algoarena.dto.dsa;

import com.algoarena.model.UserApproaches.ApproachData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class ApproachDetailDTO {

    private String id;
    private String questionId;
    private String userId;
    private String userName;

    @NotBlank(message = "Text content is required")
    @Size(min = 10, max = 15000, message = "Text content must be between 10 and 15000 characters")
    private String textContent;

    @Size(max = 5000, message = "Code content must not exceed 5000 characters")
    private String codeContent;

    @NotNull(message = "Code language is required")
    @Pattern(regexp = "^(java|python|javascript|cpp|c|csharp|go|rust|kotlin|swift|ruby|php|typescript)$", message = "Invalid programming language. Allowed: java, python, javascript, cpp, c, csharp, go, rust, kotlin, swift, ruby, php, typescript")
    private String codeLanguage;

    private int contentSize;
    private double contentSizeKB;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApproachDetailDTO() {
    }

    public ApproachDetailDTO(ApproachData data, String userId, String userName) {
        this.id = data.getId();
        this.questionId = data.getQuestionId();
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