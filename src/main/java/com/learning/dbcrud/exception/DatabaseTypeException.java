package com.learning.dbcrud.exception;

/**
 * Exception thrown when there is an issue with database type operations
 */
public class DatabaseTypeException extends RuntimeException {

    public DatabaseTypeException(String message) {
        super(message);
    }

    public DatabaseTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseTypeException(Throwable cause) {
        super(cause);
    }
}