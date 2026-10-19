package com.pawlink.medical;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Medical Records using Spring JdbcTemplate.
 * Strictly adheres to technical constraints:
 * - NO JPA/Hibernate annotations (@Entity, @Table, @Id).
 * - Plain SQL with RowMapper and parameterized queries.
 * - Multi-table SQL JOIN with users to retrieve the treating veterinarian's name.
 */
@Repository
public class MedicalRecordDAO {

    private final JdbcTemplate jdbcTemplate;

    public MedicalRecordDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MedicalRecord> medicalRecordRowMapper = (rs, rowNum) -> {
        MedicalRecord record = new MedicalRecord();
        record.setId(rs.getLong("id"));
        record.setPetId(rs.getLong("pet_id"));
        record.setVetId(rs.getLong("vet_id"));
        record.setVetName(rs.getString("vet_name"));

        Date recDate = rs.getDate("record_date");
        record.setRecordDate(recDate != null ? recDate.toLocalDate() : null);

        record.setDiagnosis(rs.getString("diagnosis"));
        record.setPrescription(rs.getString("prescription"));

        Timestamp ts = rs.getTimestamp("created_at");
        record.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return record;
    };

    /**
     * Inserts a new medical consultation record and populates the generated key.
     *
     * @param record medical record to save
     * @return generated ID
     */
    public Long insert(MedicalRecord record) {
        final String sql = "INSERT INTO medical_records (pet_id, vet_id, record_date, diagnosis, prescription) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDate dateToUse = record.getRecordDate() != null ? record.getRecordDate() : LocalDate.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, record.getPetId());
            ps.setLong(2, record.getVetId());
            ps.setDate(3, Date.valueOf(dateToUse));
            ps.setString(4, record.getDiagnosis());
            ps.setString(5, record.getPrescription());
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
            throw new IllegalStateException("Failed to retrieve generated medical record ID.");
        }

        record.setId(generatedId);
        record.setRecordDate(dateToUse);
        return generatedId;
    }

    /**
     * Fetches medical records for a specific pet, joining with the users table to retrieve
     * the treating veterinarian's full name.
     *
     * @param petId pet ID
     * @return list of medical records ordered by record_date DESC, id DESC
     */
    public List<MedicalRecord> findByPetId(Long petId) {
        final String sql = "SELECT m.id, m.pet_id, m.vet_id, u.name AS vet_name, m.record_date, m.diagnosis, m.prescription, m.created_at " +
                "FROM medical_records m " +
                "LEFT JOIN users u ON m.vet_id = u.id " +
                "WHERE m.pet_id = ? " +
                "ORDER BY m.record_date DESC, m.id DESC";

        return jdbcTemplate.query(sql, medicalRecordRowMapper, petId);
    }

    /**
     * Finds a single medical record by ID.
     *
     * @param id record ID
     * @return Optional containing the record if found
     */
    public Optional<MedicalRecord> findById(Long id) {
        final String sql = "SELECT m.id, m.pet_id, m.vet_id, u.name AS vet_name, m.record_date, m.diagnosis, m.prescription, m.created_at " +
                "FROM medical_records m " +
                "LEFT JOIN users u ON m.vet_id = u.id " +
                "WHERE m.id = ?";

        List<MedicalRecord> list = jdbcTemplate.query(sql, medicalRecordRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
