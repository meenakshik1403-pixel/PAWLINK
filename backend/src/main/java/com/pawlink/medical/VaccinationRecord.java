package com.pawlink.medical;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model representing an administered pet vaccination record.
 * Strictly NO JPA / Hibernate annotations. Uses Spring JdbcTemplate.
 */
public class VaccinationRecord {

    private Long id;
    private Long petId;
    private String vaccineName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate givenDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueDate;

    private LocalDateTime createdAt;

    public VaccinationRecord() {
    }

    public VaccinationRecord(Long id, Long petId, String vaccineName, LocalDate givenDate, LocalDate nextDueDate) {
        this.id = id;
        this.petId = petId;
        this.vaccineName = vaccineName;
        this.givenDate = givenDate;
        this.nextDueDate = nextDueDate;
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

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public LocalDate getGivenDate() {
        return givenDate;
    }

    public void setGivenDate(LocalDate givenDate) {
        this.givenDate = givenDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "VaccinationRecord{" +
                "id=" + id +
                ", petId=" + petId +
                ", vaccineName='" + vaccineName + '\'' +
                ", givenDate=" + givenDate +
                ", nextDueDate=" + nextDueDate +
                '}';
    }
}
