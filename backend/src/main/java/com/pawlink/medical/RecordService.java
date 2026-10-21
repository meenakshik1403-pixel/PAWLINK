package com.pawlink.medical;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service orchestrating clinical medical consultations and vaccinations.
 * Integrates directly with P5 reminders table when vaccinations with next due dates are added.
 */
@Service
public class RecordService {

    private final MedicalRecordDAO medicalRecordDAO;
    private final VaccinationDAO vaccinationDAO;
    private final JdbcTemplate jdbcTemplate;

    public RecordService(MedicalRecordDAO medicalRecordDAO,
                         VaccinationDAO vaccinationDAO,
                         JdbcTemplate jdbcTemplate) {
        this.medicalRecordDAO = medicalRecordDAO;
        this.vaccinationDAO = vaccinationDAO;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Adds a clinical medical examination record for a pet.
     *
     * @param record incoming medical consultation details
     * @param vetId  attending veterinarian user ID
     * @return persisted MedicalRecord with generated ID and vet details
     */
    @Transactional
    public MedicalRecord addMedicalRecord(MedicalRecord record, Long vetId) {
        if (record == null) {
            throw new IllegalArgumentException("Medical record cannot be null.");
        }
        if (record.getPetId() == null || record.getPetId() <= 0) {
            throw new IllegalArgumentException("Valid Pet ID is required.");
        }
        if (record.getDiagnosis() == null || record.getDiagnosis().trim().isEmpty()) {
            throw new IllegalArgumentException("Clinical diagnosis cannot be blank.");
        }
        if (record.getPrescription() == null || record.getPrescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Prescription cannot be blank.");
        }

        Long effectiveVetId = record.getVetId() != null ? record.getVetId() : vetId;
        if (effectiveVetId == null || effectiveVetId <= 0) {
            throw new IllegalArgumentException("Treating Veterinarian ID is required.");
        }

        record.setVetId(effectiveVetId);
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }

        medicalRecordDAO.insert(record);

        // Fetch back with populated vet name from JOIN query
        return medicalRecordDAO.findById(record.getId()).orElse(record);
    }

    /**
     * Adds an administered vaccination record and automatically registers
     * an automated reminder in the P5 reminders table.
     *
     * @param vac vaccination record
     * @return persisted VaccinationRecord
     */
    @Transactional
    public VaccinationRecord addVaccination(VaccinationRecord vac) {
        if (vac == null) {
            throw new IllegalArgumentException("Vaccination record cannot be null.");
        }
        if (vac.getPetId() == null || vac.getPetId() <= 0) {
            throw new IllegalArgumentException("Valid Pet ID is required.");
        }
        if (vac.getVaccineName() == null || vac.getVaccineName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vaccine name cannot be blank.");
        }
        if (vac.getNextDueDate() == null) {
            throw new IllegalArgumentException("Vaccination next due date is required.");
        }

        if (vac.getGivenDate() == null) {
            vac.setGivenDate(LocalDate.now());
        }

        vaccinationDAO.insert(vac);

        // Inter-module integration with P5 Reminders:
        // Automatically insert a reminder row in the reminders table if not already scheduled
        final String checkSql = "SELECT COUNT(*) FROM reminders WHERE pet_id = ? AND type = 'VACCINATION' AND due_date = ?";
        Integer existing = jdbcTemplate.queryForObject(checkSql, Integer.class, vac.getPetId(), Date.valueOf(vac.getNextDueDate()));
        if (existing == null || existing == 0) {
            final String reminderSql = "INSERT INTO reminders (pet_id, type, due_date, sent) VALUES (?, 'VACCINATION', ?, false)";
            jdbcTemplate.update(reminderSql, vac.getPetId(), Date.valueOf(vac.getNextDueDate()));
        }

        return vac;
    }

    /**
     * Retrieves the combined chronological medical timeline for a pet,
     * including clinical diagnoses and vaccinations sorted by date descending.
     *
     * @param petId pet ID
     * @return unified timeline list
     */
    public List<PetMedicalTimelineDTO> getPetHistory(Long petId) {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Valid Pet ID is required.");
        }

        List<MedicalRecord> medicals = medicalRecordDAO.findByPetId(petId);
        List<VaccinationRecord> vaccinations = vaccinationDAO.findByPetId(petId);

        List<PetMedicalTimelineDTO> timeline = new ArrayList<>();
        for (MedicalRecord m : medicals) {
            timeline.add(PetMedicalTimelineDTO.fromMedical(m));
        }
        for (VaccinationRecord v : vaccinations) {
            timeline.add(PetMedicalTimelineDTO.fromVaccination(v));
        }

        // Sort descending by event date, then by ID
        timeline.sort((a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            int dateCmp = b.getDate().compareTo(a.getDate());
            if (dateCmp != 0) return dateCmp;
            if (a.getId() == null && b.getId() == null) return 0;
            if (a.getId() == null) return 1;
            if (b.getId() == null) return -1;
            return b.getId().compareTo(a.getId());
        });

        return timeline;
    }

    /**
     * Retrieves all clinical medical records for a pet.
     */
    public List<MedicalRecord> getMedicalRecords(Long petId) {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Valid Pet ID is required.");
        }
        return medicalRecordDAO.findByPetId(petId);
    }

    /**
     * Retrieves all vaccinations for a pet.
     */
    public List<VaccinationRecord> getVaccinations(Long petId) {
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Valid Pet ID is required.");
        }
        return vaccinationDAO.findByPetId(petId);
    }
}
