package com.pawlink.payment;

/**
 * Checked exception thrown when payment processing or escrow authorization fails.
 * Inherits from com.pawlink.common.exception.PaymentFailedException for unified ControllerAdvice mapping.
 */
public class PaymentFailedException extends com.pawlink.common.exception.PaymentFailedException {

    public PaymentFailedException(String message) {
        super(message);
    }
}

