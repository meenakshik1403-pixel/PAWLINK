package com.pawlink.appointment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Appointments using Spring JdbcTemplate.
 * Strictly adheres to technical constraints:
 * - NO JPA/Hibernate annotations (@Entity, @Table, @Id).
 * - Plain SQL with parameterized queries.
 * - Multi-table SQL JOIN with users (vet and owner) and pets.
 */
@Repository
public class AppointmentDAO {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Appointment> appointmentRowMapper = (rs, rowNum) -> {
        Appointment a = new Appointment();
        a.setId(rs.getLong("id"));
        a.setVetId(rs.getLong("vet_id"));
        a.setVetName(rs.getString("vet_name"));
        a.setPetId(rs.getLong("pet_id"));
        a.setPetName(rs.getString("pet_name"));
        a.setOwnerId(rs.getLong("owner_id"));
        a.setOwnerName(rs.getString("owner_name"));

        Timestamp startTs = rs.getTimestamp("start_time");
        a.setStartTime(startTs != null ? startTs.toLocalDateTime() : null);

        Timestamp endTs = rs.getTimestamp("end_time");
        a.setEndTime(endTs != null ? endTs.toLocalDateTime() : null);

        a.setStatus(rs.getString("status"));

        Timestamp crTs = rs.getTimestamp("created_at");
        a.setCreatedAt(crTs != null ? crTs.toLocalDateTime() : null);

        return a;
    };

