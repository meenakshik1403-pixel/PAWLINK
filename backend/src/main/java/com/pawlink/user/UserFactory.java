package com.pawlink.user;

import java.time.LocalDateTime;

/**
 * Factory Pattern implementation for instantiating the appropriate {@link User} subclass.
 * Decouples caller code from concrete subclass constructors.
 */
public class UserFactory {

    private UserFactory() {
        // Prevent direct instantiation of factory class
    }

    /**
     * Instantiates a User subclass based on the provided role string.
     *
     * @param role         User role ('OWNER', 'VET', 'SITTER', 'ADMIN')
     * @param userId       Database primary key ID
     * @param name         Full name
     * @param email        Unique email address
     * @param phone        Contact phone number
     * @param passwordHash BCrypt hashed password
     * @param createdAt    Record creation timestamp
     * @return a concrete {@link User} instance
     * @throws IllegalArgumentException if the role is null or unrecognized
     */
    public static User createUser(String role, Long userId, String name, String email,
                                  String phone, String passwordHash, LocalDateTime createdAt) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("User role cannot be null or empty.");
        }

        String normalizedRole = role.trim().toUpperCase();
        return switch (normalizedRole) {
            case "OWNER" -> new PetOwner(userId, name, email, phone, passwordHash, createdAt);
            case "VET" -> new Veterinarian(userId, name, email, phone, passwordHash, createdAt);
            case "SITTER" -> new PetSitter(userId, name, email, phone, passwordHash, createdAt);
            case "ADMIN" -> new Admin(userId, name, email, phone, passwordHash, createdAt);
            default -> throw new IllegalArgumentException("Unsupported user role: '" + role + "'. Must be OWNER, VET, SITTER, or ADMIN.");
        };
    }

    /**
     * Overloaded factory method for creating new (pre-persistence) User instances.
     *
     * @param role         User role
     * @param name         Full name
     * @param email        Unique email
     * @param phone        Contact phone
     * @param passwordHash BCrypt hashed password
     * @return a concrete {@link User} instance
     */
    public static User createUser(String role, String name, String email, String phone, String passwordHash) {
        return createUser(role, null, name, email, phone, passwordHash, LocalDateTime.now());
    }
}
