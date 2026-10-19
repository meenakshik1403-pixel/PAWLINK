package com.pawlink.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Request DTO for scheduling a new veterinary clinic appointment.
 */
public class AppointmentRequest {

    private Long vetId;
    private Long petId;
    private Long ownerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public AppointmentRequest() {
    }

    public AppointmentRequest(Long vetId, Long petId, Long ownerId, LocalDateTime startTime, LocalDateTime endTime) {
        this.vetId = vetId;
        this.petId = petId;
        this.ownerId = ownerId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getVetId() {
        return vetId;
    }

    public void setVetId(Long vetId) {
        this.vetId = vetId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
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
}
