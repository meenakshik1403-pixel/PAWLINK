package com.pawlink.pet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pawlink.pet.Pet;

import java.time.LocalDate;

/**
 * REST API response representation of a Pet, including polymorphic care guidelines.
 */
public class PetResponse {

    private Long id;
    private String name;
    private String species;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private Long ownerId;
    private String careGuidelines;

    public PetResponse() {
    }

    public PetResponse(Long id, String name, String species, LocalDate dob, Long ownerId, String careGuidelines) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.dob = dob;
        this.ownerId = ownerId;
        this.careGuidelines = careGuidelines;
    }

    public static PetResponse from(Pet pet) {
        if (pet == null) return null;
        return new PetResponse(
                pet.getPetId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getDob(),
                pet.getOwnerId(),
                pet.getCareGuidelines()
        );
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
}
