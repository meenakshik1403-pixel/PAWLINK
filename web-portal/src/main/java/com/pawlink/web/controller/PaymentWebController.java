package com.pawlink.web.controller;

import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AuthUserDTO;
import com.pawlink.web.dto.PaymentDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Web Portal Controller for Pet Owner Payments & Escrow Transaction History.
 */
@Controller
@RequestMapping("/payments")
public class PaymentWebController {

    private final ApiGateway apiGateway;

    public PaymentWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    /**
     * GET /payments: Displays payment transaction history and escrow statuses for the logged-in owner.
     */
    @GetMapping
    public String getPaymentHistory(HttpSession session, Model model) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            PaymentDTO[] raw = apiGateway.get("/api/payments?ownerId=" + user.getId(), PaymentDTO[].class);
            List<PaymentDTO> payments = raw != null ? Arrays.asList(raw) : Collections.emptyList();

            BigDecimal totalSpent = BigDecimal.ZERO;
            BigDecimal totalHeld = BigDecimal.ZERO;

            for (PaymentDTO p : payments) {
                if (p.getAmount() != null) {
                    totalSpent = totalSpent.add(p.getAmount());
                    if ("HELD".equalsIgnoreCase(p.getStatus())) {
                        totalHeld = totalHeld.add(p.getAmount());
                    }
                }
            }

            model.addAttribute("payments", payments);
            model.addAttribute("totalSpent", totalSpent);
            model.addAttribute("totalHeld", totalHeld);
            model.addAttribute("totalCount", payments.size());
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to fetch payment history: " + ex.getMessage());
            model.addAttribute("payments", Collections.emptyList());
            model.addAttribute("totalSpent", BigDecimal.ZERO);
            model.addAttribute("totalHeld", BigDecimal.ZERO);
            model.addAttribute("totalCount", 0);
        }

        return "payments/history";
    }
}

