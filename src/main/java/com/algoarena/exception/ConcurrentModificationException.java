// File: src/main/java/com/algoarena/exception/ConcurrentModificationException.java
package com.algoarena.exception;

public class ConcurrentModificationException extends RuntimeException {
    public ConcurrentModificationException() {
        super("Unable to complete operation due to concurrent modifications. Please try again.");
    }
}