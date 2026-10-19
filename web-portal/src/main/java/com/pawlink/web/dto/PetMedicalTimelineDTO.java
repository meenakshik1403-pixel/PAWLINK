package com.pawlink.web.dto;

import java.time.LocalDate;

/**
 * Timeline DTO for rendering clinical medical and vaccination history in Thymeleaf web portal.
 */
public class PetMedicalTimelineDTO {

    private String type; // "MEDICAL" or "VACCINATION"
    private Long id;
    private Long petId;
    private LocalDate date;
    private String title;
    private String details;
    private String vetName;
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

    public boolean isDueSoon() {
        if (nextDueDate == null) return false;
        LocalDate now = LocalDate.now();
        return !nextDueDate.isBefore(now) && nextDueDate.isBefore(now.plusDays(30));
    }

    public boolean isOverdue() {
        if (nextDueDate == null) return false;
        return nextDueDate.isBefore(LocalDate.now());
    }
}
