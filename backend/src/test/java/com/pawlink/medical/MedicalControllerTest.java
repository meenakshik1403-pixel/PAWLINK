package com.pawlink.medical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.pet.PetDAO;
import com.pawlink.pet.PetFactory;
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
public class MedicalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long vetId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reminders");
        jdbcTemplate.update("DELETE FROM vaccinations");
        jdbcTemplate.update("DELETE FROM medical_records");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var vet = UserFactory.createUser("VET", null, "Dr. Sreekumar", "vet.sreekumar@pawlink.com", "555-0101", "hash", LocalDateTime.now());
        userDAO.insert(vet);
        vetId = vet.getUserId();

        var owner = UserFactory.createUser("OWNER", null, "John Vargese", "john@pawlink.com", "555-0202", "hash", LocalDateTime.now());
        userDAO.insert(owner);

        var pet = PetFactory.createPet("Cat", "Jimmy", LocalDate.of(2022, 1, 1), owner.getUserId());
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("POST /api/pets/{id}/medical records a visit and returns 201 Created")
    void testAddMedicalVisit() throws Exception {
        MedicalRecord record = new MedicalRecord(null, petId, vetId, null, LocalDate.now(), "Annual checkup", "Flea prevention");

        mockMvc.perform(post("/api/pets/" + petId + "/medical")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", vetId)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.diagnosis", is("Annual checkup")))
                .andExpect(jsonPath("$.prescription", is("Flea prevention")))
                .andExpect(jsonPath("$.vetName", is("Dr. Sreekumar")));
    }

    @Test
    @DisplayName("POST /api/pets/{id}/vaccinations records vaccine and returns 201 Created")
    void testAddVaccine() throws Exception {
        VaccinationRecord vac = new VaccinationRecord(null, petId, "FVRCP", LocalDate.now(), LocalDate.now().plusYears(1));

        mockMvc.perform(post("/api/pets/" + petId + "/vaccinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", vetId)
                        .content(objectMapper.writeValueAsString(vac)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.vaccineName", is("FVRCP")));
    }

    @Test
    @DisplayName("GET /api/pets/{id}/records returns unified chronological history")
    void testGetPetRecordsTimeline() throws Exception {
        // Add one medical visit
        MedicalRecord med = new MedicalRecord(null, petId, vetId, null, LocalDate.now().minusDays(5), "Gastroenteritis", "Probiotics");
        mockMvc.perform(post("/api/pets/" + petId + "/medical")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", vetId)
                .content(objectMapper.writeValueAsString(med)))
                .andExpect(status().isCreated());

        // Add one vaccination
        VaccinationRecord vac = new VaccinationRecord(null, petId, "Rabies", LocalDate.now(), LocalDate.now().plusYears(1));
        mockMvc.perform(post("/api/pets/" + petId + "/vaccinations")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", vetId)
                .content(objectMapper.writeValueAsString(vac)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pets/" + petId + "/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("VACCINATION")))
                .andExpect(jsonPath("$[0].title", is("Rabies")))
                .andExpect(jsonPath("$[1].type", is("MEDICAL")))
                .andExpect(jsonPath("$[1].title", is("Gastroenteritis")))
                .andExpect(jsonPath("$[1].vetName", is("Dr. Sreekumar")));
    }
}
