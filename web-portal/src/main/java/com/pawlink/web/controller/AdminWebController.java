package com.pawlink.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AuthUserDTO;
import com.pawlink.web.dto.SitterDTO;
import com.pawlink.web.dto.SystemStatsDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Web Controller for Admin vertical (P8):
 * - Sitter verification and approval workflows.
 * - System analytics & aggregation metrics dashboard.
 */
@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    /**
     * GET /admin/sitters: Lists all registered sitters with verification actions.
     */
    @GetMapping("/sitters")
    public String listSitters(Model model, HttpSession session) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/?error=Admin+access+required";
        }

        try {
            SitterDTO[] raw = apiGateway.get("/api/admin/sitters", SitterDTO[].class);
            List<SitterDTO> sitters = raw != null ? Arrays.asList(raw) : Collections.emptyList();
            model.addAttribute("sitters", sitters);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to retrieve sitter queue: " + extractError(ex));
            model.addAttribute("sitters", Collections.emptyList());
        }

        return "admin/sitters";
    }

    /**
     * POST /admin/sitters/{id}/approve: Approves and marks a sitter as verified.
     */
    @PostMapping("/sitters/{id}/approve")
    public String approveSitter(@PathVariable("id") Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttrs.addFlashAttribute("error", "Access denied: Admin role required.");
            return "redirect:/";
        }

        try {
            apiGateway.patch("/api/admin/sitters/" + id + "/approve", null, Void.class);
            redirectAttrs.addFlashAttribute("success", "✓ Sitter #" + id + " has been verified and approved successfully!");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Unable to approve sitter #" + id + ": " + extractError(ex));
        }

        return "redirect:/admin/sitters";
    }

    /**
     * GET /admin/stats: Displays system performance and aggregation analytics.
     */
    @GetMapping("/stats")
    public String viewStats(Model model, HttpSession session) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole()) && !"VET".equalsIgnoreCase(user.getRole())) {
            return "redirect:/?error=Admin+or+Vet+access+required";
        }

        try {
            SystemStatsDTO stats = apiGateway.get("/api/admin/stats", SystemStatsDTO.class);
            model.addAttribute("stats", stats != null ? stats : new SystemStatsDTO());
        } catch (Exception ex) {
            model.addAttribute("error", "Unable to load system statistics: " + extractError(ex));
            model.addAttribute("stats", new SystemStatsDTO());
        }

        return "admin/stats";
    }

    private String extractError(Exception ex) {
        if (ex instanceof HttpClientErrorException hce) {
            try {
                JsonNode root = objectMapper.readTree(hce.getResponseBodyAsString());
                if (root.has("error")) {
                    return root.get("error").asText();
                }
            } catch (Exception ignored) {
            }
            return hce.getStatusText();
        } else if (ex instanceof HttpServerErrorException hse) {
            try {
                JsonNode root = objectMapper.readTree(hse.getResponseBodyAsString());
                if (root.has("error")) {
                    return root.get("error").asText();
                }
            } catch (Exception ignored) {
            }
            return hse.getStatusText();
        }
        return ex.getMessage() != null ? ex.getMessage() : "Unexpected communication failure.";
    }
}

