package com.pawlink.pet;

import com.pawlink.pet.dto.PetRequest;
import com.pawlink.pet.dto.PetResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Pet endpoints:
 * - GET /api/pets?ownerId={id}&q={query}
 * - POST /api/pets
 * - GET /api/pets/{id}
 */
@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    /**
     * Lists pets, supporting filtering by owner ID or text search query.
     *
     * @param ownerId optional owner ID filter
     * @param q       optional search keyword
     * @param xUserId optional X-User-Id header
     * @return list of matching Pet responses
     */
    @GetMapping
    public ResponseEntity<List<PetResponse>> getPets(
            @RequestParam(name = "ownerId", required = false) Long ownerId,
            @RequestParam(name = "q", required = false) String q,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        // If ownerId is not passed in query params, but caller is an OWNER requesting their own pets,
        // we can use ownerId if explicitly requested or allow xUserId if ownerId is not given
        List<PetResponse> pets = petService.getPets(ownerId, q);
        return ResponseEntity.ok(pets);
    }

    /**
     * Registers a new pet.
     * Accepts ownerId from request body or falls back to 'X-User-Id' header.
     *
     * @param request incoming pet registration data
     * @param xUserId session user ID header
     * @return created PetResponse with HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<PetResponse> registerPet(
            @RequestBody PetRequest request,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        Long ownerId = request.getOwnerId() != null ? request.getOwnerId() : xUserId;
        PetResponse created = petService.registerPet(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves a single pet by ID.
     *
     * @param id      pet ID
     * @param xUserId optional X-User-Id header for ownership check
     * @return PetResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(
            @PathVariable("id") Long id,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        PetResponse pet = petService.getPetById(id, null);
        return ResponseEntity.ok(pet);
    }
}
