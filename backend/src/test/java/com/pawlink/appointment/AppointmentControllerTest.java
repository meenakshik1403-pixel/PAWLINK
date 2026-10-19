package com.pawlink.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.appointment.dto.AppointmentRequest;
import com.pawlink.appointment.dto.StatusUpdateRequest;
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
public class AppointmentControllerTest {

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
    private Long ownerId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM appointments");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var vet = UserFactory.createUser("VET", null, "Dr. Sarah Jenkins", "vet.sarah@pawlink.com", "555-0101", "hash", LocalDateTime.now());
        userDAO.insert(vet);
        vetId = vet.getUserId();

        var owner = UserFactory.createUser("OWNER", null, "John Doe", "john@pawlink.com", "555-0202", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();

        var pet = PetFactory.createPet("Dog", "Buddy", LocalDate.of(2021, 5, 10), ownerId);
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("POST /api/appointments successfully books slot and returns 201 Created")
    void testBookAppointmentSuccess() throws Exception {
        AppointmentRequest req = new AppointmentRequest(
                vetId,
                petId,
                ownerId,
                LocalDateTime.of(2026, 11, 10, 10, 0),
                LocalDateTime.of(2026, 11, 10, 11, 0)
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.vetName", is("Dr. Sarah Jenkins")))
                .andExpect(jsonPath("$.petName", is("Buddy")))
                .andExpect(jsonPath("$.ownerName", is("John Doe")));
    }

    @Test
    @DisplayName("POST /api/appointments returns 409 Conflict when doctor slot collides")
    void testBookAppointmentCollision() throws Exception {
        AppointmentRequest req1 = new AppointmentRequest(
                vetId,
                petId,
                ownerId,
                LocalDateTime.of(2026, 11, 10, 10, 0),
                LocalDateTime.of(2026, 11, 10, 11, 0)
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        // Overlapping request: 10:30 - 11:30
        AppointmentRequest req2 = new AppointmentRequest(
                vetId,
                petId,
                ownerId,
                LocalDateTime.of(2026, 11, 10, 10, 30),
                LocalDateTime.of(2026, 11, 10, 11, 30)
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("Doctor is not available at the selected time slot.")));
    }

    @Test
    @DisplayName("GET /api/appointments filters by vetId & date or ownerId")
    void testGetAppointments() throws Exception {
        AppointmentRequest req = new AppointmentRequest(
                vetId,
                petId,
                ownerId,
                LocalDateTime.of(2026, 11, 10, 14, 0),
                LocalDateTime.of(2026, 11, 10, 15, 0)
        );

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Query by vetId and date
        mockMvc.perform(get("/api/appointments?vetId=" + vetId + "&date=2026-11-10")
                        .header("X-User-Id", vetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].petName", is("Buddy")));

        // Query by ownerId
        mockMvc.perform(get("/api/appointments?ownerId=" + ownerId)
                        .header("X-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ownerName", is("John Doe")));
    }

    @Test
    @DisplayName("PATCH /api/appointments/{id}/status updates status and returns 200 OK")
    void testUpdateStatus() throws Exception {
        AppointmentRequest req = new AppointmentRequest(
                vetId,
                petId,
                ownerId,
                LocalDateTime.of(2026, 11, 12, 9, 0),
                LocalDateTime.of(2026, 11, 12, 10, 0)
        );

        String postRes = mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long apptId = objectMapper.readTree(postRes).get("id").asLong();

        // Patch to CONFIRMED
        StatusUpdateRequest patchReq = new StatusUpdateRequest("CONFIRMED");
        mockMvc.perform(patch("/api/appointments/" + apptId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", vetId)
                        .content(objectMapper.writeValueAsString(patchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }
}
