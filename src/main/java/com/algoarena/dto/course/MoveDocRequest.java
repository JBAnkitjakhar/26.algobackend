// src/main/java/com/algoarena/dto/course/MoveDocRequest.java
package com.algoarena.dto.course;

import jakarta.validation.constraints.NotBlank;

public class MoveDocRequest {

    @NotBlank(message = "New topic ID is required")
    private String newTopicId;

    public MoveDocRequest() {}

    public MoveDocRequest(String newTopicId) {
        this.newTopicId = newTopicId;
    }

    public String getNewTopicId() {
        return newTopicId;
    }

    public void setNewTopicId(String newTopicId) {
        this.newTopicId = newTopicId;
    }
}