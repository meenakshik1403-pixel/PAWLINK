package com.pawlink.medical;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Unified timeline DTO representing either a clinical medical visit or a vaccination event.
 * Combines two distinct relational tables into a single chronologically sortable feed.
 */
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

    public static PetMedicalTimelineDTO fromMedical(MedicalRecord record) {
        return new PetMedicalTimelineDTO(
                "MEDICAL",
                record.getId(),
                record.getPetId(),
                record.getRecordDate(),
                record.getDiagnosis(),
                record.getPrescription(),
                record.getVetName() != null ? record.getVetName() : "Treating Veterinarian",
                null
        );
    }

    public static PetMedicalTimelineDTO fromVaccination(VaccinationRecord vac) {
        return new PetMedicalTimelineDTO(
                "VACCINATION",
                vac.getId(),
                vac.getPetId(),
                vac.getGivenDate(),
                vac.getVaccineName(),
                vac.getNextDueDate() != null ? "Next dose due: " + vac.getNextDueDate() : "Completed",
                null,
                vac.getNextDueDate()
        );
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
