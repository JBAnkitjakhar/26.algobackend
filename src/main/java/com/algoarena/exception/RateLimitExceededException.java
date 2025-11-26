// File: src/main/java/com/algoarena/exception/RateLimitExceededException.java
package com.algoarena.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("Too many requests. Please try again in a minute.");
    }
}