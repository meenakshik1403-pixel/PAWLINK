package com.pawlink.admin;

import com.pawlink.admin.dto.SystemStatsDTO;
import com.pawlink.common.exception.ResourceNotFoundException;
import com.pawlink.common.exception.UnauthorizedException;
import com.pawlink.sitter.SitterDAO;
import com.pawlink.sitter.dto.SitterDTO;
import com.pawlink.user.DocumentDAO;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserDocumentDTO;
import com.pawlink.vet.VetDAO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Administrator operations and Analytical Aggregations:
 * - PATCH /api/admin/sitters/{id}/approve (supports setting hourlyRate)
 * - PATCH /api/admin/vets/{id}/approve
 * - GET   /api/admin/stats
 * - GET   /api/admin/sitters
 * - GET   /api/admin/vets
 * - GET   /api/admin/documents/{userId} (secured: admin only)
 * - GET   /api/admin/documents/download/{documentId} (secured: admin only)
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final SitterDAO sitterDAO;
    private final VetDAO vetDAO;
    private final DocumentDAO documentDAO;
    private final UserDAO userDAO;

    public AdminController(AdminService adminService, SitterDAO sitterDAO, VetDAO vetDAO, DocumentDAO documentDAO, UserDAO userDAO) {
        this.adminService = adminService;
        this.sitterDAO = sitterDAO;
        this.vetDAO = vetDAO;
        this.documentDAO = documentDAO;
        this.userDAO = userDAO;
    }

    private void validateAdminRole(Long xUserId, String xUserRole, Long adminIdParam) {
        if ("ADMIN".equalsIgnoreCase(xUserRole)) {
            return;
        }
        Long effectiveId = xUserId != null ? xUserId : adminIdParam;
        if (effectiveId != null) {
            boolean isAdmin = userDAO.findById(effectiveId)
                    .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                    .orElse(false);
            if (isAdmin) {
                return;
            }
        }
        throw new UnauthorizedException("Access Denied: Only administrators can view or download user verification documents.");
    }

    /**
     * PATCH /api/admin/sitters/{id}/approve: Approves a sitter, setting verified = TRUE,
     * and optionally updates their hourly rate based on reviewed documentation.
     */
    @PatchMapping("/sitters/{id}/approve")
    public ResponseEntity<Void> approveSitter(
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(name = "hourlyRate", required = false) BigDecimal hourlyRateParam
    ) {
        BigDecimal hourlyRate = hourlyRateParam;
        if (hourlyRate == null && body != null && body.containsKey("hourlyRate")) {
            Object val = body.get("hourlyRate");
            if (val instanceof Number num) {
                hourlyRate = BigDecimal.valueOf(num.doubleValue());
            } else if (val instanceof String str && !str.trim().isEmpty()) {
                hourlyRate = new BigDecimal(str.trim());
            }
        }
        adminService.approveSitter(id, hourlyRate);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/admin/vets/{id}/approve: Approves a veterinarian, setting verified = TRUE.
     */
    @PatchMapping("/vets/{id}/approve")
    public ResponseEntity<Void> approveVet(@PathVariable("id") Long id) {
        adminService.approveVet(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/stats: Fetches aggregated platform metrics (Appointments per Vet, Bookings per Sitter, Users by Role).
     */
    @GetMapping("/stats")
    public ResponseEntity<SystemStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    /**
     * GET /api/admin/sitters: List all sitters with verification statuses for admin inspection.
     */
    @GetMapping("/sitters")
    public ResponseEntity<List<SitterDTO>> getAllSitters() {
        List<SitterDTO> list = sitterDAO.getSitters().stream()
                .map(SitterDTO::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/admin/vets: List all veterinarians with verification statuses for admin inspection.
     */
    @GetMapping("/vets")
    public ResponseEntity<List<Map<String, Object>>> getAllVets() {
        return ResponseEntity.ok(vetDAO.getAllVetsWithStatus());
    }

    /**
     * GET /api/admin/documents/{userId}: List all uploaded qualification and ID documents for a user.
     * Restricted strictly to administrators.
     */
    @GetMapping("/documents/{userId}")
    public ResponseEntity<List<UserDocumentDTO>> getDocumentsByUser(
            @PathVariable("userId") Long userId,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId,
            @RequestHeader(name = "X-User-Role", required = false) String xUserRole,
            @RequestParam(name = "adminId", required = false) Long adminIdParam
    ) {
        validateAdminRole(xUserId, xUserRole, adminIdParam);
        return ResponseEntity.ok(documentDAO.findByUserId(userId));
    }

    /**
     * GET /api/admin/documents/download/{documentId}: Download / stream the uploaded PDF document.
     * Restricted strictly to administrators.
     */
    @GetMapping("/documents/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable("documentId") Long documentId,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId,
            @RequestHeader(name = "X-User-Role", required = false) String xUserRole,
            @RequestParam(name = "adminId", required = false) Long adminIdParam
    ) {
        validateAdminRole(xUserId, xUserRole, adminIdParam);

        UserDocumentDTO doc = documentDAO.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        File file = new File(doc.getFilePath());
        if (!file.exists()) {
            throw new ResourceNotFoundException("Physical file not found on disk: " + doc.getFileName());
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }
}
