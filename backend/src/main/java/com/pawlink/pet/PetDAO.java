package com.pawlink.pet;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Pet operations using Spring JdbcTemplate.
 * Strictly adheres to technical constraints:
 * - NO JPA/Hibernate annotations (@Entity, @Table, @Id).
 * - Plain SQL with RowMapper and parameterized queries.
 */
@Repository
public class PetDAO {

    private final JdbcTemplate jdbcTemplate;

    public PetDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper translating a JDBC ResultSet row into a concrete Pet subclass
     * via the PetFactory.
     */
    private final RowMapper<Pet> petRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        Long ownerId = rs.getLong("owner_id");
        String species = rs.getString("species");
        String name = rs.getString("name");
        Date sqlDate = rs.getDate("dob");
        LocalDate dob = sqlDate != null ? sqlDate.toLocalDate() : null;

        return PetFactory.createExistingPet(id, species, name, dob, ownerId);
    };

    /**
     * Inserts a new pet record and returns the generated primary key ID.
     *
     * @param pet the Pet domain object to save
     * @return generated pet ID
     */
    public Long insert(Pet pet) {
        final String sql = "INSERT INTO pets (owner_id, species, name, dob) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setLong(1, pet.getOwnerId());
            ps.setString(2, pet.getSpecies());
            ps.setString(3, pet.getName());
            ps.setDate(4, Date.valueOf(pet.getDob()));
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
            throw new IllegalStateException("Failed to retrieve generated pet ID from database.");
        }

        pet.setPetId(generatedId);
        return generatedId;
    }

    /**
     * Finds a pet by primary key ID.
     *
     * @param petId the pet ID
     * @return Optional containing Pet if found, empty otherwise
     */
    public Optional<Pet> findById(Long petId) {
        final String sql = "SELECT id, owner_id, species, name, dob FROM pets WHERE id = ?";
        List<Pet> results = jdbcTemplate.query(sql, petRowMapper, petId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Retrieves all pets belonging to a specific owner.
     * Parameterized query: SELECT * FROM pets WHERE owner_id = ?
     *
     * @param ownerId user ID of the pet owner
     * @return list of pets owned by the specified user
     */
    public List<Pet> findByOwnerId(Long ownerId) {
        final String sql = "SELECT id, owner_id, species, name, dob FROM pets WHERE owner_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, petRowMapper, ownerId);
    }

    /**
     * Searches pets by pet name using SQL ILIKE (case-insensitive search),
     * or by owner ID if query is numeric.
     *
     * @param query search keyword
     * @return list of matching pets
     */
    public List<Pet> searchByNameOrOwner(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }

        String trimmed = query.trim();
        String pattern = "%" + trimmed + "%";

        Long ownerIdParam = null;
        try {
            ownerIdParam = Long.parseLong(trimmed);
        } catch (NumberFormatException ignored) {
            // Not a numeric query
        }

        if (ownerIdParam != null) {
            final String sql = "SELECT id, owner_id, species, name, dob FROM pets " +
                               "WHERE name ILIKE ? OR owner_id = ? ORDER BY id ASC";
            return jdbcTemplate.query(sql, petRowMapper, pattern, ownerIdParam);
        } else {
            final String sql = "SELECT id, owner_id, species, name, dob FROM pets " +
                               "WHERE name ILIKE ? ORDER BY id ASC";
            return jdbcTemplate.query(sql, petRowMapper, pattern);
        }
    }

    /**
     * Retrieves all pets in the system.
     *
     * @return list of all registered pets
     */
    public List<Pet> findAll() {
        final String sql = "SELECT id, owner_id, species, name, dob FROM pets ORDER BY id ASC";
        return jdbcTemplate.query(sql, petRowMapper);
    }
}
