package com.pawlink.sitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.pet.PetDAO;
import com.pawlink.pet.PetFactory;
import com.pawlink.sitter.dto.BookingRequest;
import com.pawlink.sitter.dto.CheckinRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SitterBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private SitterDAO sitterDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long verifiedSitterId;
    private Long unverifiedSitterId;
    private Long ownerId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM checkins");
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM sitter_bookings");
        jdbcTemplate.update("DELETE FROM sitters");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        // Verified Sitter
        var sitter1 = UserFactory.createUser("SITTER", null, "Emily Johnson", "emily.sitter@pawlink.com", "555-0201", "hash", LocalDateTime.now());
        userDAO.insert(sitter1);
        verifiedSitterId = sitter1.getUserId();
        sitterDAO.saveSitter(verifiedSitterId, BigDecimal.valueOf(30.00), true);

        // Unverified Sitter
        var sitter2 = UserFactory.createUser("SITTER", null, "Pramod S P", "david.sitter@pawlink.com", "555-0202", "hash", LocalDateTime.now());
        userDAO.insert(sitter2);
        unverifiedSitterId = sitter2.getUserId();
        sitterDAO.saveSitter(unverifiedSitterId, BigDecimal.valueOf(20.00), false);

        // Owner & Pet
        var owner = UserFactory.createUser("OWNER", null, "John Vargese", "john.owner@pawlink.com", "555-0301", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();

        var pet = PetFactory.createPet("Dog", "Bruno", LocalDate.of(2021, 5, 10), ownerId);
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("GET /api/sitters returns all sitters")
    void testGetSitters() throws Exception {
        mockMvc.perform(get("/api/sitters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Emily Johnson")))
                .andExpect(jsonPath("$[0].verified", is(true)));
    }

    @Test
    @DisplayName("POST /api/sitter-bookings with verified sitter succeeds and holds payment")
    void testBookVerifiedSitterSuccess() throws Exception {
        BookingRequest req = new BookingRequest(
                petId,
                verifiedSitterId,
                ownerId,
                LocalDateTime.of(2026, 12, 1, 9, 0),
                LocalDateTime.of(2026, 12, 1, 13, 0),
                "CARD"
        );

        mockMvc.perform(post("/api/sitter-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sitterName", is("Emily Johnson")))
                .andExpect(jsonPath("$.petName", is("Bruno")))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.paymentStatus", is("HELD")))
                .andExpect(jsonPath("$.totalAmount", is(120.0)));
    }

    @Test
    @DisplayName("POST /api/sitter-bookings with unverified sitter fails with HTTP 400 Bad Request")
    void testBookUnverifiedSitterFails() throws Exception {
        BookingRequest req = new BookingRequest(
                petId,
                unverifiedSitterId,
                ownerId,
                LocalDateTime.of(2026, 12, 1, 9, 0),
                LocalDateTime.of(2026, 12, 1, 13, 0),
                "CARD"
        );

        mockMvc.perform(post("/api/sitter-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("not verified")));
    }

    @Test
    @DisplayName("POST and GET /api/sitter-bookings/{id}/checkins logs notes and returns timeline")
    void testCheckinFlow() throws Exception {
        // First, create booking
        BookingRequest req = new BookingRequest(
                petId,
                verifiedSitterId,
                ownerId,
                LocalDateTime.of(2026, 12, 2, 10, 0),
                LocalDateTime.of(2026, 12, 2, 12, 0),
                "UPI"
        );

        String bookingJson = mockMvc.perform(post("/api/sitter-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(bookingJson).get("id").asLong();

        // Add Check-In note
        CheckinRequest checkinReq = new CheckinRequest("Pet fed fresh kibble and water. Happy and energetic.");
        mockMvc.perform(post("/api/sitter-bookings/" + bookingId + "/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkinReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.bookingId", is(bookingId.intValue())))
                .andExpect(jsonPath("$.note", containsString("Pet fed fresh kibble")));

        // Query checkins
        mockMvc.perform(get("/api/sitter-bookings/" + bookingId + "/checkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].note", containsString("Pet fed fresh kibble")));
    }
}
