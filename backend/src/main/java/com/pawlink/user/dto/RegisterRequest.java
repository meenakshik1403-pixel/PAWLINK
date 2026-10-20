package com.pawlink.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration.
 * ADMIN role cannot be registered publicly; only OWNER, VET, and SITTER are permitted.
 * Phone numbers are strictly 10 digits with a +91 prefix.
 */
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Pattern(regexp = "^\\+91\\d{10}$", message = "Phone number must be in +91XXXXXXXXXX format with strictly 10 digits.")
    private String phone;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "(?i)^(OWNER|VET|SITTER)$", message = "Role must be OWNER, VET, or SITTER (ADMIN accounts cannot be registered publicly)")
    private String role;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password, String phone, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        setPhone(phone);
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null) {
            String trimmed = phone.trim().replaceAll("[\\s\\-]+", "");
            if (trimmed.matches("^\\d{10}$")) {
                this.phone = "+91" + trimmed;
            } else if (trimmed.matches("^\\+91\\d{10}$")) {
                this.phone = trimmed;
            } else {
                this.phone = phone;
            }
        } else {
            this.phone = null;
        }
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
