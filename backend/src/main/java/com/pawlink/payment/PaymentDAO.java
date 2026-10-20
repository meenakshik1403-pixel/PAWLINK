package com.pawlink.payment;

import com.pawlink.payment.dto.PaymentResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Payments using Spring JdbcTemplate and plain SQL.
 * STRICT: No JPA/Hibernate.
 */
@Repository
public class PaymentDAO {

    private final JdbcTemplate jdbcTemplate;

    public PaymentDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> paymentRowMapper = new RowMapper<>() {
        @Override
        public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Long bookingId = rs.getLong("booking_id");
            BigDecimal amount = rs.getBigDecimal("amount");
            String method = rs.getString("method");
            String status = rs.getString("status");
            Timestamp ts = rs.getTimestamp("created_at");
            LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

            return PaymentFactory.createPayment(id, bookingId, amount, method, status, createdAt);
        }
    };

    private final RowMapper<PaymentResponse> paymentResponseRowMapper = new RowMapper<>() {
        @Override
        public PaymentResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            PaymentResponse resp = new PaymentResponse();
            resp.setId(rs.getLong("id"));
            resp.setBookingId(rs.getLong("booking_id"));
            resp.setAmount(rs.getBigDecimal("amount"));
            resp.setMethod(rs.getString("method"));
            resp.setStatus(rs.getString("status"));

            Timestamp ts = rs.getTimestamp("created_at");
            resp.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

            resp.setOwnerId(rs.getLong("owner_id"));
            resp.setPetName(rs.getString("pet_name"));
            resp.setSitterName(rs.getString("sitter_name"));

            Timestamp startTs = rs.getTimestamp("start_time");
            resp.setStartTime(startTs != null ? startTs.toLocalDateTime() : null);

            Timestamp endTs = rs.getTimestamp("end_time");
            resp.setEndTime(endTs != null ? endTs.toLocalDateTime() : null);

            return resp;
        }
    };

    /**
     * Inserts a payment record linked to a sitting booking.
     */
    public Long insert(Payment payment, Long bookingId) {
        String sql = """
            INSERT INTO payments (booking_id, amount, method, status, created_at)
            VALUES (?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, bookingId);
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getMethod());
            ps.setString(4, payment.getStatus() != null ? payment.getStatus() : "HELD");
            ps.setTimestamp(5, Timestamp.valueOf(payment.getCreatedAt() != null ? payment.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Long key = extractGeneratedId(keyHolder);
        if (key != null) {
            payment.setId(key);
            payment.setBookingId(bookingId);
            return key;
        }
        return null;
    }

    /**
     * Finds payments made by a specific pet owner with joined booking, pet, and sitter information.
     */
    public List<PaymentResponse> findByOwnerId(Long ownerId) {
        String sql = """
            SELECT p.id, p.booking_id, p.amount, p.method, p.status, p.created_at,
                   b.owner_id, pt.name AS pet_name, u.name AS sitter_name,
                   b.start_time, b.end_time
            FROM payments p
            JOIN sitter_bookings b ON p.booking_id = b.id
            JOIN pets pt ON b.pet_id = pt.id
            JOIN users u ON b.sitter_id = u.id
            WHERE b.owner_id = ?
            ORDER BY p.created_at DESC
        """;
        return jdbcTemplate.query(sql, paymentResponseRowMapper, ownerId);
    }

    /**
     * Retrieves all payments on the platform with full joins.
     */
    public List<PaymentResponse> findAllWithDetails() {
        String sql = """
            SELECT p.id, p.booking_id, p.amount, p.method, p.status, p.created_at,
                   b.owner_id, pt.name AS pet_name, u.name AS sitter_name,
                   b.start_time, b.end_time
            FROM payments p
            JOIN sitter_bookings b ON p.booking_id = b.id
            JOIN pets pt ON b.pet_id = pt.id
            JOIN users u ON b.sitter_id = u.id
            ORDER BY p.created_at DESC
        """;
        return jdbcTemplate.query(sql, paymentResponseRowMapper);
    }

    /**
     * Finds a payment by its primary key ID.
     */
    public Optional<Payment> findById(Long id) {
        String sql = "SELECT id, booking_id, amount, method, status, created_at FROM payments WHERE id = ?";
        List<Payment> list = jdbcTemplate.query(sql, paymentRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Finds a payment by its associated booking ID.
     */
    public Optional<Payment> findByBookingId(Long bookingId) {
        String sql = "SELECT id, booking_id, amount, method, status, created_at FROM payments WHERE booking_id = ?";
        List<Payment> list = jdbcTemplate.query(sql, paymentRowMapper, bookingId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Updates payment status ('RELEASED', 'REFUNDED', etc.).
     */
    public boolean updateStatus(Long paymentId, String status) {
        String sql = "UPDATE payments SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, paymentId) > 0;
    }

    /**
     * Updates payment status for a given booking ID.
     */
    public boolean updateStatusByBookingId(Long bookingId, String status) {
        String sql = "UPDATE payments SET status = ? WHERE booking_id = ?";
        return jdbcTemplate.update(sql, status, bookingId) > 0;
    }

    private Long extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder == null) return null;
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) {
                idVal = keyHolder.getKeys().get("ID");
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
}

