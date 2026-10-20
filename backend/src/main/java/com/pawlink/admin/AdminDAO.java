package com.pawlink.admin;

import com.pawlink.admin.dto.SystemStatsDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data Access Object for Admin Sitter and Vet verification and system analytics.
 * STRICT: Plain SQL with Spring JdbcTemplate. No JPA/Hibernate.
 */
@Repository
public class AdminDAO {

    private final JdbcTemplate jdbcTemplate;

    public AdminDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Approves a registered sitter by toggling verified = TRUE, and updates their hourly rate if provided.
     */
    public boolean approveSitter(Long sitterId) {
        return approveSitter(sitterId, null);
    }

    public boolean approveSitter(Long sitterId, java.math.BigDecimal hourlyRate) {
        if (hourlyRate != null && hourlyRate.compareTo(java.math.BigDecimal.ZERO) > 0) {
            String sql = "UPDATE sitters SET verified = TRUE, hourly_rate = ? WHERE user_id = ?";
            return jdbcTemplate.update(sql, hourlyRate, sitterId) > 0;
        } else {
            String sql = "UPDATE sitters SET verified = TRUE WHERE user_id = ?";
            return jdbcTemplate.update(sql, sitterId) > 0;
        }
    }

    /**
     * Approves a registered veterinarian by toggling verified = TRUE in vets table.
     */
    public boolean approveVet(Long vetId) {
        String countSql = "SELECT COUNT(*) FROM vets WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, vetId);
        if (count != null && count > 0) {
            return jdbcTemplate.update("UPDATE vets SET verified = TRUE WHERE user_id = ?", vetId) > 0;
        } else {
            return jdbcTemplate.update("INSERT INTO vets (user_id, verified) VALUES (?, TRUE)", vetId) > 0;
        }
    }

    /**
     * Executes advanced analytical aggregation queries across users, appointments, and sitter bookings.
     */
    public SystemStatsDTO getStats() {
        SystemStatsDTO stats = new SystemStatsDTO();

        // 1. Total users
        Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        stats.setTotalUsers(totalUsers != null ? totalUsers : 0);

        // 2. Users grouped by role
        String roleSql = "SELECT role, COUNT(*) AS cnt FROM users GROUP BY role ORDER BY cnt DESC";
        Map<String, Integer> usersByRole = new LinkedHashMap<>();
        jdbcTemplate.query(roleSql, rs -> {
            usersByRole.put(rs.getString("role"), rs.getInt("cnt"));
        });
        stats.setUsersByRole(usersByRole);

        // 3. Appointments per Veterinarian (Advanced Aggregation with JOIN & GROUP BY)
        String vetApptSql = """
            SELECT u.name, COUNT(a.id) AS cnt
            FROM users u
            LEFT JOIN appointments a ON a.vet_id = u.id
            WHERE u.role = 'VET'
            GROUP BY u.name
            ORDER BY cnt DESC, u.name ASC
        """;
        Map<String, Integer> apptsPerVet = new LinkedHashMap<>();
        jdbcTemplate.query(vetApptSql, rs -> {
            apptsPerVet.put(rs.getString("name"), rs.getInt("cnt"));
        });
        stats.setAppointmentsPerVet(apptsPerVet);

        // 4. Bookings per Sitter (Advanced Aggregation with JOIN & GROUP BY)
        String sitterBookingSql = """
            SELECT u.name, COUNT(b.id) AS cnt
            FROM users u
            LEFT JOIN sitter_bookings b ON b.sitter_id = u.id
            WHERE u.role = 'SITTER'
            GROUP BY u.name
            ORDER BY cnt DESC, u.name ASC
        """;
        Map<String, Integer> bookingsPerSitter = new LinkedHashMap<>();
        jdbcTemplate.query(sitterBookingSql, rs -> {
            bookingsPerSitter.put(rs.getString("name"), rs.getInt("cnt"));
        });
        stats.setBookingsPerSitter(bookingsPerSitter);

        return stats;
    }
}
