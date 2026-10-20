package com.pawlink.sitter;

import java.time.LocalDateTime;

/**
 * Domain entity representing a Sitter Care Check-in note for a booking.
 */
public class CheckIn {

    private Long id;
    private Long bookingId;
    private LocalDateTime timestamp;
    private String note;

    public CheckIn() {
    }

    public CheckIn(Long id, Long bookingId, LocalDateTime timestamp, String note) {
        this.id = id;
        this.bookingId = bookingId;
        this.timestamp = timestamp;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

