package com.pawlink.pet;

import com.pawlink.pet.dto.PetRequest;
import com.pawlink.pet.dto.PetResponse;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PetServiceTest {

    @Autowired
    private PetService petService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId1;
    private Long ownerId2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var owner1 = UserFactory.createUser("OWNER", null, "John Doe", "john@example.com", "1234567890", "hash1", LocalDateTime.now());
        userDAO.insert(owner1);
        ownerId1 = owner1.getUserId();

        var owner2 = UserFactory.createUser("OWNER", null, "Jane Smith", "jane@example.com", "0987654321", "hash2", LocalDateTime.now());
        userDAO.insert(owner2);
        ownerId2 = owner2.getUserId();
    }

    @Test
    @DisplayName("Verify Polymorphic Care Guidelines for Dog, Cat, Bird, and Reptile")
    void testPolymorphicCareGuidelines() {
        LocalDate dob = LocalDate.of(2022, 1, 1);
        Pet dog = PetFactory.createPet("Dog", "Rover", dob, ownerId1);
        Pet cat = PetFactory.createPet("Cat", "Whiskers", dob, ownerId1);
        Pet bird = PetFactory.createPet("Bird", "Tweety", dob, ownerId1);
        Pet reptile = PetFactory.createPet("Reptile", "Draco", dob, ownerId1);

        assertTrue(dog instanceof Dog);
        assertEquals("Daily exercise, annual rabies vaccine, dental checkups.", dog.getCareGuidelines());

        assertTrue(cat instanceof Cat);
        assertEquals("Indoor enrichment, litter box hygiene, scratch posts.", cat.getCareGuidelines());

        assertTrue(bird instanceof Bird);
        assertEquals("Spacious cage, wing care, temperature monitoring.", bird.getCareGuidelines());

        assertTrue(reptile instanceof Reptile);
        assertEquals("UVB lighting, humidity control, heat lamp regulation.", reptile.getCareGuidelines());
    }

    @Test
    @DisplayName("Factory throws InvalidPetDataException on unknown species or blank fields")
    void testFactoryValidations() {
        LocalDate dob = LocalDate.of(2022, 1, 1);

        // Unknown species
        assertThrows(InvalidPetDataException.class, () ->
                PetFactory.createPet("Dragon", "Smaug", dob, ownerId1)
        );

        // Blank name
        assertThrows(InvalidPetDataException.class, () ->
                PetFactory.createPet("Dog", "   ", dob, ownerId1)
        );

        // Null species
        assertThrows(InvalidPetDataException.class, () ->
                PetFactory.createPet(null, "Rex", dob, ownerId1)
        );

        // Null dob
        assertThrows(InvalidPetDataException.class, () ->
                PetFactory.createPet("Dog", "Rex", null, ownerId1)
        );

        // Null ownerId
        assertThrows(InvalidPetDataException.class, () ->
                PetFactory.createPet("Dog", "Rex", dob, null)
        );
    }

    @Test
    @DisplayName("Register Pet via PetService persists and returns correct guidelines")
    void testRegisterPet() {
        PetRequest req = new PetRequest("Max", "Dog", LocalDate.of(2021, 5, 20), null);
        PetResponse res = petService.registerPet(req, ownerId1);

        assertNotNull(res.getId());
        assertEquals("Max", res.getName());
        assertEquals("Dog", res.getSpecies());
        assertEquals(ownerId1, res.getOwnerId());
        assertEquals("Daily exercise, annual rabies vaccine, dental checkups.", res.getCareGuidelines());
    }

    @Test
    @DisplayName("Ownership Validation: getPets(ownerId) returns only pets belonging to that owner")
    void testOwnershipValidation() {
        PetRequest p1 = new PetRequest("Buddy", "Dog", LocalDate.of(2020, 1, 1), null);
        PetRequest p2 = new PetRequest("Luna", "Cat", LocalDate.of(2021, 2, 2), null);
        PetRequest p3 = new PetRequest("Polly", "Bird", LocalDate.of(2022, 3, 3), null);

        petService.registerPet(p1, ownerId1);
        petService.registerPet(p2, ownerId1);
        petService.registerPet(p3, ownerId2);

        List<PetResponse> owner1Pets = petService.getPets(ownerId1, null);
        assertEquals(2, owner1Pets.size());
        assertTrue(owner1Pets.stream().allMatch(p -> p.getOwnerId().equals(ownerId1)));

        List<PetResponse> owner2Pets = petService.getPets(ownerId2, null);
        assertEquals(1, owner2Pets.size());
        assertEquals("Polly", owner2Pets.get(0).getName());
        assertEquals(ownerId2, owner2Pets.get(0).getOwnerId());
    }

    @Test
    @DisplayName("Search pets by name query using ILIKE")
    void testSearchPetsByName() {
        petService.registerPet(new PetRequest("Shadow", "Dog", LocalDate.of(2020, 1, 1), null), ownerId1);
        petService.registerPet(new PetRequest("Shimmer", "Cat", LocalDate.of(2021, 2, 2), null), ownerId1);
        petService.registerPet(new PetRequest("Rocky", "Reptile", LocalDate.of(2022, 3, 3), null), ownerId2);

        List<PetResponse> results = petService.getPets(null, "sh");
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Shadow")));
        assertTrue(results.stream().anyMatch(p -> p.getName().equals("Shimmer")));
    }
}
