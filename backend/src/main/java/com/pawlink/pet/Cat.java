package com.pawlink.pet;

import java.time.LocalDate;

/**
 * Concrete Pet subclass representing Cats.
 */
public class Cat extends Pet {

    public Cat() {
        super();
        setSpecies("Cat");
    }

    public Cat(Long petId, String name, LocalDate dob, Long ownerId) {
        super(petId, name, dob, ownerId, "Cat");
    }

    @Override
    public String getCareGuidelines() {
        return "Indoor enrichment, litter box hygiene, scratch posts.";
    }
}
