package com.pawlink.common.exception;

/**
 * Thrown when a request lacks authentication or the X-User-Id header.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
