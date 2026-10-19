package com.pawlink.common.exception;

/**
 * Thrown when credentials fail verification during login.
 * Maps to HTTP 401 Unauthorized.
 */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
