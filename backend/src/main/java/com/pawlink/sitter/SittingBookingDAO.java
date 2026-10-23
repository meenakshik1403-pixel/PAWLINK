package com.pawlink.sitter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Sitter Bookings, Payments, and Check-ins using Spring JdbcTemplate.
 * STRICT: No JPA/Hibernate.
 */
@Repository
public class SittingBookingDAO {

    private final JdbcTemplate jdbcTemplate;

    public SittingBookingDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SittingBooking> bookingRowMapper = (rs, rowNum) -> {
        SittingBooking b = new SittingBooking();
        b.setId(rs.getLong("id"));
        b.setPetId(rs.getLong("pet_id"));
        b.setPetName(rs.getString("pet_name"));
        b.setSitterId(rs.getLong("sitter_id"));
        b.setSitterName(rs.getString("sitter_name"));
        b.setOwnerId(rs.getLong("owner_id"));
        b.setOwnerName(rs.getString("owner_name"));

        Timestamp startTs = rs.getTimestamp("start_time");
        b.setStartTime(startTs != null ? startTs.toLocalDateTime() : null);

        Timestamp endTs = rs.getTimestamp("end_time");
        b.setEndTime(endTs != null ? endTs.toLocalDateTime() : null);

        b.setStatus(rs.getString("status"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        b.setCreatedAt(createdTs != null ? createdTs.toLocalDateTime() : null);

        b.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setPaymentMethod(rs.getString("payment_method"));
        return b;
    };

    private final RowMapper<CheckIn> checkInRowMapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("timestamp");
        return new CheckIn(
                rs.getLong("id"),
                rs.getLong("booking_id"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("note")
        );
    };

    private static final String BASE_SELECT = """
        SELECT b.id, b.pet_id, p.name AS pet_name, b.sitter_id, us.name AS sitter_name,
               b.owner_id, uo.name AS owner_name, b.start_time, b.end_time, b.status, b.created_at,
               s.hourly_rate, pm.amount AS total_amount, pm.status AS payment_status, pm.method AS payment_method
        FROM sitter_bookings b
        JOIN pets p ON b.pet_id = p.id
        JOIN users us ON b.sitter_id = us.id
        JOIN users uo ON b.owner_id = uo.id
        LEFT JOIN sitters s ON b.sitter_id = s.user_id
        LEFT JOIN payments pm ON b.id = pm.booking_id
    """;

    private Long extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder == null) return null;
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && !keys.isEmpty()) {
            Object idVal = keys.get("id");
            if (idVal == null) {
                idVal = keys.get("ID");
            }
            if (idVal instanceof Number num) {
                return num.longValue();
            }
        }
        try {
            Number key = keyHolder.getKey();
            if (key != null) {
                return key.longValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Inserts a new SittingBooking row and returns its generated ID.
     */
    public Long insertBooking(SittingBooking booking) {
        String sql = """
            INSERT INTO sitter_bookings (pet_id, sitter_id, owner_id, start_time, end_time, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, booking.getPetId());
            ps.setLong(2, booking.getSitterId());
            ps.setLong(3, booking.getOwnerId());
            ps.setTimestamp(4, Timestamp.valueOf(booking.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(booking.getEndTime()));
            ps.setString(6, booking.getStatus() != null ? booking.getStatus() : "CONFIRMED");
            ps.setTimestamp(7, Timestamp.valueOf(booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Long key = extractGeneratedId(keyHolder);
        if (key != null) {
            booking.setId(key);
            return key;
        }
        return null;
    }

    /**
     * Inserts an escrow payment record linked to a booking (P7 integration).
     */
    public Long insertPayment(Long bookingId, BigDecimal amount, String method, String status) {
        String sql = """
            INSERT INTO payments (booking_id, amount, method, status, created_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method != null ? method : "CARD");
            ps.setString(4, status != null ? status : "HELD");
            return ps;
        }, keyHolder);

        return extractGeneratedId(keyHolder);
    }

    /**
     * Inserts a check-in note into checkins table and returns its generated ID.
     */
    public Long insertCheckin(CheckIn checkin) {
        String sql = "INSERT INTO checkins (booking_id, timestamp, note) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, checkin.getBookingId());
            ps.setTimestamp(2, Timestamp.valueOf(checkin.getTimestamp() != null ? checkin.getTimestamp() : LocalDateTime.now()));
            ps.setString(3, checkin.getNote());
            return ps;
        }, keyHolder);

        Long key = extractGeneratedId(keyHolder);
        if (key != null) {
            checkin.setId(key);
            return key;
        }
        return null;
    }

    /**
     * Finds a booking by ID.
     */
    public Optional<SittingBooking> findById(Long bookingId) {
        if (bookingId == null) return Optional.empty();
        String sql = BASE_SELECT + " WHERE b.id = ?";
        List<SittingBooking> list = jdbcTemplate.query(sql, bookingRowMapper, bookingId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Finds bookings created by a specific pet owner.
     */
    public List<SittingBooking> findByOwnerId(Long ownerId) {
        String sql = BASE_SELECT + " WHERE b.owner_id = ? ORDER BY b.start_time DESC";
        return jdbcTemplate.query(sql, bookingRowMapper, ownerId);
    }

    /**
     * Finds bookings assigned to a specific pet sitter.
     */
    public List<SittingBooking> findBySitterId(Long sitterId) {
        String sql = BASE_SELECT + " WHERE b.sitter_id = ? ORDER BY b.start_time DESC";
        return jdbcTemplate.query(sql, bookingRowMapper, sitterId);
    }

    /**
     * Finds all bookings across the platform.
     */
    public List<SittingBooking> findAll() {
        String sql = BASE_SELECT + " ORDER BY b.start_time DESC";
        return jdbcTemplate.query(sql, bookingRowMapper);
    }

    /**
     * Retrieves all check-in notes for a booking, sorted chronologically descending.
     */
    public List<CheckIn> findCheckinsByBookingId(Long bookingId) {
        String sql = "SELECT id, booking_id, timestamp, note FROM checkins WHERE booking_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, checkInRowMapper, bookingId);
    }

    /**
     * Checks if a sitter has an existing active booking overlapping with the given start and end time.
     * Condition: start_time < requestedEnd AND end_time > requestedStart
     */
    public boolean hasConflict(Long sitterId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT COUNT(*) FROM sitter_bookings
            WHERE sitter_id = ? AND status != 'CANCELLED'
              AND start_time < ? AND end_time > ?
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                sitterId, Timestamp.valueOf(end), Timestamp.valueOf(start));
        return count != null && count > 0;
    }

    /**
     * Updates booking status ('CONFIRMED', 'CANCELLED', 'COMPLETED').
     */
    public boolean updateStatus(Long bookingId, String status) {
        String sql = "UPDATE sitter_bookings SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, bookingId) > 0;
    }

    /**
     * Updates booking end_time (e.g. when session is completed early, releasing remaining slot).
     */
    public boolean updateEndTime(Long bookingId, LocalDateTime endTime) {
        String sql = "UPDATE sitter_bookings SET end_time = ? WHERE id = ?";
        return jdbcTemplate.update(sql, Timestamp.valueOf(endTime), bookingId) > 0;
    }
}
