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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Pet Vaccinations using Spring JdbcTemplate.
 * Strictly adheres to technical constraints:
 * - NO JPA/Hibernate annotations (@Entity, @Table, @Id).
 * - Plain SQL with RowMapper and parameterized queries.
 */
@Repository
public class VaccinationDAO {

    private final JdbcTemplate jdbcTemplate;

    public VaccinationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<VaccinationRecord> vaccinationRowMapper = (rs, rowNum) -> {
        VaccinationRecord vac = new VaccinationRecord();
        vac.setId(rs.getLong("id"));
        vac.setPetId(rs.getLong("pet_id"));
        vac.setVaccineName(rs.getString("vaccine_name"));

        Date gDate = rs.getDate("given_date");
        vac.setGivenDate(gDate != null ? gDate.toLocalDate() : null);

        Date nDate = rs.getDate("next_due_date");
        vac.setNextDueDate(nDate != null ? nDate.toLocalDate() : null);

        Timestamp ts = rs.getTimestamp("created_at");
        vac.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);

        return vac;
    };

    /**
     * Inserts a new vaccination record and returns generated ID.
     *
     * @param vac vaccination record to save
     * @return generated ID
     */
    public Long insert(VaccinationRecord vac) {
        final String sql = "INSERT INTO vaccinations (pet_id, vaccine_name, given_date, next_due_date) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDate given = vac.getGivenDate() != null ? vac.getGivenDate() : LocalDate.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, vac.getPetId());
            ps.setString(2, vac.getVaccineName());
            ps.setDate(3, Date.valueOf(given));
            ps.setDate(4, Date.valueOf(vac.getNextDueDate()));
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
            throw new IllegalStateException("Failed to retrieve generated vaccination record ID.");
        }

        vac.setId(generatedId);
        vac.setGivenDate(given);
        return generatedId;
    }

    /**
     * Retrieves all vaccinations for a given pet.
     *
     * @param petId pet ID
     * @return list of vaccinations ordered by given_date DESC, id DESC
     */
    public List<VaccinationRecord> findByPetId(Long petId) {
        final String sql = "SELECT id, pet_id, vaccine_name, given_date, next_due_date, created_at " +
                "FROM vaccinations " +
                "WHERE pet_id = ? " +
                "ORDER BY given_date DESC, id DESC";

        return jdbcTemplate.query(sql, vaccinationRowMapper, petId);
    }

    /**
     * Finds a vaccination record by ID.
     *
     * @param id vaccination ID
     * @return Optional containing the record if found
     */
    public Optional<VaccinationRecord> findById(Long id) {
        final String sql = "SELECT id, pet_id, vaccine_name, given_date, next_due_date, created_at " +
                "FROM vaccinations WHERE id = ?";

        List<VaccinationRecord> list = jdbcTemplate.query(sql, vaccinationRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
