package com.pawlink.pet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.pet.dto.PetRequest;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var owner = UserFactory.createUser("OWNER", null, "John Doe", "john@test.com", "123456", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();
    }

    @Test
    @DisplayName("POST /api/pets returns 201 Created with care guidelines")
    void testCreatePetEndpoint() throws Exception {
        PetRequest req = new PetRequest("Buddy", "Dog", LocalDate.of(2021, 6, 15), ownerId);

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Buddy")))
                .andExpect(jsonPath("$.species", is("Dog")))
                .andExpect(jsonPath("$.careGuidelines", containsString("Daily exercise")));
    }

    @Test
    @DisplayName("POST /api/pets with invalid species returns 400 Bad Request")
    void testCreatePetInvalidSpecies() throws Exception {
        PetRequest req = new PetRequest("Spike", "AlienBeast", LocalDate.of(2021, 6, 15), ownerId);

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Unsupported species")));
    }

    @Test
    @DisplayName("GET /api/pets?ownerId={id} returns only pets of specified owner")
    void testGetPetsByOwnerId() throws Exception {
        PetRequest req = new PetRequest("Milo", "Cat", LocalDate.of(2022, 1, 10), ownerId);

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pets")
                        .param("ownerId", String.valueOf(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Milo")))
                .andExpect(jsonPath("$[0].species", is("Cat")))
                .andExpect(jsonPath("$[0].careGuidelines", containsString("Indoor enrichment")));
    }
}
