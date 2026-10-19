package com.pawlink.user;

import java.time.LocalDateTime;

/**
 * Concrete User subclass representing a System Administrator.
 */
public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(Long userId, String name, String email, String phone, String passwordHash, LocalDateTime createdAt) {
        super(userId, name, email, phone, passwordHash, createdAt);
    }

    public Admin(String name, String email, String phone, String passwordHash) {
        super(name, email, phone, passwordHash);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
