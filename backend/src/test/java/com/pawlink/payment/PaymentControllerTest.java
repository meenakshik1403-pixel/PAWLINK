package com.pawlink.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.pet.PetDAO;
import com.pawlink.pet.PetFactory;
import com.pawlink.sitter.SitterDAO;
import com.pawlink.sitter.dto.BookingRequest;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentControllerTest {

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
    private PaymentDAO paymentDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId;
    private Long sitterId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM checkins");
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM sitter_bookings");
        jdbcTemplate.update("DELETE FROM sitters");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var sitter = UserFactory.createUser("SITTER", null, "Emily Watson", "emily.payment@pawlink.com", "555-0201", "hash", LocalDateTime.now());
        userDAO.insert(sitter);
        sitterId = sitter.getUserId();
        sitterDAO.saveSitter(sitterId, BigDecimal.valueOf(35.00), true);

        var owner = UserFactory.createUser("OWNER", null, "John Doe", "john.payment@pawlink.com", "555-0301", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();

        var pet = PetFactory.createPet("Dog", "Max", LocalDate.of(2020, 4, 12), ownerId);
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("GET /api/payments?ownerId={id} returns list of payments with joined booking details")
    void testGetPaymentsByOwner() throws Exception {
        // Create booking with escrow payment via API
        BookingRequest req = new BookingRequest(
                petId,
                sitterId,
                ownerId,
                LocalDateTime.of(2026, 11, 20, 10, 0),
                LocalDateTime.of(2026, 11, 20, 13, 0), // 3 hours @ $35 = $105
                "UPI"
        );

        mockMvc.perform(post("/api/sitter-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ownerId)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Query payment history for owner
        mockMvc.perform(get("/api/payments?ownerId=" + ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].method", is("UPI")))
                .andExpect(jsonPath("$[0].status", is("HELD")))
                .andExpect(jsonPath("$[0].amount", is(105.0)))
                .andExpect(jsonPath("$[0].petName", is("Max")))
                .andExpect(jsonPath("$[0].sitterName", is("Emily Watson")));
    }

    @Test
    @DisplayName("PATCH /api/payments/{id}/release transitions payment status to RELEASED")
    void testReleaseEscrow() throws Exception {
        BookingRequest req = new BookingRequest(
                petId,
                sitterId,
                ownerId,
                LocalDateTime.of(2026, 11, 21, 10, 0),
                LocalDateTime.of(2026, 11, 21, 12, 0),
                "CARD"
        );

        mockMvc.perform(post("/api/sitter-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        var payments = paymentDAO.findByOwnerId(ownerId);
        assertEquals(1, payments.size());
        Long paymentId = payments.get(0).getId();

        mockMvc.perform(patch("/api/payments/" + paymentId + "/release"))
                .andExpect(status().isNoContent());

        var updated = paymentDAO.findById(paymentId);
        assertEquals("RELEASED", updated.orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Transactional Integrity: Unsupported payment method fails and rolls back booking creation")
    void testTransactionalRollbackOnPaymentFailure() throws Exception {
        Integer initialCountObj = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sitter_bookings", Integer.class);
        int initialBookingCount = initialCountObj != null ? initialCountObj : 0;

        BookingRequest req = new BookingRequest(
                petId,
                sitterId,
                ownerId,
                LocalDateTime.of(2026, 11, 22, 10, 0),
                LocalDateTime.of(2026, 11, 22, 12, 0),
                "INVALID_CHANNEL"
        );

        mockMvc.perform(post("/api/sitter-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        Integer finalCountObj = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sitter_bookings", Integer.class);
        int finalBookingCount = finalCountObj != null ? finalCountObj : 0;
        assertEquals(initialBookingCount, finalBookingCount, "No booking row should exist when payment processing fails!");
    }
}

