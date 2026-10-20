package com.pawlink.admin;

import com.pawlink.admin.dto.SystemStatsDTO;
import com.pawlink.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminDAO adminDAO;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("approveSitter updates sitter verification status")
    void testApproveSitterSuccess() {
        when(adminDAO.approveSitter(10L)).thenReturn(true);
        assertDoesNotThrow(() -> adminService.approveSitter(10L));
        verify(adminDAO).approveSitter(10L);
    }

    @Test
    @DisplayName("approveSitter throws ResourceNotFoundException when sitter not found")
    void testApproveSitterNotFound() {
        when(adminDAO.approveSitter(999L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> adminService.approveSitter(999L));
    }

    @Test
    @DisplayName("approveVet updates vet verification status")
    void testApproveVetSuccess() {
        when(adminDAO.approveVet(20L)).thenReturn(true);
        assertDoesNotThrow(() -> adminService.approveVet(20L));
        verify(adminDAO).approveVet(20L);
    }

    @Test
    @DisplayName("approveVet throws ResourceNotFoundException when vet not found")
    void testApproveVetNotFound() {
        when(adminDAO.approveVet(999L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> adminService.approveVet(999L));
    }

    @Test
    @DisplayName("getStats returns aggregated platform statistics")
    void testGetStats() {
        SystemStatsDTO mockStats = new SystemStatsDTO(
                10, 5, 3,
                Map.of("OWNER", 5, "VET", 3, "SITTER", 2),
                Map.of("Dr. Sarah", 3, "Dr. Mike", 2),
                Map.of("Emily", 3)
        );
        when(adminDAO.getStats()).thenReturn(mockStats);

        SystemStatsDTO result = adminService.getStats();
        assertNotNull(result);
        assertEquals(10, result.getTotalUsers());
        verify(adminDAO).getStats();
    }
}
