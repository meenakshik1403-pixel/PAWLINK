package com.pawlink.web.dto;

import java.time.LocalDate;
import java.time.Period;

/**
 * Pet DTO for Web Portal rendering.
 */
public class PetDTO {

    private Long id;
    private String name;
    private String species;
    private LocalDate dob;
    private Long ownerId;
    private String careGuidelines;

    public PetDTO() {
    }

    public PetDTO(Long id, String name, String species, LocalDate dob, Long ownerId, String careGuidelines) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.dob = dob;
        this.ownerId = ownerId;
        this.careGuidelines = careGuidelines;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
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

    public String getCareGuidelines() {
        return careGuidelines;
    }

    public void setCareGuidelines(String careGuidelines) {
        this.careGuidelines = careGuidelines;
    }

    public String getAgeString() {
        if (dob == null) return "Unknown";
        Period period = Period.between(dob, LocalDate.now());
        if (period.getYears() > 0) {
            return period.getYears() + " year" + (period.getYears() > 1 ? "s" : "") +
                    (period.getMonths() > 0 ? ", " + period.getMonths() + " mo" : "");
        } else if (period.getMonths() > 0) {
            return period.getMonths() + " month" + (period.getMonths() > 1 ? "s" : "");
        } else {
            return period.getDays() + " days";
        }
    }
}
