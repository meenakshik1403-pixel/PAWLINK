package com.pawlink.web.controller;

import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AuthUserDTO;
import com.pawlink.web.dto.PetDTO;
import com.pawlink.web.dto.PetMedicalTimelineDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Web Portal Controller for Pet Medical & Vaccination History.
 * Communicates with backend REST API via ApiGateway.
 */
@Controller
@RequestMapping("/pets/{id}/history")
public class RecordWebController {

    private final ApiGateway apiGateway;

    public RecordWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    /**
     * GET /pets/{id}/history: Fetches combined medical history and vaccinations via ApiGateway.
     */
    @GetMapping
    public String getPetMedicalHistory(@PathVariable("id") Long petId, HttpSession session, Model model) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // 1. Fetch Pet Details
            PetDTO pet = apiGateway.get("/api/pets/" + petId, PetDTO.class);
            model.addAttribute("pet", pet);

            // 2. Fetch Combined History Timeline
            PetMedicalTimelineDTO[] recordsArray = apiGateway.get("/api/pets/" + petId + "/records", PetMedicalTimelineDTO[].class);
            List<PetMedicalTimelineDTO> timeline = recordsArray != null ? Arrays.asList(recordsArray) : Collections.emptyList();
            model.addAttribute("timeline", timeline);

            // Filter lists for specialized UI cards
            List<PetMedicalTimelineDTO> vaccinations = new ArrayList<>();
            List<PetMedicalTimelineDTO> medicalVisits = new ArrayList<>();
            for (PetMedicalTimelineDTO item : timeline) {
                if ("VACCINATION".equalsIgnoreCase(item.getType())) {
                    vaccinations.add(item);
                } else {
                    medicalVisits.add(item);
                }
            }
            model.addAttribute("vaccinations", vaccinations);
            model.addAttribute("medicalVisits", medicalVisits);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Could not retrieve medical records: " + e.getMessage());
            model.addAttribute("timeline", Collections.emptyList());
            model.addAttribute("vaccinations", Collections.emptyList());
            model.addAttribute("medicalVisits", Collections.emptyList());
        }

        return "medical/history";
    }
}
