package com.pawlink.user;

import com.pawlink.common.exception.AccountPendingApprovalException;
import com.pawlink.common.exception.AuthenticationFailedException;
import com.pawlink.common.exception.UserAlreadyExistsException;
import com.pawlink.user.dto.AuthResponse;
import com.pawlink.user.dto.LoginRequest;
import com.pawlink.user.dto.RegisterRequest;
import com.pawlink.user.dto.UserSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("Should register new user and hash password with BCrypt")
    void testRegisterUser() {
        RegisterRequest req = new RegisterRequest(
                "Dr. Sreekumar",
                "vet.sreekumar@pawlink.com",
                "password123",
                "+919876543101",
                "VET"
        );

        AuthResponse res = userService.register(req);
        assertNotNull(res);
        assertNotNull(res.getId());
        assertEquals("Dr. Sreekumar", res.getName());
        assertEquals("vet.sreekumar@pawlink.com", res.getEmail());
        assertEquals("VET", res.getRole());

        // Verify in database that password is not stored plaintext
        String dbHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?",
                String.class,
                res.getId()
        );
        assertNotEquals("password123", dbHash);
        assertTrue(passwordEncoder.matches("password123", dbHash));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testDuplicateRegistration() {
        RegisterRequest req = new RegisterRequest(
                "Owner John",
                "owner.john@pawlink.com",
                "password123",
                "+919876543401",
                "OWNER"
        );
        userService.register(req);

        // Second registration with same email
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(req));
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials for approved user")
    void testLoginSuccess() {
        RegisterRequest regReq = new RegisterRequest(
                "Emily Johnson",
                "sitter.emily@pawlink.com",
                "password123",
                "+919876543201",
                "SITTER"
        );
        AuthResponse res = userService.register(regReq);

        // Verify the sitter so login succeeds
        jdbcTemplate.update("UPDATE sitters SET verified = TRUE WHERE user_id = ?", res.getId());

        LoginRequest loginReq = new LoginRequest("sitter.emily@pawlink.com", "password123");
        AuthResponse loginRes = userService.login(loginReq);

        assertNotNull(loginRes);
        assertEquals("Emily Johnson", loginRes.getName());
        assertEquals("SITTER", loginRes.getRole());
    }

    @Test
    @DisplayName("Should reject login for unapproved Sitter or Vet")
    void testUnapprovedLoginFails() {
        RegisterRequest regReq = new RegisterRequest(
                "Pending Sitter",
                "sitter.pending@pawlink.com",
                "password123",
                "+919876543202",
                "SITTER"
        );
        userService.register(regReq);

        LoginRequest loginReq = new LoginRequest("sitter.pending@pawlink.com", "password123");
        assertThrows(AccountPendingApprovalException.class, () -> userService.login(loginReq));
    }

    @Test
    @DisplayName("Should reject login with wrong password or unknown email")
    void testLoginFailure() {
        RegisterRequest regReq = new RegisterRequest(
                "Test Owner",
                "test.owner@pawlink.com",
                "password123",
                "+919876543301",
                "OWNER"
        );
        userService.register(regReq);

        // Wrong password
        assertThrows(AuthenticationFailedException.class, () ->
                userService.login(new LoginRequest("test.owner@pawlink.com", "wrongpass")));

        // Non-existent email
        assertThrows(AuthenticationFailedException.class, () ->
                userService.login(new LoginRequest("unknown@pawlink.com", "password123")));
    }

    @Test
    @DisplayName("Should list all veterinarians")
    void testFindVets() {
        userService.register(new RegisterRequest("Dr. Alpha", "vet.alpha@pawlink.com", "pass123", "+919876543111", "VET"));
        userService.register(new RegisterRequest("Dr. Beta", "vet.beta@pawlink.com", "pass123", "+919876543112", "VET"));
        userService.register(new RegisterRequest("Owner Charlie", "owner.charlie@pawlink.com", "pass123", "+919876543113", "OWNER"));

        List<UserSummaryDTO> vets = userService.getVeterinarians();
        assertEquals(2, vets.size());
        assertTrue(vets.stream().allMatch(v -> "VET".equals(v.getRole())));
    }

    @Test
    @DisplayName("Should reject admin registration publicly")
    void testAdminRegistrationForbidden() {
        RegisterRequest regReq = new RegisterRequest(
                "Attempted Admin",
                "hacker@pawlink.com",
                "password123",
                "+919876543999",
                "ADMIN"
        );
        assertThrows(IllegalArgumentException.class, () -> userService.register(regReq));
    }
}
