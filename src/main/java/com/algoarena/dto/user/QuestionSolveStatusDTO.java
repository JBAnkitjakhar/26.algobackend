// src/main/java/com/algoarena/dto/user/QuestionSolveStatusDTO.java
package com.algoarena.dto.user;

import java.time.LocalDateTime;

public class QuestionSolveStatusDTO {
    private boolean solved;
    private LocalDateTime solvedAt;  // null if not solved

    public QuestionSolveStatusDTO() {}

    public QuestionSolveStatusDTO(boolean solved, LocalDateTime solvedAt) {
        this.solved = solved;
        this.solvedAt = solvedAt;
    }

    // Static factory methods for clarity
    public static QuestionSolveStatusDTO notSolved() {
        return new QuestionSolveStatusDTO(false, null);
    }

    public static QuestionSolveStatusDTO solved(LocalDateTime solvedAt) {
        return new QuestionSolveStatusDTO(true, solvedAt);
    }

    // Getters and Setters
    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public LocalDateTime getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
    }

    @Override
    public String toString() {
        return "QuestionSolveStatusDTO{" +
                "solved=" + solved +
                ", solvedAt=" + solvedAt +
                '}';
    }
}