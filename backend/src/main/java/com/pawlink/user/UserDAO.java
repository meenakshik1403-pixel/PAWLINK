package com.pawlink.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User operations using Spring JdbcTemplate.
 * Strictly NO JPA / Hibernate annotations.
 * Uses parameterized SQL queries and custom RowMapper integrating UserFactory.
 */
@Repository
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper that translates a JDBC ResultSet row into a concrete User subclass
     * via the UserFactory pattern.
     */
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String passwordHash = rs.getString("password_hash");
        String role = rs.getString("role");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        return UserFactory.createUser(role, id, name, email, phone, passwordHash, createdAt);
    };

    /**
     * Inserts a new user record and populates the auto-generated primary key ID.
     *
     * @param user the User domain object to persist
     * @return the persisted User with its generated ID
     */
    public User insert(User user) {
        final String sql = "INSERT INTO users (name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole());
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) {
                idVal = keyHolder.getKeys().get("ID");
            }
            if (idVal instanceof Number num) {
                user.setUserId(num.longValue());
            }
        } else if (keyHolder.getKey() != null) {
            user.setUserId(keyHolder.getKey().longValue());
        }
        return user;
    }

    /**
     * Finds a user by their unique email using parameterized SQL.
     * Signature query: SELECT * FROM users WHERE email = ?
     *
     * @param email user email address
     * @return Optional containing the User if found, empty otherwise
     */
    public Optional<User> findByEmail(String email) {
        final String sql = "SELECT id, name, email, phone, password_hash, role, created_at FROM users WHERE email = ?";
        List<User> results = jdbcTemplate.query(sql, userRowMapper, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Finds a user by their primary key ID.
     *
     * @param id user ID
     * @return Optional containing the User if found
     */
    public Optional<User> findById(Long id) {
        final String sql = "SELECT id, name, email, phone, password_hash, role, created_at FROM users WHERE id = ?";
        List<User> results = jdbcTemplate.query(sql, userRowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Retrieves all users who hold the 'VET' role.
     *
     * @return list of Veterinarians
     */
    public List<User> findVets() {
        final String sql = "SELECT id, name, email, phone, password_hash, role, created_at FROM users WHERE role = 'VET' ORDER BY name ASC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    /**
     * Checks if a user already exists with the given email.
     *
     * @param email email to check
     * @return true if exists
     */
    public boolean existsByEmail(String email) {
        final String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Updates the password hash for a specific user email (e.g., admin sync from .env).
     *
     * @param email        user email
     * @param passwordHash new hashed password
     */
    public void updatePassword(String email, String passwordHash) {
        final String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        jdbcTemplate.update(sql, passwordHash, email);
    }
}
