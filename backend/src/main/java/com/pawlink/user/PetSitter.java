package com.pawlink.user;

import java.time.LocalDateTime;

/**
 * Concrete User subclass representing a Pet Sitter.
 */
public class PetSitter extends User {

    public PetSitter() {
        super();
    }

    public PetSitter(Long userId, String name, String email, String phone, String passwordHash, LocalDateTime createdAt) {
        super(userId, name, email, phone, passwordHash, createdAt);
    }

    public PetSitter(String name, String email, String phone, String passwordHash) {
        super(name, email, phone, passwordHash);
    }

    @Override
    public String getRole() {
        return "SITTER";
    }
}
