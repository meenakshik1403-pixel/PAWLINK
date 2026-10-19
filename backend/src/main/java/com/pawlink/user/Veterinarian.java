package com.pawlink.user;

import java.time.LocalDateTime;

/**
 * Concrete User subclass representing a licensed Veterinarian.
 */
public class Veterinarian extends User {

    public Veterinarian() {
        super();
    }

    public Veterinarian(Long userId, String name, String email, String phone, String passwordHash, LocalDateTime createdAt) {
        super(userId, name, email, phone, passwordHash, createdAt);
    }

    public Veterinarian(String name, String email, String phone, String passwordHash) {
        super(name, email, phone, passwordHash);
    }

    @Override
    public String getRole() {
        return "VET";
    }
}
