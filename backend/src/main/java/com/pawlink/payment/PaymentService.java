package com.pawlink.payment;

import com.pawlink.common.exception.ResourceNotFoundException;
import com.pawlink.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing payments, escrow releases, and refunds.
 */
@Service
public class PaymentService {

    private final PaymentDAO paymentDAO;

    public PaymentService(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }

    public List<PaymentResponse> getPaymentsByOwner(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Valid Owner ID is required.");
        }
        return paymentDAO.findByOwnerId(ownerId);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentDAO.findAllWithDetails();
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + id));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public void releaseEscrow(Long paymentId) {
        Payment payment = paymentDAO.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + paymentId));
        payment.release();
        paymentDAO.updateStatus(paymentId, "RELEASED");
    }

    @Transactional
    public void refundPayment(Long paymentId) {
        Payment payment = paymentDAO.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + paymentId));
        payment.refund();
        paymentDAO.updateStatus(paymentId, "REFUNDED");
    }
}

