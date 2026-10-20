package com.pawlink.sitter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Payload sent to POST /api/sitter-bookings to book a pet sitter.
 */
public class BookingRequest {

    private Long petId;
    private Long sitterId;
    private Long ownerId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private String paymentMethod; // 'CARD', 'UPI', 'WALLET'

    public BookingRequest() {
    }

    public BookingRequest(Long petId, Long sitterId, Long ownerId,
                          LocalDateTime startTime, LocalDateTime endTime, String paymentMethod) {
        this.petId = petId;
        this.sitterId = sitterId;
        this.ownerId = ownerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.paymentMethod = paymentMethod;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getSitterId() {
        return sitterId;
    }

    public void setSitterId(Long sitterId) {
        this.sitterId = sitterId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getPaymentMethod() {
        return paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod.toUpperCase() : "CARD";
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

