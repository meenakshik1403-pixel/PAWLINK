package com.pawlink.sitter;

/**
 * Thrown when an unverified sitter is attempted to be booked.
 * Strict Domain Invariant: Only sitters with verified = TRUE can be booked.
 */
public class UnverifiedSitterException extends com.pawlink.common.exception.UnverifiedSitterException {

    public UnverifiedSitterException(String message) {
        super(message);
    }
}

