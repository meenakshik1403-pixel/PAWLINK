package com.pawlink.pet;

import java.time.LocalDate;

/**
 * Concrete Pet subclass representing Birds.
 */
public class Bird extends Pet {

    public Bird() {
        super();
        setSpecies("Bird");
    }

    public Bird(Long petId, String name, LocalDate dob, Long ownerId) {
        super(petId, name, dob, ownerId, "Bird");
    }

    @Override
    public String getCareGuidelines() {
        return "Spacious cage, wing care, temperature monitoring.";
    }
}
