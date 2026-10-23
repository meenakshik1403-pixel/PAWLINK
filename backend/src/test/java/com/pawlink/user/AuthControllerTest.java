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
import org.springframework.mock.web.MockMultipartFile;
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
        jdbcTemplate.update("DELETE FROM user_documents");
        jdbcTemplate.update("DELETE FROM vets");
        jdbcTemplate.update("DELETE FROM sitters");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("POST /api/auth/register creates user and returns 201 Created")
    void testRegisterEndpoint() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Dr. Michael Phillip",
                "vet.michael@pawlink.com",
                "password123",
                "+919876543102",
                "VET"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Dr. Michael Phillip")))
                .andExpect(jsonPath("$.email", is("vet.michael@pawlink.com")))
                .andExpect(jsonPath("$.role", is("VET")));
    }

    @Test
    @DisplayName("POST /api/auth/login returns user info on valid credentials")
    void testLoginEndpoint() throws Exception {
        // First register an Owner
        RegisterRequest regReq = new RegisterRequest(
                "John Vargese",
                "owner.john@pawlink.com",
                "password123",
                "+919876543401",
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
                .andExpect(jsonPath("$.name", is("John Vargese")))
                .andExpect(jsonPath("$.email", is("owner.john@pawlink.com")))
                .andExpect(jsonPath("$.role", is("OWNER")));
    }

    @Test
    @DisplayName("POST /api/auth/login for unapproved Vet or Sitter returns 403 Forbidden")
    void testUnapprovedVetLoginFails() throws Exception {
        // Register a Vet (starts unverified)
        RegisterRequest regReq = new RegisterRequest(
                "Dr. Newbie",
                "vet.newbie@pawlink.com",
                "password123",
                "+919876543199",
                "VET"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)));

        // Attempt login while pending approval
        LoginRequest loginReq = new LoginRequest("vet.newbie@pawlink.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("pending admin approval")));
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
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest("Dr. Vet1", "v1@pawlink.com", "pass123", "+919876543501", "VET"))));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest("Owner1", "o1@pawlink.com", "pass123", "+919876543502", "OWNER"))));

        mockMvc.perform(get("/api/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Dr. Vet1")))
                .andExpect(jsonPath("$[0].role", is("VET")));
    }

    @Test
    @DisplayName("POST /api/auth/register with ADMIN role returns 400 Bad Request")
    void testRegisterAdminForbiddenEndpoint() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Attempted Admin",
                "hacker@pawlink.com",
                "password123",
                "+919876543999",
                "ADMIN"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register-with-documents creates sitter and stores PDF proofs")
    void testRegisterWithDocuments() throws Exception {
        MockMultipartFile idCard = new MockMultipartFile(
                "idCard",
                "my_id.pdf",
                "application/pdf",
                "%PDF-1.4 mock id bytes".getBytes()
        );
        MockMultipartFile qualProof = new MockMultipartFile(
                "qualificationProof",
                "certificate.pdf",
                "application/pdf",
                "%PDF-1.4 mock cert bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/auth/register-with-documents")
                        .file(idCard)
                        .file(qualProof)
                        .param("name", "Applicant Sitter")
                        .param("email", "applicant.sitter@pawlink.com")
                        .param("password", "password123")
                        .param("phone", "9876543210")
                        .param("role", "SITTER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.role", is("SITTER")));
    }
}
