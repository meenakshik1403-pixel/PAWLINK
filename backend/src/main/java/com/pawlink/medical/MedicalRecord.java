package com.pawlink.medical;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model representing a veterinary medical clinical consultation/record.
 * Strictly NO JPA / Hibernate annotations. Uses Spring JdbcTemplate.
 */
public class MedicalRecord {

    private Long id;
    private Long petId;
    private Long vetId;
    private String vetName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    private String diagnosis;
    private String prescription;
    private LocalDateTime createdAt;

    public MedicalRecord() {
    }

    public MedicalRecord(Long id, Long petId, Long vetId, String vetName, LocalDate recordDate, String diagnosis, String prescription) {
        this.id = id;
        this.petId = petId;
        this.vetId = vetId;
        this.vetName = vetName;
        this.recordDate = recordDate;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getVetId() {
        return vetId;
    }

    public void setVetId(Long vetId) {
        this.vetId = vetId;
    }

    public String getVetName() {
        return vetName;
    }

    public void setVetName(String vetName) {
        this.vetName = vetName;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "id=" + id +
                ", petId=" + petId +
                ", vetId=" + vetId +
                ", vetName='" + vetName + '\'' +
                ", recordDate=" + recordDate +
                ", diagnosis='" + diagnosis + '\'' +
                ", prescription='" + prescription + '\'' +
                '}';
    }
}
