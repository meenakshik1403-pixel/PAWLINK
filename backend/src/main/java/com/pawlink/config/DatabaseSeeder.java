package com.pawlink.config;

import com.pawlink.user.User;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Automatically seeds the database with default demo accounts at startup if not already present.
 * Ensures the system is ready for immediate live evaluation and Swing UI login.
 */
@Component
public class DatabaseSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    // Hash for 'password123'
    private static final String DEFAULT_HASH = "$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K";

    private final UserDAO userDAO;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSeeder(UserDAO userDAO, JdbcTemplate jdbcTemplate) {
        this.userDAO = userDAO;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUserIfNotExists("Dr. Sarah Jenkins", "vet.sarah@pawlink.com", "+1-555-0101", "VET");
        seedUserIfNotExists("Dr. Michael Brown", "vet.michael@pawlink.com", "+1-555-0102", "VET");
        seedUserIfNotExists("Emily Watson", "sitter.emily@pawlink.com", "+1-555-0201", "SITTER");
        seedUserIfNotExists("David Clark", "sitter.david@pawlink.com", "+1-555-0202", "SITTER");
        seedUserIfNotExists("Alice Sterling (Admin)", "admin@pawlink.com", "+1-555-0301", "ADMIN");
        seedUserIfNotExists("John Doe", "owner.john@pawlink.com", "+1-555-0401", "OWNER");
        seedUserIfNotExists("Jane Smith", "owner.jane@pawlink.com", "+1-555-0402", "OWNER");

        log.info("PawLink Database Seeder completed check. Total veterinarians available: {}", userDAO.findVets().size());
    }

    private void seedUserIfNotExists(String name, String email, String phone, String role) {
        if (!userDAO.existsByEmail(email)) {
            User user = UserFactory.createUser(role, name, email, phone, DEFAULT_HASH);
            userDAO.insert(user);
            log.info("Seeded demo user: {} [{}] ({})", name, role, email);
        }
    }
}

