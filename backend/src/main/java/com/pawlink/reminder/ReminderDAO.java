package com.pawlink.reminder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Reminders using Spring JdbcTemplate.
 * Strictly adheres to technical constraints:
 * - NO JPA/Hibernate annotations (@Entity, @Table, @Id).
 * - Plain SQL with parameterized queries.
 * - Multi-table SQL JOIN with pets and users to populate owner contact info for notifications.
 */
@Repository
public class ReminderDAO {

    private final JdbcTemplate jdbcTemplate;

    public ReminderDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reminder> reminderRowMapper = (rs, rowNum) -> {
        Reminder r = new Reminder();
        r.setId(rs.getLong("id"));
        r.setPetId(rs.getLong("pet_id"));
        r.setPetName(rs.getString("pet_name"));
        r.setOwnerId(rs.getLong("owner_id"));
        r.setOwnerName(rs.getString("owner_name"));
        r.setOwnerEmail(rs.getString("owner_email"));
        r.setOwnerPhone(rs.getString("owner_phone"));
        r.setType(rs.getString("type"));

        Date d = rs.getDate("due_date");
        r.setDueDate(d != null ? d.toLocalDate() : null);

        r.setSent(rs.getBoolean("sent"));

        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return r;
    };

    /**
     * Finds all active reminders that are due on or before CURRENT_DATE and have not been sent yet.
     * Signature query: WHERE r.due_date <= CURRENT_DATE AND r.sent = FALSE
     */
    public List<Reminder> findDueReminders() {
        final String sql = "SELECT r.id, r.pet_id, r.type, r.due_date, r.sent, r.created_at, " +
                "       p.name AS pet_name, " +
                "       u.id AS owner_id, u.name AS owner_name, u.email AS owner_email, u.phone AS owner_phone " +
                "FROM reminders r " +
                "JOIN pets p ON r.pet_id = p.id " +
                "JOIN users u ON p.owner_id = u.id " +
                "WHERE r.due_date <= CURRENT_DATE AND r.sent = FALSE " +
                "ORDER BY r.due_date ASC";

        return jdbcTemplate.query(sql, reminderRowMapper);
    }

    /**
     * Marks a reminder as sent after observers have been notified.
     *
     * @param reminderId reminder ID
     */
    public void markAsSent(Long reminderId) {
        final String sql = "UPDATE reminders SET sent = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, reminderId);
    }

    /**
     * Retrieves all reminders for pets belonging to a specific pet owner.
     *
     * @param ownerId user ID of pet owner
     * @return list of reminders sorted by due_date ASC
     */
    public List<Reminder> findByOwnerId(Long ownerId) {
        final String sql = "SELECT r.id, r.pet_id, r.type, r.due_date, r.sent, r.created_at, " +
                "       p.name AS pet_name, " +
                "       u.id AS owner_id, u.name AS owner_name, u.email AS owner_email, u.phone AS owner_phone " +
                "FROM reminders r " +
                "JOIN pets p ON r.pet_id = p.id " +
                "JOIN users u ON p.owner_id = u.id " +
                "WHERE p.owner_id = ? " +
                "ORDER BY r.due_date ASC";

        return jdbcTemplate.query(sql, reminderRowMapper, ownerId);
    }

    /**
     * Retrieves all reminders for a specific pet.
     */
    public List<Reminder> findByPetId(Long petId) {
        final String sql = "SELECT r.id, r.pet_id, r.type, r.due_date, r.sent, r.created_at, " +
                "       p.name AS pet_name, " +
                "       u.id AS owner_id, u.name AS owner_name, u.email AS owner_email, u.phone AS owner_phone " +
                "FROM reminders r " +
                "JOIN pets p ON r.pet_id = p.id " +
                "JOIN users u ON p.owner_id = u.id " +
                "WHERE r.pet_id = ? " +
                "ORDER BY r.due_date ASC";

        return jdbcTemplate.query(sql, reminderRowMapper, petId);
    }

    /**
     * Retrieves all reminders in the system.
     */
    public List<Reminder> findAll() {
        final String sql = "SELECT r.id, r.pet_id, r.type, r.due_date, r.sent, r.created_at, " +
                "       p.name AS pet_name, " +
                "       u.id AS owner_id, u.name AS owner_name, u.email AS owner_email, u.phone AS owner_phone " +
                "FROM reminders r " +
                "JOIN pets p ON r.pet_id = p.id " +
                "JOIN users u ON p.owner_id = u.id " +
                "ORDER BY r.due_date ASC";

        return jdbcTemplate.query(sql, reminderRowMapper);
    }

    /**
     * Finds a single reminder by ID.
     */
    public Optional<Reminder> findById(Long id) {
        final String sql = "SELECT r.id, r.pet_id, r.type, r.due_date, r.sent, r.created_at, " +
                "       p.name AS pet_name, " +
                "       u.id AS owner_id, u.name AS owner_name, u.email AS owner_email, u.phone AS owner_phone " +
                "FROM reminders r " +
                "JOIN pets p ON r.pet_id = p.id " +
                "JOIN users u ON p.owner_id = u.id " +
                "WHERE r.id = ?";

        List<Reminder> list = jdbcTemplate.query(sql, reminderRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Inserts a new reminder record and retrieves the auto-generated ID.
     *
     * @param reminder reminder entity
     * @return generated ID
     */
    public Long insert(Reminder reminder) {
        final String sql = "INSERT INTO reminders (pet_id, type, due_date, sent) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reminder.getPetId());
            ps.setString(2, reminder.getType());
            ps.setDate(3, Date.valueOf(reminder.getDueDate()));
            ps.setBoolean(4, reminder.isSent());
            return ps;
        }, keyHolder);

        Long generatedId = null;
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) {
                idVal = keyHolder.getKeys().get("ID");
            }
            if (idVal instanceof Number num) {
                generatedId = num.longValue();
            }
        } else if (keyHolder.getKey() != null) {
            generatedId = keyHolder.getKey().longValue();
        }

        if (generatedId == null) {
            throw new IllegalStateException("Failed to retrieve generated reminder ID.");
        }

        reminder.setId(generatedId);
        return generatedId;
    }

    /**
     * Deletes a reminder by ID.
     */
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reminders WHERE id = ?", id);
    }
}

