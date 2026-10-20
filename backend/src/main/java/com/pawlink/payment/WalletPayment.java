package com.pawlink.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Concrete Payment implementation simulating Pet Wallet balance verification.
 */
public class WalletPayment extends Payment {

    // Simulated maximum wallet balance
    private static final BigDecimal SIMULATED_WALLET_BALANCE = new BigDecimal("10000.00");

    public WalletPayment() {
        super();
    }

    public WalletPayment(BigDecimal amount) {
        super(amount);
    }

    public WalletPayment(Long id, Long bookingId, BigDecimal amount, String status, LocalDateTime createdAt) {
        super(id, bookingId, amount, status, createdAt);
    }

    @Override
    public boolean process() throws PaymentFailedException {
        if (getAmount() == null || getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            setStatus("FAILED");
            throw new PaymentFailedException("Wallet Payment rejected: Amount must be positive.");
        }

        // Simulate balance check against wallet limit
        if (getAmount().compareTo(SIMULATED_WALLET_BALANCE) > 0) {
            setStatus("FAILED");
            throw new PaymentFailedException("Wallet Payment failed: Insufficient wallet balance. Balance limit is $" + SIMULATED_WALLET_BALANCE);
        }

        setStatus("HELD");
        return true;
    }

    @Override
    public String getMethod() {
        return "WALLET";
    }
}

