package com.pawlink.pet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Request payload for registering or updating a Pet.
 */
public class PetRequest {

    private String name;
    private String species;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private Long ownerId;

    public PetRequest() {
    }

    public PetRequest(String name, String species, LocalDate dob, Long ownerId) {
        this.name = name;
        this.species = species;
        this.dob = dob;
        this.ownerId = ownerId;
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
}
