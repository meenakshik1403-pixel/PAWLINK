package com.pawlink.sitter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Sitters table using Spring JdbcTemplate and plain SQL.
 * STRICT: No JPA/Hibernate.
 */
@Repository
public class SitterDAO {

    private final JdbcTemplate jdbcTemplate;

    public SitterDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Sitter> sitterRowMapper = new RowMapper<>() {
        @Override
        public Sitter mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Sitter(
                    rs.getLong("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getBigDecimal("hourly_rate"),
                    rs.getBoolean("verified")
            );
        }
    };

    /**
     * Checks if a sitter is verified by ID.
     * Returns false if sitter record does not exist or verified is false.
     */
    public boolean isVerified(Long sitterId) {
        if (sitterId == null) return false;
        String sql = "SELECT verified FROM sitters WHERE user_id = ?";
        List<Boolean> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getBoolean("verified"), sitterId);
        return !list.isEmpty() && Boolean.TRUE.equals(list.get(0));
    }

    /**
     * Retrieves all registered sitters with their user profile info.
     */
    public List<Sitter> getSitters() {
        String sql = """
            SELECT s.user_id, u.name, u.email, u.phone, s.hourly_rate, s.verified
            FROM sitters s
            JOIN users u ON s.user_id = u.id
            ORDER BY s.verified DESC, u.name ASC
        """;
        return jdbcTemplate.query(sql, sitterRowMapper);
    }

    /**
     * Finds a specific sitter by user ID.
     */
    public Optional<Sitter> findById(Long sitterId) {
        if (sitterId == null) return Optional.empty();
        String sql = """
            SELECT s.user_id, u.name, u.email, u.phone, s.hourly_rate, s.verified
            FROM sitters s
            JOIN users u ON s.user_id = u.id
            WHERE s.user_id = ?
        """;
        List<Sitter> list = jdbcTemplate.query(sql, sitterRowMapper, sitterId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Saves or updates a sitter profile.
     */
    public void saveSitter(Long userId, BigDecimal hourlyRate, boolean verified) {
        String countSql = "SELECT COUNT(*) FROM sitters WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
        if (count != null && count > 0) {
            String updateSql = "UPDATE sitters SET hourly_rate = ?, verified = ? WHERE user_id = ?";
            jdbcTemplate.update(updateSql, hourlyRate, verified, userId);
        } else {
            String insertSql = "INSERT INTO sitters (user_id, hourly_rate, verified) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, hourlyRate, verified);
        }
    }

    /**
     * Updates only the verification status of a sitter.
     */
    public boolean updateVerification(Long sitterId, boolean verified) {
        String sql = "UPDATE sitters SET verified = ? WHERE user_id = ?";
        return jdbcTemplate.update(sql, verified, sitterId) > 0;
    }
}

