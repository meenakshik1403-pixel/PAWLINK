package com.pawlink.payment;

import com.pawlink.payment.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Payment transactions and Escrow management:
 * - GET   /api/payments?ownerId={id}
 * - GET   /api/payments/{id}
 * - PATCH /api/payments/{id}/release
 * - PATCH /api/payments/{id}/refund
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * GET /api/payments: Retrieves payment transactions for owner or platform.
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(name = "ownerId", required = false) Long ownerId,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        if (ownerId != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByOwner(ownerId));
        } else if (xUserId != null) {
            return ResponseEntity.ok(paymentService.getPaymentsByOwner(xUserId));
        }
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * GET /api/payments/{id}: Retrieves single payment details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    /**
     * PATCH /api/payments/{id}/release: Releases funds from escrow to sitter upon job completion.
     */
    @PatchMapping("/{id}/release")
    public ResponseEntity<Void> releaseEscrow(@PathVariable("id") Long id) {
        paymentService.releaseEscrow(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/payments/{id}/refund: Refunds escrow payment back to pet parent.
     */
    @PatchMapping("/{id}/refund")
    public ResponseEntity<Void> refundPayment(@PathVariable("id") Long id) {
        paymentService.refundPayment(id);
        return ResponseEntity.noContent().build();
    }
}

