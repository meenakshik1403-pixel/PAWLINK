package com.pawlink.sitter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pawlink.sitter.CheckIn;

import java.time.LocalDateTime;

/**
 * Representation of a timestamped pet care check-in note.
 */
public class CheckinResponse {

    private Long id;
    private Long bookingId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String note;

    public CheckinResponse() {
    }

    public CheckinResponse(Long id, Long bookingId, LocalDateTime timestamp, String note) {
        this.id = id;
        this.bookingId = bookingId;
        this.timestamp = timestamp;
        this.note = note;
    }

    public static CheckinResponse from(CheckIn checkin) {
        if (checkin == null) return null;
        return new CheckinResponse(
                checkin.getId(),
                checkin.getBookingId(),
                checkin.getTimestamp(),
                checkin.getNote()
        );
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

