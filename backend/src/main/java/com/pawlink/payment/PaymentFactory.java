package com.pawlink.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Factory pattern implementation for instantiating polymorphic Payment subclasses.
 */
public class PaymentFactory {

    private PaymentFactory() {
    }

    /**
     * Factory method creating appropriate Payment subclass based on method string.
     *
     * @param method "UPI", "CARD", or "WALLET"
     * @param amount monetary value to process
     * @return concrete Payment instance
     * @throws IllegalArgumentException if method is unknown or unsupported
     */
    public static Payment createPayment(String method, BigDecimal amount) {
        if (method == null || method.trim().isEmpty()) {
            method = "CARD";
        }

        String normalized = method.trim().toUpperCase();
        return switch (normalized) {
            case "UPI" -> new UpiPayment(amount);
            case "CARD" -> new CardPayment(amount);
            case "WALLET" -> new WalletPayment(amount);
            default -> throw new IllegalArgumentException("Unsupported payment method: '" + method + "'. Supported methods are UPI, CARD, WALLET.");
        };
    }

    /**
     * Factory method for hydrating payments from database records.
     */
    public static Payment createPayment(Long id, Long bookingId, BigDecimal amount, String method,
                                        String status, LocalDateTime createdAt) {
        if (method == null || method.trim().isEmpty()) {
            method = "CARD";
        }

        String normalized = method.trim().toUpperCase();
        return switch (normalized) {
            case "UPI" -> new UpiPayment(id, bookingId, amount, status, createdAt);
            case "CARD" -> new CardPayment(id, bookingId, amount, status, createdAt);
            case "WALLET" -> new WalletPayment(id, bookingId, amount, status, createdAt);
            default -> throw new IllegalArgumentException("Unsupported payment method: " + method);
        };
    }
}

