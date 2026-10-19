package com.pawlink.web.controller;

import com.pawlink.web.api.ApiGateway;
import com.pawlink.web.dto.AuthUserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Web Controller handling Authentication flows (Login, Register, Logout) on the Web Portal.
 * Interacts with the backend REST API via ApiGateway.
 */
@Controller
public class AuthWebController {

    private final ApiGateway apiGateway;

    public AuthWebController(ApiGateway apiGateway) {
        this.apiGateway = apiGateway;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/pets";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("email", email);
            payload.put("password", password);

            AuthUserDTO user = apiGateway.post("/api/auth/login", payload, AuthUserDTO.class);
            session.setAttribute("user", user);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin/stats";
            } else if ("SITTER".equalsIgnoreCase(user.getRole())) {
                return "redirect:/bookings";
            }
            return "redirect:/pets";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Login failed: " + ex.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam("name") String name,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  @RequestParam("role") String role,
                                  HttpSession session,
                                  Model model) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("name", name);
            payload.put("email", email);
            payload.put("password", password);
            payload.put("phone", phone);
            payload.put("role", role);

            AuthUserDTO user = apiGateway.post("/api/auth/register", payload, AuthUserDTO.class);
            session.setAttribute("user", user);

            return "redirect:/pets";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Registration failed: " + ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?error=Logged+out+successfully.";
    }
}
