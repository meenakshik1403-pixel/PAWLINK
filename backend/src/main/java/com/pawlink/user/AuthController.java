package com.pawlink.user;

import com.pawlink.common.exception.UnauthorizedException;
import com.pawlink.user.dto.AuthResponse;
import com.pawlink.user.dto.LoginRequest;
import com.pawlink.user.dto.RegisterRequest;
import com.pawlink.user.dto.UserSummaryDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Authentication and Core User endpoints.
 * Endpoints:
 * - POST /api/auth/register
 * - POST /api/auth/login
 * - GET  /api/vets
 * - GET  /api/auth/me (validates X-User-Id header)
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user account.
     *
     * @param req Registration payload (name, email, password, phone, role)
     * @return 201 Created with AuthResponse: { "id": 1, "name": "...", "email": "...", "role": "..." }
     */
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user.
     *
     * @param req Login payload (email, password)
     * @return 200 OK with AuthResponse: { "id": 1, "name": "...", "email": "...", "role": "..." }
     */
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse response = userService.login(req);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a list of all registered veterinarians (users with role = 'VET').
     *
     * @return 200 OK with list of veterinarians
     */
    @GetMapping("/vets")
    public ResponseEntity<List<UserSummaryDTO>> getVeterinarians() {
        List<UserSummaryDTO> vets = userService.getVeterinarians();
        return ResponseEntity.ok(vets);
    }

    /**
     * Verifies current session based on the X-User-Id header.
     *
     * @param userId header passed from authenticated client
     * @return 200 OK with AuthResponse
     */
    @GetMapping("/auth/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("Missing required 'X-User-Id' header.");
        }
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(new AuthResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        ));
    }
}
