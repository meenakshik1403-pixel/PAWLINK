package com.pawlink.vet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class VetDAO {

    private final JdbcTemplate jdbcTemplate;

    public VetDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isVerified(Long userId) {
        String sql = "SELECT verified FROM vets WHERE user_id = ?";
        List<Boolean> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getBoolean("verified"), userId);
        return !list.isEmpty() && list.get(0);
    }

    public void saveVet(Long userId, boolean verified) {
        String countSql = "SELECT COUNT(*) FROM vets WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
        if (count != null && count > 0) {
            jdbcTemplate.update("UPDATE vets SET verified = ? WHERE user_id = ?", verified, userId);
        } else {
            jdbcTemplate.update("INSERT INTO vets (user_id, verified) VALUES (?, ?)", userId, verified);
        }
    }

    public boolean approveVet(Long userId) {
        saveVet(userId, true);
        return true;
    }

    public List<Map<String, Object>> getAllVetsWithStatus() {
        String sql = """
            SELECT u.id, u.name, u.email, u.phone, COALESCE(v.verified, FALSE) AS verified
            FROM users u
            LEFT JOIN vets v ON u.id = v.user_id
            WHERE u.role = 'VET'
            ORDER BY u.name ASC
        """;
        return jdbcTemplate.queryForList(sql);
    }
}

