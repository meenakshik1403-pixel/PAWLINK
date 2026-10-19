package com.pawlink.pet;

import java.time.LocalDate;

/**
 * Concrete Pet subclass representing Reptiles.
 */
public class Reptile extends Pet {

    public Reptile() {
        super();
        setSpecies("Reptile");
    }

    public Reptile(Long petId, String name, LocalDate dob, Long ownerId) {
        super(petId, name, dob, ownerId, "Reptile");
    }

    @Override
    public String getCareGuidelines() {
        return "UVB lighting, humidity control, heat lamp regulation.";
    }
}
