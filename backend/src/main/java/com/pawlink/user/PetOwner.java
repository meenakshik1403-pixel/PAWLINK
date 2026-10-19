package com.pawlink.user;

import java.time.LocalDateTime;

/**
 * Concrete User subclass representing a Pet Owner.
 */
public class PetOwner extends User {

    public PetOwner() {
        super();
    }

    public PetOwner(Long userId, String name, String email, String phone, String passwordHash, LocalDateTime createdAt) {
        super(userId, name, email, phone, passwordHash, createdAt);
    }

    public PetOwner(String name, String email, String phone, String passwordHash) {
        super(name, email, phone, passwordHash);
    }

    @Override
    public String getRole() {
        return "OWNER";
    }
}
