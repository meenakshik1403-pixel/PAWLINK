package com.pawlink.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFactoryTest {

    @Test
    @DisplayName("Factory creates UpiPayment and polymorphic process() transitions status to HELD")
    void testUpiPaymentProcessing() throws Exception {
        Payment upi = PaymentFactory.createPayment("UPI", BigDecimal.valueOf(75.00));
        assertTrue(upi instanceof UpiPayment);
        assertEquals("UPI", upi.getMethod());

        boolean result = upi.process();
        assertTrue(result);
        assertEquals("HELD", upi.getStatus());
    }

    @Test
    @DisplayName("Factory creates CardPayment and polymorphic process() validates card simulation")
    void testCardPaymentProcessing() throws Exception {
        Payment card = PaymentFactory.createPayment("CARD", BigDecimal.valueOf(150.00));
        assertTrue(card instanceof CardPayment);
        assertEquals("CARD", card.getMethod());

        boolean result = card.process();
        assertTrue(result);
        assertEquals("HELD", card.getStatus());
    }

    @Test
    @DisplayName("Factory creates WalletPayment and process() verifies wallet balance")
    void testWalletPaymentProcessing() throws Exception {
        Payment wallet = PaymentFactory.createPayment("WALLET", BigDecimal.valueOf(50.00));
        assertTrue(wallet instanceof WalletPayment);
        assertEquals("WALLET", wallet.getMethod());

        boolean result = wallet.process();
        assertTrue(result);
        assertEquals("HELD", wallet.getStatus());
    }

    @Test
    @DisplayName("Wallet payment exceeding balance limit throws PaymentFailedException")
    void testWalletPaymentInsufficientBalance() {
        Payment wallet = PaymentFactory.createPayment("WALLET", BigDecimal.valueOf(25000.00));
        PaymentFailedException ex = assertThrows(PaymentFailedException.class, wallet::process);
        assertTrue(ex.getMessage().contains("Insufficient wallet balance"));
        assertEquals("FAILED", wallet.getStatus());
    }

    @Test
    @DisplayName("Zero or negative payment amount throws PaymentFailedException")
    void testInvalidAmountThrows() {
        Payment card = PaymentFactory.createPayment("CARD", BigDecimal.ZERO);
        assertThrows(PaymentFailedException.class, card::process);

        Payment upi = PaymentFactory.createPayment("UPI", BigDecimal.valueOf(-10.00));
        assertThrows(PaymentFailedException.class, upi::process);
    }

    @Test
    @DisplayName("Unsupported payment method throws IllegalArgumentException")
    void testUnsupportedMethodThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PaymentFactory.createPayment("BITCOIN", BigDecimal.valueOf(100.00)));
    }

    @Test
    @DisplayName("Payment refund() transitions status to REFUNDED")
    void testRefund() throws Exception {
        Payment card = PaymentFactory.createPayment("CARD", BigDecimal.valueOf(120.00));
        card.process();
        assertEquals("HELD", card.getStatus());

        card.refund();
        assertEquals("REFUNDED", card.getStatus());
    }

    @Test
    @DisplayName("Payment release() transitions status to RELEASED")
    void testRelease() throws Exception {
        Payment upi = PaymentFactory.createPayment("UPI", BigDecimal.valueOf(90.00));
        upi.process();
        assertEquals("HELD", upi.getStatus());

        upi.release();
        assertEquals("RELEASED", upi.getStatus());
    }
}