    /**
     * Checks if a veterinarian already has an active, non-cancelled appointment overlapping
     * the requested time interval.
     * Interval intersection formula: start_time < :new_end AND end_time > :new_start
     *
     * @param vetId veterinarian user ID
     * @param start requested appointment start time
     * @param end   requested appointment end time
     * @return true if an overlap conflict exists
     */
    public boolean hasConflict(Long vetId, LocalDateTime start, LocalDateTime end) {
        final String sql = "SELECT COUNT(*) FROM appointments " +
                "WHERE vet_id = ? " +
                "  AND status != 'CANCELLED' " +
                "  AND start_time < ? " +
                "  AND end_time > ?";

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                vetId,
                Timestamp.valueOf(end),
                Timestamp.valueOf(start)
        );
        return count != null && count > 0;
    }

    /**
     * Inserts a new appointment record and retrieves the auto-generated primary key ID.
     *
     * @param appointment appointment entity
     * @return generated ID
     */
    public Long insert(Appointment appointment) {
        final String sql = "INSERT INTO appointments (vet_id, pet_id, owner_id, start_time, end_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String status = appointment.getStatus() != null ? appointment.getStatus() : "PENDING";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, appointment.getVetId());
            ps.setLong(2, appointment.getPetId());
            ps.setLong(3, appointment.getOwnerId());
            ps.setTimestamp(4, Timestamp.valueOf(appointment.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(appointment.getEndTime()));
            ps.setString(6, status);
            return ps;
        }, keyHolder);

        Long generatedId = null;
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && !keys.isEmpty()) {
            Object idVal = keys.get("id");
            if (idVal == null) {
                idVal = keys.get("ID");
            }
            if (idVal instanceof Number num) {
                generatedId = num.longValue();
            }
        } else {
            Number singleKey = keyHolder.getKey();
            if (singleKey != null) {
                generatedId = singleKey.longValue();
            }
        }

        if (generatedId == null) {
            throw new IllegalStateException("Failed to retrieve generated appointment ID.");
        }

        appointment.setId(generatedId);
        appointment.setStatus(status);
        return generatedId;
    }

    /**
     * Finds appointments scheduled for a specific veterinarian, optionally filtered by date.
     * Uses multi-table SQL JOIN to populate vet name, pet name, and owner name.
     *
     * @param vetId veterinarian ID
     * @param date  optional date filter
     * @return list of matching appointments ordered by start_time ASC
     */
    public List<Appointment> findByVetAndDate(Long vetId, LocalDate date) {
        if (date != null) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            final String sql = "SELECT a.id, a.vet_id, u_vet.name AS vet_name, " +
                    "       a.pet_id, p.name AS pet_name, " +
                    "       a.owner_id, u_owner.name AS owner_name, " +
                    "       a.start_time, a.end_time, a.status, a.created_at " +
                    "FROM appointments a " +
                    "LEFT JOIN users u_vet ON a.vet_id = u_vet.id " +
                    "LEFT JOIN pets p ON a.pet_id = p.id " +
                    "LEFT JOIN users u_owner ON a.owner_id = u_owner.id " +
                    "WHERE a.vet_id = ? " +
                    "  AND a.start_time >= ? " +
                    "  AND a.start_time < ? " +
                    "ORDER BY a.start_time ASC";

            return jdbcTemplate.query(sql, appointmentRowMapper, vetId, Timestamp.valueOf(dayStart), Timestamp.valueOf(dayEnd));
        } else {
            final String sql = "SELECT a.id, a.vet_id, u_vet.name AS vet_name, " +
                    "       a.pet_id, p.name AS pet_name, " +
                    "       a.owner_id, u_owner.name AS owner_name, " +
                    "       a.start_time, a.end_time, a.status, a.created_at " +
                    "FROM appointments a " +
                    "LEFT JOIN users u_vet ON a.vet_id = u_vet.id " +
                    "LEFT JOIN pets p ON a.pet_id = p.id " +
                    "LEFT JOIN users u_owner ON a.owner_id = u_owner.id " +
                    "WHERE a.vet_id = ? " +
                    "ORDER BY a.start_time ASC";

            return jdbcTemplate.query(sql, appointmentRowMapper, vetId);
        }
    }

    /**
     * Finds appointments booked by a specific pet owner.
     *
     * @param ownerId owner user ID
     * @return list of appointments ordered by start_time ASC
     */
    public List<Appointment> findByOwnerId(Long ownerId) {
        return findByOwnerIdAndDate(ownerId, null);
    }

    /**
     * Finds appointments booked by a specific pet owner, optionally filtered by date.
     *
     * @param ownerId owner user ID
     * @param date    optional date filter
     * @return list of appointments ordered by start_time ASC
     */
    public List<Appointment> findByOwnerIdAndDate(Long ownerId, LocalDate date) {
        if (date != null) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            final String sql = "SELECT a.id, a.vet_id, u_vet.name AS vet_name, " +
                    "       a.pet_id, p.name AS pet_name, " +
                    "       a.owner_id, u_owner.name AS owner_name, " +
                    "       a.start_time, a.end_time, a.status, a.created_at " +
                    "FROM appointments a " +
                    "LEFT JOIN users u_vet ON a.vet_id = u_vet.id " +
                    "LEFT JOIN pets p ON a.pet_id = p.id " +
                    "LEFT JOIN users u_owner ON a.owner_id = u_owner.id " +
                    "WHERE a.owner_id = ? " +
                    "  AND a.start_time >= ? " +
                    "  AND a.start_time < ? " +
                    "ORDER BY a.start_time ASC";

            return jdbcTemplate.query(sql, appointmentRowMapper, ownerId, Timestamp.valueOf(dayStart), Timestamp.valueOf(dayEnd));
        } else {
            final String sql = "SELECT a.id, a.vet_id, u_vet.name AS vet_name, " +
                    "       a.pet_id, p.name AS pet_name, " +
                    "       a.owner_id, u_owner.name AS owner_name, " +
                    "       a.start_time, a.end_time, a.status, a.created_at " +
                    "FROM appointments a " +
                    "LEFT JOIN users u_vet ON a.vet_id = u_vet.id " +
                    "LEFT JOIN pets p ON a.pet_id = p.id " +
                    "LEFT JOIN users u_owner ON a.owner_id = u_owner.id " +
                    "WHERE a.owner_id = ? " +
                    "ORDER BY a.start_time ASC";

            return jdbcTemplate.query(sql, appointmentRowMapper, ownerId);
        }
    }

    /**
     * Finds all platform appointments, optionally filtered by date.
     *
     * @param date optional date filter
     * @return list of appointments ordered by start_time ASC
     */
    public List<Appointment> findAll(LocalDate date) {
        if (date != null) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            final String sql = "SELECT a.id, a.vet_id, u_vet.name AS vet_name, " +
                    "       a.pet_id, p.name AS pet_name, " +
                    "       a.owner_id, u_owner.name AS owner_name, " +
                    "       a.start_time, a.end_time, a.status, a.created_at " +
                    "FROM appointments a " +
                    "LEFT JOIN users u_vet ON a.vet_id = u_vet.id " +
                    "LEFT JOIN pets p ON a.pet_id = p.id " +
                    "LEFT JOIN users u_owner ON a.owner_id = u_owner.id " +
                    "WHERE a.start_time >= ? " +
                    "  AND a.start_time < ? " +
                    "ORDER BY a.start_time ASC";

            return jdbcTemplate.query(sql, appointmentRowMapper, Timestamp.valueOf(dayStart), Timestamp.valueOf(dayEnd));
        } else {
            final String sql = "SELECT a.id, a.vet_id, u_vet.name AS vet_name, " +
                    "       a.pet_id, p.name AS pet_name, " +
                    "       a.owner_id, u_owner.name AS owner_name, " +
                    "       a.start_time, a.end_time, a.status, a.created_at " +
                    "FROM appointments a " +
                    "LEFT JOIN users u_vet ON a.vet_id = u_vet.id " +
                    "LEFT JOIN pets p ON a.pet_id = p.id " +
                    "LEFT JOIN users u_owner ON a.owner_id = u_owner.id " +
                    "ORDER BY a.start_time ASC";

            return jdbcTemplate.query(sql, appointmentRowMapper);
        }
    }

    /**
     * Finds a single appointment by ID.
     *
     * @param id appointment ID
     * @return Optional containing Appointment if found
     */
    public Optional<Appointment> findById(Long id) {
        final String sql = "SELECT a.id, a.vet_id, u_vet.name AS vet_name, " +
                "       a.pet_id, p.name AS pet_name, " +
                "       a.owner_id, u_owner.name AS owner_name, " +
                "       a.start_time, a.end_time, a.status, a.created_at " +
                "FROM appointments a " +
                "LEFT JOIN users u_vet ON a.vet_id = u_vet.id " +
                "LEFT JOIN pets p ON a.pet_id = p.id " +
                "LEFT JOIN users u_owner ON a.owner_id = u_owner.id " +
                "WHERE a.id = ?";

        List<Appointment> list = jdbcTemplate.query(sql, appointmentRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Updates an appointment's status (CONFIRMED, COMPLETED, CANCELLED).
     *
     * @param appointmentId appointment ID
     * @param newStatus     new status string
     */
    public void updateStatus(Long appointmentId, String newStatus) {
        final String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, newStatus, appointmentId);
    }

    /**
     * Updates an appointment's end time (e.g. when consultation concludes early).
     *
     * @param appointmentId appointment ID
     * @param endTime       new end time
     */
    public void updateEndTime(Long appointmentId, LocalDateTime endTime) {
        final String sql = "UPDATE appointments SET end_time = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(endTime), appointmentId);
    }
}
