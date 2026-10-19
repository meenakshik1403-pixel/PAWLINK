package com.pawlink.web.dto;

import java.io.Serializable;

/**
 * Session User DTO stored in HttpSession on the Web Portal.
 */
public class AuthUserDTO implements Serializable {

    private Long id;
    private String name;
    private String email;
    private String role;

    public AuthUserDTO() {
    }

    public AuthUserDTO(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isOwner() {
        return "OWNER".equalsIgnoreCase(role);
    }

    public boolean isVet() {
        return "VET".equalsIgnoreCase(role);
    }

    public boolean isSitter() {
        return "SITTER".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
