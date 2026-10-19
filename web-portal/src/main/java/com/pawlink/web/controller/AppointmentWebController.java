package com.pawlink.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AppointmentDTO;
import com.pawlink.web.dto.AuthUserDTO;
import com.pawlink.web.dto.PetDTO;
import com.pawlink.web.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Web Portal Controller for Vet Appointments & Scheduling Vertical.
 * Integrates with backend REST API via ApiGateway, passing user session and X-User-Id header.
 */
@Controller
@RequestMapping("/appointments")
public class AppointmentWebController {

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppointmentWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    /**
     * GET /appointments: Shows list of user's appointments in HTML table.
     */
    @GetMapping
    public String listAppointments(HttpSession session, Model model) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            String uri;
            if ("VET".equalsIgnoreCase(user.getRole())) {
                uri = "/api/appointments?vetId=" + user.getId();
            } else {
                uri = "/api/appointments?ownerId=" + user.getId();
            }

            AppointmentDTO[] appointments = apiGateway.get(uri, AppointmentDTO[].class);
            model.addAttribute("appointments", appointments != null ? Arrays.asList(appointments) : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("appointments", Collections.emptyList());
            model.addAttribute("errorMessage", "Failed to retrieve appointments: " + e.getMessage());
        }

        return "appointments/list";
    }

    /**
     * GET /appointments/book: Shows booking form with Vet dropdown, Pet dropdown, Date and Time inputs.
     */
    @GetMapping("/book")
    public String showBookForm(
            @RequestParam(name = "petId", required = false) Long preselectedPetId,
            HttpSession session,
            Model model
    ) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        prepareBookingFormData(user.getId(), model);
        if (preselectedPetId != null) {
            model.addAttribute("selectedPetId", preselectedPetId);
        }
        return "appointments/book";
    }

    /**
     * POST /appointments/book: Books appointment via POST /api/appointments.
     * If 409 Conflict (SlotUnavailableException), flash error message:
     * "Selected time slot is already booked. Please choose another time."
     */
    @PostMapping("/book")
    public String bookAppointment(
            @RequestParam("vetId") Long vetId,
            @RequestParam("petId") Long petId,
            @RequestParam("date") String dateStr,
            @RequestParam("time") String timeStr,
            @RequestParam(name = "duration", defaultValue = "30") int durationMins,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        LocalDate date;
        LocalTime startTime;
        try {
            date = LocalDate.parse(dateStr.trim());
            startTime = LocalTime.parse(timeStr.trim());
        } catch (Exception e) {
            prepareBookingFormData(user.getId(), model);
            model.addAttribute("errorMessage", "Invalid date or time format. Please select valid values.");
            model.addAttribute("selectedVetId", vetId);
            model.addAttribute("selectedPetId", petId);
            model.addAttribute("date", dateStr);
            model.addAttribute("time", timeStr);
            return "appointments/book";
        }

        LocalTime endTime = startTime.plusMinutes(durationMins);
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> payload = new HashMap<>();
        payload.put("vetId", vetId);
        payload.put("petId", petId);
        payload.put("ownerId", user.getId());
        payload.put("startTime", startDateTime.format(dtf));
        payload.put("endTime", endDateTime.format(dtf));

        try {
            apiGateway.post("/api/appointments", payload, AppointmentDTO.class);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment successfully booked!");
            return "redirect:/appointments";
        } catch (RestClientResponseException ex) {
            prepareBookingFormData(user.getId(), model);
            model.addAttribute("selectedVetId", vetId);
            model.addAttribute("selectedPetId", petId);
            model.addAttribute("date", dateStr);
            model.addAttribute("time", timeStr);

            if (ex.getStatusCode().value() == 409) {
                model.addAttribute("errorMessage", "Selected time slot is already booked. Please choose another time.");
            } else {
                model.addAttribute("errorMessage", extractErrorMessage(ex.getResponseBodyAsString(), ex.getStatusCode().value()));
            }
            return "appointments/book";
        } catch (Exception ex) {
            prepareBookingFormData(user.getId(), model);
            model.addAttribute("errorMessage", "Unexpected error: " + ex.getMessage());
            return "appointments/book";
        }
    }

    /**
     * POST /appointments/{id}/cancel: Cancels an appointment.
     */
    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
            @PathVariable("id") Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Map<String, String> body = Map.of("status", "CANCELLED");
            apiGateway.patch("/api/appointments/" + id + "/status", body, AppointmentDTO.class);
            redirectAttributes.addFlashAttribute("successMessage", "Appointment #" + id + " has been cancelled.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel appointment: " + ex.getMessage());
        }

        return "redirect:/appointments";
    }

    private void prepareBookingFormData(Long ownerId, Model model) {
        try {
            UserDTO[] vets = apiGateway.get("/api/vets", UserDTO[].class);
            model.addAttribute("vets", vets != null ? Arrays.asList(vets) : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("vets", Collections.emptyList());
        }

        try {
            PetDTO[] pets = apiGateway.get("/api/pets?ownerId=" + ownerId, PetDTO[].class);
            model.addAttribute("pets", pets != null ? Arrays.asList(pets) : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("pets", Collections.emptyList());
        }

        model.addAttribute("minDate", LocalDate.now().toString());
        model.addAttribute("defaultDate", LocalDate.now().plusDays(1).toString());
    }

    private String extractErrorMessage(String body, int statusCode) {
        if (body != null && !body.trim().isEmpty()) {
            try {
                JsonNode root = objectMapper.readTree(body);
                if (root.has("error")) {
                    return root.get("error").asText();
                }
            } catch (Exception ignored) {}
        }
        return "Booking failed (HTTP " + statusCode + ")";
    }
}
