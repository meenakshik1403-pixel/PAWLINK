package com.pawlink.pet;

import java.time.LocalDate;

/**
 * Concrete Pet subclass representing Dogs.
 */
public class Dog extends Pet {

    public Dog() {
        super();
        setSpecies("Dog");
    }

    public Dog(Long petId, String name, LocalDate dob, Long ownerId) {
        super(petId, name, dob, ownerId, "Dog");
    }

    @Override
    public String getCareGuidelines() {
        return "Daily exercise, annual rabies vaccine, dental checkups.";
    }
}
