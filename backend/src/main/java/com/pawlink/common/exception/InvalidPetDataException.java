package com.pawlink.common.exception;

/**
 * Thrown when pet registration data is invalid (unknown species, blank fields).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidPetDataException extends RuntimeException {
    public InvalidPetDataException(String message) {
        super(message);
    }
}
