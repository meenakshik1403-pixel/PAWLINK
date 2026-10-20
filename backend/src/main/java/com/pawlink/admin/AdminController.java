package com.pawlink.admin;

import com.pawlink.admin.dto.SystemStatsDTO;
import com.pawlink.sitter.SitterDAO;
import com.pawlink.sitter.dto.SitterDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Administrator operations and Analytical Aggregations:
 * - PATCH /api/admin/sitters/{id}/approve
 * - GET   /api/admin/stats
 * - GET   /api/admin/sitters
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final SitterDAO sitterDAO;

    public AdminController(AdminService adminService, SitterDAO sitterDAO) {
        this.adminService = adminService;
        this.sitterDAO = sitterDAO;
    }

    /**
     * PATCH /api/admin/sitters/{id}/approve: Approves a sitter, setting verified = TRUE.
     */
    @PatchMapping("/sitters/{id}/approve")
    public ResponseEntity<Void> approveSitter(@PathVariable("id") Long id) {
        adminService.approveSitter(id);
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
}

