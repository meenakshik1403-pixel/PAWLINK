package com.pawlink.common.exception;

/**
 * Thrown when payment processing fails during booking transactions.
 * Maps to HTTP 400 Bad Request.
 */
public class PaymentFailedException extends Exception {
    public PaymentFailedException(String message) {
        super(message);
    }
}
