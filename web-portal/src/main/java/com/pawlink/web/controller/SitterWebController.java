package com.pawlink.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Web Controller for Sitter Bookings and Check-ins vertical.
 * Communicates with backend REST API via ApiGateway.
 */
@Controller
public class SitterWebController {

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SitterWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    /**
     * GET /sitters: Displays directory of pet sitters.
     */
    @GetMapping("/sitters")
    public String listSitters(Model model, HttpSession session) {
        try {
            SitterDTO[] raw = apiGateway.get("/api/sitters", SitterDTO[].class);
            List<SitterDTO> sitters = raw != null ? Arrays.asList(raw) : Collections.emptyList();
            model.addAttribute("sitters", sitters);
        } catch (Exception ex) {
            model.addAttribute("error", "Unable to load sitters: " + extractError(ex));
            model.addAttribute("sitters", Collections.emptyList());
        }
        return "sitters/list";
    }

    /**
     * GET /sitters/{id}/book: Opens booking confirmation form for a specific sitter.
     */
    @GetMapping("/sitters/{id}/book")
    public String showBookForm(@PathVariable("id") Long id, Model model, HttpSession session, RedirectAttributes redirectAttrs) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            SitterDTO sitter = apiGateway.get("/api/sitters/" + id, SitterDTO.class);
            if (sitter == null) {
                redirectAttrs.addFlashAttribute("error", "Sitter not found.");
                return "redirect:/sitters";
            }

            if (!sitter.isVerified()) {
                redirectAttrs.addFlashAttribute("error", "Sitter '" + sitter.getName() + "' is not verified yet. Bookings are only permitted for verified sitters.");
                return "redirect:/sitters";
            }

            PetDTO[] rawPets = apiGateway.get("/api/pets?ownerId=" + user.getId(), PetDTO[].class);
            List<PetDTO> pets = rawPets != null ? Arrays.asList(rawPets) : Collections.emptyList();

            if (pets.isEmpty()) {
                redirectAttrs.addFlashAttribute("warning", "Please add a pet in 'My Pets' before booking a sitter.");
                return "redirect:/pets/new";
            }

            BookingForm form = new BookingForm(id);
            // Default to tomorrow 9am - 1pm
            LocalDateTime tomorrow9am = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
            LocalDateTime tomorrow1pm = tomorrow9am.plusHours(4);
            form.setStartDateTime(tomorrow9am.toString().substring(0, 16));
            form.setEndDateTime(tomorrow1pm.toString().substring(0, 16));

            model.addAttribute("sitter", sitter);
            model.addAttribute("pets", pets);
            model.addAttribute("bookingForm", form);
            return "sitters/book";
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Could not load booking form: " + extractError(ex));
            return "redirect:/sitters";
        }
    }

    /**
     * POST /sitters/{id}/book: Submits booking and payment hold.
     */
    @PostMapping("/sitters/{id}/book")
    public String submitBooking(
            @PathVariable("id") Long id,
            @ModelAttribute("bookingForm") BookingForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttrs
    ) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> req = new HashMap<>();
            req.put("sitterId", id);
            req.put("petId", form.getPetId());
            req.put("ownerId", user.getId());

            String startStr = form.getStartDateTime().length() == 16 ? form.getStartDateTime() + ":00" : form.getStartDateTime();
            String endStr = form.getEndDateTime().length() == 16 ? form.getEndDateTime() + ":00" : form.getEndDateTime();

            req.put("startTime", startStr);
            req.put("endTime", endStr);
            req.put("paymentMethod", form.getPaymentMethod());

            SittingBookingDTO booking = apiGateway.post("/api/sitter-bookings", req, SittingBookingDTO.class);
            redirectAttrs.addFlashAttribute("success",
                    "🎉 Sitter booking confirmed! Booking #" + booking.getId() +
                    " - Total Fee: $" + booking.getTotalAmount() + " placed in Escrow (HELD).");
            return "redirect:/bookings";
        } catch (Exception ex) {
            String errorMsg = extractError(ex);
            redirectAttrs.addFlashAttribute("error", "Booking failed: " + errorMsg);
            return "redirect:/sitters/" + id + "/book";
        }
    }

    /**
     * GET /bookings: Displays user's bookings (Owner bookings or Sitter jobs) and check-in timeline.
     */
    @GetMapping("/bookings")
    public String viewBookings(HttpSession session, Model model) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            String uri;
            if ("SITTER".equalsIgnoreCase(user.getRole())) {
                uri = "/api/sitter-bookings?sitterId=" + user.getId();
            } else if ("OWNER".equalsIgnoreCase(user.getRole())) {
                uri = "/api/sitter-bookings?ownerId=" + user.getId();
            } else {
                uri = "/api/sitter-bookings";
            }

            SittingBookingDTO[] raw = apiGateway.get(uri, SittingBookingDTO[].class);
            List<SittingBookingDTO> bookings = raw != null ? new ArrayList<>(Arrays.asList(raw)) : new ArrayList<>();

            // Enrich bookings with their check-in logs
            for (SittingBookingDTO b : bookings) {
                try {
                    CheckinDTO[] checkins = apiGateway.get("/api/sitter-bookings/" + b.getId() + "/checkins", CheckinDTO[].class);
                    if (checkins != null) {
                        b.setCheckins(Arrays.asList(checkins));
                    }
                } catch (Exception ignored) {
                }
            }

            model.addAttribute("bookings", bookings);
            model.addAttribute("userRole", user.getRole());
        } catch (Exception ex) {
            model.addAttribute("error", "Unable to load bookings: " + extractError(ex));
            model.addAttribute("bookings", Collections.emptyList());
        }

        return "sitters/bookings";
    }

    /**
     * POST /bookings/{id}/checkins: Adds a check-in note to a booking.
     */
    @PostMapping("/bookings/{id}/checkins")
    public String addCheckin(
            @PathVariable("id") Long id,
            @RequestParam("note") String note,
            HttpSession session,
            RedirectAttributes redirectAttrs
    ) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (note == null || note.trim().isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Check-in note cannot be blank.");
            return "redirect:/bookings";
        }

        try {
            Map<String, String> body = Collections.singletonMap("note", note.trim());
            apiGateway.post("/api/sitter-bookings/" + id + "/checkins", body, CheckinDTO.class);
            redirectAttrs.addFlashAttribute("success", "✓ Care check-in note posted successfully!");
        } catch (Exception ex) {
            redirectAttrs.addFlashAttribute("error", "Failed to post check-in: " + extractError(ex));
        }

        return "redirect:/bookings";
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

