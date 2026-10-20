package com.pawlink.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Concrete Payment implementation simulating UPI VPA verification.
 */
public class UpiPayment extends Payment {

    public UpiPayment() {
        super();
    }

    public UpiPayment(BigDecimal amount) {
        super(amount);
    }

    public UpiPayment(Long id, Long bookingId, BigDecimal amount, String status, LocalDateTime createdAt) {
        super(id, bookingId, amount, status, createdAt);
    }

    @Override
    public boolean process() throws PaymentFailedException {
        if (getAmount() == null || getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            setStatus("FAILED");
            throw new PaymentFailedException("UPI Payment rejected: Amount must be greater than zero.");
        }

        // Simulate UPI VPA verification and instant authorization
        setStatus("HELD");
        return true;
    }

    @Override
    public String getMethod() {
        return "UPI";
    }
}

