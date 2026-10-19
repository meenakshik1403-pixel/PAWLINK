package com.pawlink.appointment.dto;

/**
 * Request DTO for updating appointment status (CONFIRMED, COMPLETED, CANCELLED).
 */
public class StatusUpdateRequest {

    private String status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
