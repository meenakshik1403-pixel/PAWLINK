package com.pawlink.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pawlink.user.dto.LoginRequest;
import com.pawlink.user.dto.RegisterRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("POST /api/auth/register creates user and returns 201 Created")
    void testRegisterEndpoint() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Dr. Michael Brown",
                "vet.michael@pawlink.com",
                "password123",
                "+1-555-0102",
                "VET"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Dr. Michael Brown")))
                .andExpect(jsonPath("$.email", is("vet.michael@pawlink.com")))
                .andExpect(jsonPath("$.role", is("VET")));
    }

    @Test
    @DisplayName("POST /api/auth/login returns user info on valid credentials")
    void testLoginEndpoint() throws Exception {
        // First register
        RegisterRequest regReq = new RegisterRequest(
                "John Doe",
                "owner.john@pawlink.com",
                "password123",
                "+1-555-0401",
                "OWNER"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)));

        // Then login
        LoginRequest loginReq = new LoginRequest("owner.john@pawlink.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("owner.john@pawlink.com")))
                .andExpect(jsonPath("$.role", is("OWNER")));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 401 on bad credentials")
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest loginReq = new LoginRequest("nonexistent@pawlink.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/vets returns list of veterinarians")
    void testGetVetsEndpoint() throws Exception {
        // Register one vet and one owner
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest("Dr. Vet1", "v1@pawlink.com", "pass123", null, "VET"))));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest("Owner1", "o1@pawlink.com", "pass123", null, "OWNER"))));

        mockMvc.perform(get("/api/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Dr. Vet1")))
                .andExpect(jsonPath("$[0].role", is("VET")));
    }
}
