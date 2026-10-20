package com.pawlink.config;

import com.pawlink.user.User;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Automatically seeds demo accounts and synchronizes the Platform Administrator
 * account configured via environment variables (.env).
 * Admin accounts cannot be registered publicly via REST or UI.
 */
@Component
public class DatabaseSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    // Default demo password hash for 'password123'
    private static final String DEFAULT_HASH = "$2a$10$zPfq9J9dMrCMLI8E1qc9dOenskj7Nx4OGdPqhkEoEn.fdV3mvld0K";

    private final UserDAO userDAO;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${pawlink.admin.email:admin@pawlink.com}")
    private String adminEmail;

    @Value("${pawlink.admin.password:password123}")
    private String adminPassword;

    @Value("${pawlink.admin.name:Platform Administrator}")
    private String adminName;

    @Value("${pawlink.admin.phone:+919876543001}")
    private String adminPhone;

    public DatabaseSeeder(UserDAO userDAO, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Seed standard demo users (Vets, Sitters, Owners) with +91 10-digit phones
        seedUserIfNotExists("Dr. Sarah Jenkins", "vet.sarah@pawlink.com", "+919876543101", "VET");
        seedUserIfNotExists("Dr. Michael Brown", "vet.michael@pawlink.com", "+919876543102", "VET");
        seedUserIfNotExists("Emily Watson", "sitter.emily@pawlink.com", "+919876543201", "SITTER");
        seedUserIfNotExists("David Clark", "sitter.david@pawlink.com", "+919876543202", "SITTER");
        seedUserIfNotExists("John Doe", "owner.john@pawlink.com", "+919876543301", "OWNER");
        seedUserIfNotExists("Jane Smith", "owner.jane@pawlink.com", "+919876543302", "OWNER");

        // Ensure demo sitters have profiles (Emily verified, David unverified)
        ensureSitterProfile("sitter.emily@pawlink.com", 250.0, true);
        ensureSitterProfile("sitter.david@pawlink.com", 200.0, false);

        // Ensure demo vets have profiles (Sarah and Michael verified)
        ensureVetProfile("vet.sarah@pawlink.com", true);
        ensureVetProfile("vet.michael@pawlink.com", true);

        // Seed or synchronize Administrator from .env configuration
        seedOrSyncAdmin();

        // Safely consolidate duplicate pets
        deduplicatePets();

        log.info("PawLink Database Seeder completed check. Total veterinarians available: {}", userDAO.findVets().size());
    }

    private void ensureSitterProfile(String email, double hourlyRate, boolean verified) {
        userDAO.findByEmail(email).ifPresent(user -> {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sitters WHERE user_id = ?",
                    Integer.class,
                    user.getUserId()
            );
            if (count == null || count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO sitters (user_id, hourly_rate, verified) VALUES (?, ?, ?)",
                        user.getUserId(), hourlyRate, verified
                );
            }
        });
    }

    private void ensureVetProfile(String email, boolean verified) {
        userDAO.findByEmail(email).ifPresent(user -> {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vets WHERE user_id = ?",
                    Integer.class,
                    user.getUserId()
            );
            if (count == null || count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO vets (user_id, verified) VALUES (?, ?)",
                        user.getUserId(), verified
                );
            }
        });
    }

    private void seedOrSyncAdmin() {
        if (adminEmail == null || adminEmail.trim().isEmpty() || adminPassword == null || adminPassword.trim().isEmpty()) {
            log.warn("Admin email or password not specified; skipping Admin synchronization.");
            return;
        }

        String email = adminEmail.trim().toLowerCase();
        String hashedPassword = passwordEncoder.encode(adminPassword.trim());

        if (userDAO.existsByEmail(email)) {
            userDAO.updatePassword(email, hashedPassword);
            log.info("Admin account '{}' detected; synchronized password hash from environment.", email);
        } else {
            User adminUser = UserFactory.createUser("ADMIN", adminName, email, adminPhone, hashedPassword);
            userDAO.insert(adminUser);
            log.info("Created Platform Administrator '{}' ({}) from environment configuration.", adminName, email);
        }
    }

    private void deduplicatePets() {
        try {
            List<Map<String, Object>> duplicates = jdbcTemplate.queryForList(
                "SELECT owner_id, LOWER(name) AS lname, MIN(id) AS keep_id " +
                "FROM pets GROUP BY owner_id, LOWER(name) HAVING COUNT(*) > 1"
            );
            for (Map<String, Object> dup : duplicates) {
                Long keepId = ((Number) dup.get("keep_id")).longValue();
                Long ownerId = ((Number) dup.get("owner_id")).longValue();
                String lname = (String) dup.get("lname");
                List<Long> removeIds = jdbcTemplate.queryForList(
                    "SELECT id FROM pets WHERE owner_id = ? AND LOWER(name) = ? AND id != ?",
                    Long.class, ownerId, lname, keepId
                );
                for (Long remId : removeIds) {
                    try { jdbcTemplate.update("UPDATE appointments SET pet_id = ? WHERE pet_id = ?", keepId, remId); } catch (Exception ignored) {}
                    try { jdbcTemplate.update("UPDATE medical_records SET pet_id = ? WHERE pet_id = ?", keepId, remId); } catch (Exception ignored) {}
                    try { jdbcTemplate.update("UPDATE vaccinations SET pet_id = ? WHERE pet_id = ?", keepId, remId); } catch (Exception ignored) {}
                    try { jdbcTemplate.update("UPDATE reminders SET pet_id = ? WHERE pet_id = ?", keepId, remId); } catch (Exception ignored) {}
                    try { jdbcTemplate.update("UPDATE sitter_bookings SET pet_id = ? WHERE pet_id = ?", keepId, remId); } catch (Exception ignored) {}
                    jdbcTemplate.update("DELETE FROM pets WHERE id = ?", remId);
                }
            }
            if (!duplicates.isEmpty()) {
                log.info("Deduplicated {} duplicate pet cluster(s) from database.", duplicates.size());
            }
        } catch (Exception ex) {
            log.warn("Pet deduplication skipped or error: {}", ex.getMessage());
        }
    }

    private void seedUserIfNotExists(String name, String email, String phone, String role) {
        if (!userDAO.existsByEmail(email)) {
            User user = UserFactory.createUser(role, name, email, phone, DEFAULT_HASH);
            userDAO.insert(user);
            log.info("Seeded demo user: {} [{}] ({})", name, role, email);
        }
    }
}
