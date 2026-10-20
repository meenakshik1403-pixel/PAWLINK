package com.pawlink.admin;

import com.pawlink.admin.dto.SystemStatsDTO;
import com.pawlink.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing Admin approvals and analytical statistics.
 */
@Service
public class AdminService {

    private final AdminDAO adminDAO;

    public AdminService(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    /**
     * Approves a sitter for bookings.
     */
    @Transactional
    public void approveSitter(Long sitterId) {
        if (sitterId == null || sitterId <= 0) {
            throw new IllegalArgumentException("Valid Sitter ID is required.");
        }
        boolean updated = adminDAO.approveSitter(sitterId);
        if (!updated) {
            throw new ResourceNotFoundException("Sitter profile not found with ID: " + sitterId);
        }
    }

    /**
     * Approves a sitter for bookings and assigns/adjusts their hourly rate.
     */
    @Transactional
    public void approveSitter(Long sitterId, java.math.BigDecimal hourlyRate) {
        if (hourlyRate != null) {
            if (sitterId == null || sitterId <= 0) {
                throw new IllegalArgumentException("Valid Sitter ID is required.");
            }
            boolean updated = adminDAO.approveSitter(sitterId, hourlyRate);
            if (!updated) {
                throw new ResourceNotFoundException("Sitter profile not found with ID: " + sitterId);
            }
        } else {
            approveSitter(sitterId);
        }
    }

    /**
     * Approves a veterinarian for clinic appointments.
     */
    @Transactional
    public void approveVet(Long vetId) {
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Valid Veterinarian ID is required.");
        }
        boolean updated = adminDAO.approveVet(vetId);
        if (!updated) {
            throw new ResourceNotFoundException("Veterinarian profile not found with ID: " + vetId);
        }
    }

    /**
     * Retrieves aggregated platform statistics across users, clinic appointments, and sitter jobs.
     */
    public SystemStatsDTO getStats() {
        return adminDAO.getStats();
    }
}
