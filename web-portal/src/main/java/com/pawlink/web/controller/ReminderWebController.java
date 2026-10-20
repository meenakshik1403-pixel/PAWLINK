package com.pawlink.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AuthUserDTO;
import com.pawlink.web.dto.PetDTO;
import com.pawlink.web.dto.ReminderDTO;
import com.pawlink.web.dto.ReminderRunResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Web Portal Controller for the Reminders and Notifications Vertical.
 * Communicates with backend REST API via ApiGateway, passing user session and X-User-Id header.
 */
@Controller
@RequestMapping("/reminders")
public class ReminderWebController {

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReminderWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    /**
     * GET /reminders: Displays active and upcoming reminders with urgency badges.
     */
    @GetMapping
    public String listReminders(HttpSession session, Model model) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            String uri;
            if ("OWNER".equalsIgnoreCase(user.getRole())) {
                uri = "/api/reminders?ownerId=" + user.getId();
            } else {
                uri = "/api/reminders";
            }

            ReminderDTO[] raw = apiGateway.get(uri, ReminderDTO[].class);
            List<ReminderDTO> list = raw != null ? Arrays.asList(raw) : Collections.emptyList();

            // Categorize by urgency
            List<ReminderDTO> overdue = list.stream().filter(r -> "OVERDUE".equals(r.getUrgency())).collect(Collectors.toList());
            List<ReminderDTO> dueToday = list.stream().filter(r -> "DUE_TODAY".equals(r.getUrgency())).collect(Collectors.toList());
            List<ReminderDTO> upcoming = list.stream().filter(r -> "UPCOMING".equals(r.getUrgency())).collect(Collectors.toList());
            List<ReminderDTO> sent = list.stream().filter(ReminderDTO::isSent).collect(Collectors.toList());

            model.addAttribute("reminders", list);
            model.addAttribute("overdueReminders", overdue);
            model.addAttribute("dueTodayReminders", dueToday);
            model.addAttribute("upcomingReminders", upcoming);
            model.addAttribute("sentReminders", sent);

            model.addAttribute("overdueCount", overdue.size());
            model.addAttribute("dueTodayCount", dueToday.size());
            model.addAttribute("upcomingCount", upcoming.size());
            model.addAttribute("sentCount", sent.size());

            // Fetch pets for quick-create form if owner
            if ("OWNER".equalsIgnoreCase(user.getRole())) {
                try {
                    PetDTO[] pets = apiGateway.get("/api/pets?ownerId=" + user.getId(), PetDTO[].class);
                    model.addAttribute("userPets", pets != null ? Arrays.asList(pets) : Collections.emptyList());
                } catch (Exception ignored) {
                    model.addAttribute("userPets", Collections.emptyList());
                }
            }
        } catch (Exception e) {
            model.addAttribute("reminders", Collections.emptyList());
            model.addAttribute("errorMessage", "Failed to retrieve reminders: " + e.getMessage());
        }

        return "reminders/list";
    }

    /**
     * POST /reminders/run: Manually triggers the automated reminder observer engine.
     */
    @PostMapping("/run")
    public String runObserverEngine(HttpSession session, RedirectAttributes redirectAttributes) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            ReminderRunResponseDTO resp = apiGateway.post("/api/reminders/run", null, ReminderRunResponseDTO.class);
            int count = resp != null ? resp.getProcessedCount() : 0;
            if (count > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Observer engine executed! Dispatched " + count + " reminder(s) to recipients via Email and SMS adapters.");
            } else {
                redirectAttributes.addFlashAttribute("infoMessage",
                        "Observer scan finished: All reminders are up to date (0 pending dispatches).");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to run reminder engine: " + ex.getMessage());
        }

        return "redirect:/reminders";
    }

    /**
     * POST /reminders/new: Creates a new reminder for an owner's pet.
     */
    @PostMapping("/new")
    public String createReminder(
            @RequestParam("petId") Long petId,
            @RequestParam("type") String type,
            @RequestParam("dueDate") String dueDateStr,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr.trim());
            Map<String, Object> payload = new HashMap<>();
            payload.put("petId", petId);
            payload.put("type", type.trim().toUpperCase());
            payload.put("dueDate", dueDate.toString());

            apiGateway.post("/api/reminders", payload, ReminderDTO.class);
            redirectAttributes.addFlashAttribute("successMessage", "New " + type + " reminder scheduled successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create reminder: " + ex.getMessage());
        }

        return "redirect:/reminders";
    }
}

