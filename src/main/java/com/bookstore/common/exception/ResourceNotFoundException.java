package com.bookstore.common.exception;

/**
 * Thrown when a requested resource does not exist.
 * Maps to HTTP 404 Not Found in {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
