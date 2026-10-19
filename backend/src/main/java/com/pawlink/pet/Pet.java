package com.pawlink.pet;

import com.pawlink.medical.MedicalRecord;
import com.pawlink.medical.VaccinationRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract domain model for Pets in PawLink.
 * Adheres to strict technical constraint: NO JPA/Hibernate annotations.
 * Uses plain SQL and RowMapper via Spring JdbcTemplate.
 * Demonstrates Object Composition: Pet has multiple MedicalRecords and VaccinationRecords.
 */
public abstract class Pet {

    private Long petId;
    private String name;
    private LocalDate dob;
    private Long ownerId;
    private String species;

    // Composition: Pet has-a collection of MedicalRecords and VaccinationRecords
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
    private List<VaccinationRecord> vaccinations = new ArrayList<>();

    public Pet() {
    }

    public Pet(Long petId, String name, LocalDate dob, Long ownerId, String species) {
        this.petId = petId;
        this.name = name;
        this.dob = dob;
        this.ownerId = ownerId;
        this.species = species;
    }

    /**
     * Polymorphic method overridden by each concrete pet species subclass.
     * Returns clinical care and wellness guidelines specific to the species.
     */
    public abstract String getCareGuidelines();

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    // Alias for getPetId / setPetId
    public Long getId() {
        return petId;
    }

    public void setId(Long id) {
        this.petId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }

    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords != null ? medicalRecords : new ArrayList<>();
    }

    public void addMedicalRecord(MedicalRecord record) {
        if (record != null) {
            this.medicalRecords.add(record);
        }
    }

    public List<VaccinationRecord> getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(List<VaccinationRecord> vaccinations) {
        this.vaccinations = vaccinations != null ? vaccinations : new ArrayList<>();
    }

    public void addVaccination(VaccinationRecord vac) {
        if (vac != null) {
            this.vaccinations.add(vac);
        }
    }

    @Override
    public String toString() {
        return "Pet{" +
                "petId=" + petId +
                ", name='" + name + '\'' +
                ", species='" + species + '\'' +
                ", dob=" + dob +
                ", ownerId=" + ownerId +
                '}';
    }
}
