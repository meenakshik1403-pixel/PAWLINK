package com.pawlink.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Abstract base class for polymorphic payment processing in PawLink.
 * Concrete subclasses override process() with payment-method specific validation.
 */
public abstract class Payment {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String status; // 'HELD', 'RELEASED', 'REFUNDED', 'FAILED'
    private LocalDateTime createdAt;

    public Payment() {
        this.status = "HELD";
        this.createdAt = LocalDateTime.now();
    }

    public Payment(BigDecimal amount) {
        this();
        this.amount = amount;
    }

    public Payment(Long id, Long bookingId, BigDecimal amount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status != null ? status : "HELD";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * Polymorphic method implemented by each payment channel.
     * Validates credentials/balance and transitions status to 'HELD'.
     *
     * @return true if processing succeeded
     * @throws PaymentFailedException if validation or funds check fails
     */
    public abstract boolean process() throws PaymentFailedException;

    /**
     * Returns the payment method code ('UPI', 'CARD', 'WALLET').
     */
    public abstract String getMethod();

    /**
     * Transitions payment status to REFUNDED.
     */
    public void refund() {
        this.status = "REFUNDED";
    }

    /**
     * Transitions payment status to RELEASED (escrow payout to sitter).
     */
    public void release() {
        this.status = "RELEASED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

