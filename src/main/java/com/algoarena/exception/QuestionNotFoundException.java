// File: src/main/java/com/algoarena/exception/QuestionNotFoundException.java
package com.algoarena.exception;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String questionId) {
        super("Question not found: " + questionId);
    }
}