package com.pawlink.vet.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

/**
 * Unified timeline DTO for displaying medical and vaccination events in the Swing JTable.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PetMedicalTimelineDTO {

    private String type; // "MEDICAL" or "VACCINATION"
    private Long id;
    private Long petId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String title;
    private String details;
    private String vetName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDueDate;

    public PetMedicalTimelineDTO() {
    }

    public PetMedicalTimelineDTO(String type, Long id, Long petId, LocalDate date, String title, String details, String vetName, LocalDate nextDueDate) {
        this.type = type;
        this.id = id;
        this.petId = petId;
        this.date = date;
        this.title = title;
        this.details = details;
        this.vetName = vetName;
        this.nextDueDate = nextDueDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getVetName() {
        return vetName;
    }

    public void setVetName(String vetName) {
        this.vetName = vetName;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }
}
