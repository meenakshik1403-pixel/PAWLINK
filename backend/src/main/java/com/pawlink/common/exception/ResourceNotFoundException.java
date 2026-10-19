package com.pawlink.common.exception;

/**
 * Thrown when a requested entity cannot be found.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
