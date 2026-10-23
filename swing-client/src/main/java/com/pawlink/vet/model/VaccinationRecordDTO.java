package com.pawlink.vet.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

/**
 * DTO representing vaccination records in the Swing client.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaccinationRecordDTO {

    private Long id;
    private Long petId;
    private String vaccineName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate givenDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueDate;

    public VaccinationRecordDTO() {
    }

    public VaccinationRecordDTO(Long id, Long petId, String vaccineName, LocalDate givenDate, LocalDate nextDueDate) {
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
}
