// File: src/main/java/com/algoarena/exception/QuestionNotSolvedException.java
package com.algoarena.exception;

public class QuestionNotSolvedException extends RuntimeException {
    public QuestionNotSolvedException(String questionId) {
        super("Question not solved by user: " + questionId);
    }
}