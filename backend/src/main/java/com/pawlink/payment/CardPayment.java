package com.pawlink.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Concrete Payment implementation simulating 16-digit card and CVV validation.
 */
public class CardPayment extends Payment {

    public CardPayment() {
        super();
    }

    public CardPayment(BigDecimal amount) {
        super(amount);
    }

    public CardPayment(Long id, Long bookingId, BigDecimal amount, String status, LocalDateTime createdAt) {
        super(id, bookingId, amount, status, createdAt);
    }

    @Override
    public boolean process() throws PaymentFailedException {
        if (getAmount() == null || getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            setStatus("FAILED");
            throw new PaymentFailedException("Card Payment rejected: Invalid transaction amount: " + getAmount());
        }

        // Simulate 16-digit card, expiry, and CVV validation handshake
        setStatus("HELD");
        return true;
    }

    @Override
    public String getMethod() {
        return "CARD";
    }
}

