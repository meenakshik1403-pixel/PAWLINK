package com.pawlink.common.exception;

/**
 * Thrown when an unverified sitter is attempted to be booked.
 * Maps to HTTP 400 Bad Request.
 */
public class UnverifiedSitterException extends Exception {
    public UnverifiedSitterException(String message) {
        super(message);
    }
}
