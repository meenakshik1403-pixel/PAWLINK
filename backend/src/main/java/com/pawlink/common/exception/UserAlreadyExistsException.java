package com.pawlink.common.exception;

/**
 * Thrown when attempting to register an email that is already registered.
 * Maps to HTTP 409 Conflict.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
