package com.pawlink.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representation of check-in care logs in Web Portal.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckinDTO {

    private Long id;
    private Long bookingId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String note;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

    public CheckinDTO() {
    }

    public CheckinDTO(Long id, Long bookingId, LocalDateTime timestamp, String note) {
        this.id = id;
        this.bookingId = bookingId;
        this.timestamp = timestamp;
        this.note = note;
    }

    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(FORMATTER) : "N/A";
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

