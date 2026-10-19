package com.pawlink.pet;

import java.time.LocalDate;

/**
 * Factory class responsible for instantiating Pet polymorphic subclasses
 * based on the species identifier.
 *
 * Implements strict domain validation and throws {@link InvalidPetDataException}
 * if required fields are missing/blank or if the species is unsupported.
 */
public class PetFactory {

    private PetFactory() {
        // Utility factory class
    }

    /**
     * Creates a new Pet subclass instance for registration.
     *
     * @param species Supported species: "Dog", "Cat", "Bird", "Reptile" (case-insensitive)
     * @param name    Name of the pet (non-blank)
     * @param dob     Date of birth (non-null)
     * @param ownerId Associated pet owner user ID (non-null)
     * @return Specific Pet subclass instance
     * @throws InvalidPetDataException if validation fails or species is unsupported
     */
    public static Pet createPet(String species, String name, LocalDate dob, Long ownerId) {
        validateInputs(species, name, dob, ownerId);
        return instantiateSubclass(null, species.trim(), name.trim(), dob, ownerId);
    }

    /**
     * Reconstructs an existing Pet subclass instance loaded from the database.
     *
     * @param petId   Existing database primary key ID
     * @param species Supported species
     * @param name    Name of the pet
     * @param dob     Date of birth
     * @param ownerId Associated pet owner user ID
     * @return Specific Pet subclass instance
     */
    public static Pet createExistingPet(Long petId, String species, String name, LocalDate dob, Long ownerId) {
        validateInputs(species, name, dob, ownerId);
        return instantiateSubclass(petId, species.trim(), name.trim(), dob, ownerId);
    }

    private static void validateInputs(String species, String name, LocalDate dob, Long ownerId) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidPetDataException("Pet name cannot be blank.");
        }
        if (species == null || species.trim().isEmpty()) {
            throw new InvalidPetDataException("Pet species cannot be blank.");
        }
        if (dob == null) {
            throw new InvalidPetDataException("Date of birth is required.");
        }
        if (ownerId == null || ownerId <= 0) {
            throw new InvalidPetDataException("Valid Owner ID is required.");
        }
    }

    private static Pet instantiateSubclass(Long petId, String species, String name, LocalDate dob, Long ownerId) {
        String normalizedSpecies = species.trim().toUpperCase();
        return switch (normalizedSpecies) {
            case "DOG" -> new Dog(petId, name, dob, ownerId);
            case "CAT" -> new Cat(petId, name, dob, ownerId);
            case "BIRD" -> new Bird(petId, name, dob, ownerId);
            case "REPTILE" -> new Reptile(petId, name, dob, ownerId);
            default -> throw new InvalidPetDataException(
                    "Unsupported species: '" + species + "'. Supported species are Dog, Cat, Bird, and Reptile."
            );
        };
    }
}
