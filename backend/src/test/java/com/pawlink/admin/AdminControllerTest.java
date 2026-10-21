package com.pawlink.admin;

import com.pawlink.sitter.SitterDAO;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import com.pawlink.vet.VetDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SitterDAO sitterDAO;

    @Autowired
    private VetDAO vetDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long unverifiedSitterId;
    private Long unverifiedVetId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM checkins");
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM sitter_bookings");
        jdbcTemplate.update("DELETE FROM appointments");
        jdbcTemplate.update("DELETE FROM reminders");
        jdbcTemplate.update("DELETE FROM medical_records");
        jdbcTemplate.update("DELETE FROM vaccinations");
        jdbcTemplate.update("DELETE FROM user_documents");
        jdbcTemplate.update("DELETE FROM vets");
        jdbcTemplate.update("DELETE FROM sitters");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        // Create Unverified Sitter
        var sitter = UserFactory.createUser("SITTER", null, "Alex Pending", "alex.sitter@pawlink.com", "+919876543801", "hash", LocalDateTime.now());
        userDAO.insert(sitter);
        unverifiedSitterId = sitter.getUserId();
        sitterDAO.saveSitter(unverifiedSitterId, BigDecimal.valueOf(280.00), false);

        // Create a Vet and an Owner
        var vet = UserFactory.createUser("VET", null, "Dr. Sarah", "sarah@pawlink.com", "+919876543802", "hash", LocalDateTime.now());
        userDAO.insert(vet);
        unverifiedVetId = vet.getUserId();
        vetDAO.saveVet(unverifiedVetId, false);

        var owner = UserFactory.createUser("OWNER", null, "Lisa Owner", "lisa@pawlink.com", "+919876543803", "hash", LocalDateTime.now());
        userDAO.insert(owner);
    }

    @Test
    @DisplayName("PATCH /api/admin/sitters/{id}/approve approves unverified sitter")
    void testApproveSitter() throws Exception {
        mockMvc.perform(patch("/api/admin/sitters/" + unverifiedSitterId + "/approve"))
                .andExpect(status().isNoContent());

        assertTrue(sitterDAO.isVerified(unverifiedSitterId));
    }

    @Test
    @DisplayName("PATCH /api/admin/vets/{id}/approve approves unverified veterinarian")
    void testApproveVet() throws Exception {
        mockMvc.perform(patch("/api/admin/vets/" + unverifiedVetId + "/approve"))
                .andExpect(status().isNoContent());

        assertTrue(vetDAO.isVerified(unverifiedVetId));
    }

    @Test
    @DisplayName("GET /api/admin/stats returns aggregated platform statistics")
    void testGetStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(3)))
                .andExpect(jsonPath("$.usersByRole.SITTER", is(1)))
                .andExpect(jsonPath("$.usersByRole.VET", is(1)))
                .andExpect(jsonPath("$.usersByRole.OWNER", is(1)))
                .andExpect(jsonPath("$.appointmentsPerVet['Dr. Sarah']", notNullValue()))
                .andExpect(jsonPath("$.bookingsPerSitter['Alex Pending']", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/admin/sitters returns all sitters")
    void testGetAllSitters() throws Exception {
        mockMvc.perform(get("/api/admin/sitters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alex Pending")))
                .andExpect(jsonPath("$[0].verified", is(false)));
    }

    @Test
    @DisplayName("GET /api/admin/vets returns all veterinarians")
    void testGetAllVets() throws Exception {
        mockMvc.perform(get("/api/admin/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Dr. Sarah")))
                .andExpect(jsonPath("$[0].verified", is(false)));
    }
}
