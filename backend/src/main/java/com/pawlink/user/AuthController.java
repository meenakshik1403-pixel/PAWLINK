package com.pawlink.user;

import com.pawlink.common.exception.UnauthorizedException;
import com.pawlink.user.dto.AuthResponse;
import com.pawlink.user.dto.LoginRequest;
import com.pawlink.user.dto.RegisterRequest;
import com.pawlink.user.dto.UserSummaryDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * REST Controller exposing Authentication and Core User endpoints.
 * Endpoints:
 * - POST /api/auth/register
 * - POST /api/auth/register-with-documents (Multipart upload for Sitter / Vet applications)
 * - POST /api/auth/login
 * - GET  /api/vets
 * - GET  /api/auth/me (validates X-User-Id header)
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final UserService userService;
    private final DocumentDAO documentDAO;

    public AuthController(UserService userService, DocumentDAO documentDAO) {
        this.userService = userService;
        this.documentDAO = documentDAO;
    }

    /**
     * Registers a new user account via JSON.
     *
     * @param req Registration payload (name, email, password, phone, role)
     * @return 201 Created with AuthResponse: { "id": 1, "name": "...", "email": "...", "role": "..." }
     */
    @PostMapping(value = "/auth/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Registers a new user account with required verification documents (PDF, max 10MB).
     * Used by Sitters and Veterinarians during registration for admin approval review.
     */
    @PostMapping(value = "/auth/register-with-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> registerWithDocuments(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestParam("role") String role,
            @RequestParam(value = "idCard", required = false) MultipartFile idCard,
            @RequestParam(value = "qualificationProof", required = false) MultipartFile qualificationProof
    ) throws IOException {
        String normalizedRole = role != null ? role.trim().toUpperCase() : "";
        if (!"OWNER".equals(normalizedRole) && !"SITTER".equals(normalizedRole) && !"VET".equals(normalizedRole)) {
            throw new IllegalArgumentException("Role must be OWNER, VET, or SITTER.");
        }

        // Validate required documents for Sitter and Vet roles before user creation
        if ("SITTER".equals(normalizedRole) || "VET".equals(normalizedRole)) {
            if (idCard == null || idCard.isEmpty()) {
                throw new IllegalArgumentException("ID Card proof PDF is required for " + normalizedRole + " registration.");
            }
            if (qualificationProof == null || qualificationProof.isEmpty()) {
                throw new IllegalArgumentException("Qualifications/Certificates proof PDF is required for " + normalizedRole + " registration.");
            }
            validatePdfFile(idCard, "ID Card");
            validatePdfFile(qualificationProof, "Qualification Certificate");
        }

        RegisterRequest req = new RegisterRequest(name, email, password, phone, normalizedRole);
        AuthResponse response = userService.register(req);

        // Store documents securely
        if ("SITTER".equals(normalizedRole) || "VET".equals(normalizedRole)) {
            saveUploadedDocument(response.getId(), "ID_CARD", idCard, normalizedRole);
            saveUploadedDocument(response.getId(), "QUALIFICATION_PROOF", qualificationProof, normalizedRole);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private void validatePdfFile(MultipartFile file, String label) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(label + " exceeds maximum allowed size of 10MB.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(label + " must be a PDF document (.pdf).");
        }
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4 || header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F') {
                throw new IllegalArgumentException(label + " is not a valid PDF file.");
            }
        }
    }

    private void saveUploadedDocument(Long userId, String docType, MultipartFile file, String role) throws IOException {
        Path uploadDir = Paths.get("uploads", "documents", role.toLowerCase(), String.valueOf(userId));
        Files.createDirectories(uploadDir);

        String safeFileName = docType.toLowerCase() + "_" + System.currentTimeMillis() + ".pdf";
        Path targetPath = uploadDir.resolve(safeFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        documentDAO.saveDocument(userId, docType, file.getOriginalFilename(), targetPath.toString(), file.getSize());
        log.info("Saved verification document [{}] for {} ID {}: {}", docType, role, userId, targetPath.toAbsolutePath());
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
     */
    @GetMapping("/vets")
    public ResponseEntity<List<UserSummaryDTO>> getVeterinarians() {
        return ResponseEntity.ok(userService.getVeterinarians());
    }

    /**
     * Returns the currently authenticated user's profile based on the X-User-Id header.
     */
    @GetMapping("/auth/me")
    public ResponseEntity<UserSummaryDTO> getCurrentUser(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            throw new UnauthorizedException("Missing X-User-Id authentication header.");
        }

        User user = userService.getUserById(userId);
        UserSummaryDTO dto = new UserSummaryDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
        return ResponseEntity.ok(dto);
    }
}
