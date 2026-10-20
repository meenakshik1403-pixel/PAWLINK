package com.pawlink.sitter.dto;

/**
 * Payload sent to POST /api/sitter-bookings/{id}/checkins
 */
public class CheckinRequest {

    private String note;

    public CheckinRequest() {
    }

    public CheckinRequest(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

