package com.pawlink.pet;

import com.pawlink.common.exception.ResourceNotFoundException;
import com.pawlink.common.exception.UnauthorizedException;
import com.pawlink.pet.dto.PetRequest;
import com.pawlink.pet.dto.PetResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service orchestrating Pet registration, retrieval, and ownership validation.
 */
@Service
public class PetService {

    private final PetDAO petDAO;

    public PetService(PetDAO petDAO) {
        this.petDAO = petDAO;
    }

    /**
     * Registers a new pet after validating inputs and creating the proper
     * polymorphic subclass through the {@link PetFactory}.
     *
     * @param req     incoming pet registration payload
     * @param ownerId owner user ID (from session/header or body)
     * @return PetResponse containing persisted pet details and care guidelines
     */
    public PetResponse registerPet(PetRequest req, Long ownerId) {
        if (req == null) {
            throw new InvalidPetDataException("Pet request body cannot be null.");
        }

        Long effectiveOwnerId = (ownerId != null) ? ownerId : req.getOwnerId();
        if (effectiveOwnerId == null || effectiveOwnerId <= 0) {
            throw new InvalidPetDataException("Pet owner ID is required for pet registration.");
        }

        // Factory validates species, non-blank fields, and instantiates the correct subclass
        Pet pet = PetFactory.createPet(
                req.getSpecies(),
                req.getName(),
                req.getDob(),
                effectiveOwnerId
        );

        petDAO.insert(pet);
        return PetResponse.from(pet);
    }

    /**
     * Retrieves pets based on owner filter or search query.
     * Enforces ownership validation: if ownerId is specified, only that owner's pets are returned.
     *
     * @param ownerId optional owner ID to filter by
     * @param query   optional search query (pet name or owner ID)
     * @return list of matching PetResponse objects
     */
    public List<PetResponse> getPets(Long ownerId, String query) {
        List<Pet> pets;

        if (ownerId != null) {
            // Strictly filter by owner ID for owner security
            List<Pet> ownerPets = petDAO.findByOwnerId(ownerId);
            if (query != null && !query.trim().isEmpty()) {
                String lowerQuery = query.trim().toLowerCase();
                pets = ownerPets.stream()
                        .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
            } else {
                pets = ownerPets;
            }
        } else {
            // Unrestricted lookup (used by clinic veterinarians searching across patients)
            if (query != null && !query.trim().isEmpty()) {
                pets = petDAO.searchByNameOrOwner(query.trim());
            } else {
                pets = petDAO.findAll();
            }
        }

        return pets.stream()
                .map(PetResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single pet by ID with optional ownership validation.
     *
     * @param petId   pet ID to look up
     * @param ownerId optional owner ID to validate ownership against
     * @return PetResponse for the pet
     */
    public PetResponse getPetById(Long petId, Long ownerId) {
        Pet pet = petDAO.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with ID: " + petId));

        if (ownerId != null && !pet.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedException("Access Denied: You do not have ownership of pet ID: " + petId);
        }

        return PetResponse.from(pet);
    }
}
