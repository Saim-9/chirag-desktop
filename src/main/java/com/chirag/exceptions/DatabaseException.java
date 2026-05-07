package com.chirag.exceptions;

/**
 * Custom domain exception for wrapping database-related errors.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
