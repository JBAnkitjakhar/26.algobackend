// File: src/main/java/com/algoarena/exception/QuestionAlreadySolvedException.java
package com.algoarena.exception;

public class QuestionAlreadySolvedException extends RuntimeException {
    public QuestionAlreadySolvedException(String questionId) {
        super("Question already marked as solved: " + questionId);
    }
}