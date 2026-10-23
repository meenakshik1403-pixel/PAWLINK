package com.pawlink.vet.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

/**
 * Data Transfer Object representing Pet records in the Swing client.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PetDTO {

    private Long id;
    private String name;
    private String species;

    @JsonFormat(pattern = "yyyy-MM-dd")
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

    public Long getPetId() {
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

    @Override
    public String toString() {
        return name + " (" + species + ")";
    }
}
