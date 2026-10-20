package com.pawlink.admin;

import com.pawlink.admin.dto.SystemStatsDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data Access Object for Admin Sitter verification and system analytics.
 * STRICT: Plain SQL with Spring JdbcTemplate. No JPA/Hibernate.
 */
@Repository
public class AdminDAO {

    private final JdbcTemplate jdbcTemplate;

    public AdminDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Approves a registered sitter by toggling verified = TRUE.
     */
    public boolean approveSitter(Long sitterId) {
        String sql = "UPDATE sitters SET verified = TRUE WHERE user_id = ?";
        return jdbcTemplate.update(sql, sitterId) > 0;
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

        // 4. Bookings per Pet Sitter (Advanced Aggregation with JOIN & GROUP BY)
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

        // 5. Total volume counts
        Integer totalAppts = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointments", Integer.class);
        stats.setTotalAppointments(totalAppts != null ? totalAppts : 0);

        Integer totalBookings = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sitter_bookings", Integer.class);
        stats.setTotalBookings(totalBookings != null ? totalBookings : 0);

        return stats;
    }
}

