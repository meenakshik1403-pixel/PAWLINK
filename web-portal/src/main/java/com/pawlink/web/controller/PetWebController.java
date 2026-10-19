package com.pawlink.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AuthUserDTO;
import com.pawlink.web.dto.PetDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.*;

/**
 * Web Portal Controller for Pet Management.
 * Communicates with backend REST API via ApiGateway, passing user session and X-User-Id header.
 */
@Controller
@RequestMapping("/pets")
public class PetWebController {

    private final ApiGateway apiGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PetWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    /**
     * GET /pets: Displays list of pets owned by the logged-in user.
     */
    @GetMapping
    public String listPets(HttpSession session, Model model) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            PetDTO[] pets = apiGateway.get("/api/pets?ownerId=" + user.getId(), PetDTO[].class);
            model.addAttribute("pets", pets != null ? Arrays.asList(pets) : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("pets", Collections.emptyList());
            model.addAttribute("errorMessage", "Failed to retrieve pets: " + e.getMessage());
        }

        return "pets/list";
    }

    /**
     * GET /pets/new: Displays form to register a new pet.
     */
    @GetMapping("/new")
    public String newPetForm(HttpSession session, Model model) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("speciesList", List.of("Dog", "Cat", "Bird", "Reptile"));
        model.addAttribute("defaultDob", LocalDate.now().minusYears(1).toString());
        return "pets/new";
    }

    /**
     * POST /pets/new: Submits pet data.
     * Catches backend validation errors and displays error inline.
     */
    @PostMapping("/new")
    public String createPet(
            @RequestParam("name") String name,
            @RequestParam("species") String species,
            @RequestParam("dob") String dobStr,
            HttpSession session,
            Model model
    ) {
        AuthUserDTO user = (AuthUserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("speciesList", List.of("Dog", "Cat", "Bird", "Reptile"));
        model.addAttribute("name", name);
        model.addAttribute("selectedSpecies", species);
        model.addAttribute("dob", dobStr);

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Pet name cannot be blank.");
            return "pets/new";
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr.trim());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Invalid date format. Please use YYYY-MM-DD.");
            return "pets/new";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name.trim());
        payload.put("species", species);
        payload.put("dob", dob.toString());
        payload.put("ownerId", user.getId());

        try {
            apiGateway.post("/api/pets", payload, PetDTO.class);
            return "redirect:/pets";
        } catch (RestClientResponseException ex) {
            String errorMsg = extractErrorMessage(ex.getResponseBodyAsString(), ex.getStatusCode().value());
            model.addAttribute("errorMessage", errorMsg);
            return "pets/new";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Unexpected error: " + ex.getMessage());
            return "pets/new";
        }
    }

    private String extractErrorMessage(String body, int statusCode) {
        if (body != null && !body.trim().isEmpty()) {
            try {
                JsonNode root = objectMapper.readTree(body);
                if (root.has("error")) {
                    return root.get("error").asText();
                }
            } catch (Exception ignored) {
            }
        }
        return "Registration failed (HTTP " + statusCode + ")";
    }
}
