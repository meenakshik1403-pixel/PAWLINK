package com.pawlink.user;

import java.time.LocalDateTime;

/**
 * Abstract base class representing a User in PawLink.
 * Demonstrates Object-Oriented Principles:
 * - Abstraction: Exposes common user attributes and forces subclasses to implement getRole().
 * - Encapsulation: Private member fields accessible through guarded getters and setters.
 * - Inheritance: Extended by PetOwner, Veterinarian, PetSitter, and Admin.
 */
public abstract class User {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long userId, String name, String email, String phone, String passwordHash, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public User(String name, String email, String phone, String passwordHash) {
        this(null, name, email, phone, passwordHash, LocalDateTime.now());
    }

    /**
     * Polymorphic method to obtain the specific role of the user.
     * Subclasses return 'OWNER', 'VET', 'SITTER', or 'ADMIN'.
     *
     * @return the role constant string
     */
    public abstract String getRole();

    // Getters and Setters demonstrating encapsulation

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + getRole() + '\'' +
                '}';
    }
}
