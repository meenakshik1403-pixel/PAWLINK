package com.pawlink.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pawlink.appointment.Appointment;

import java.time.LocalDateTime;

/**
 * REST API response DTO representing an appointment with resolved patient, vet, and owner names.
 */
public class AppointmentResponse {

    private Long id;
    private Long vetId;
    private String vetName;
    private Long petId;
    private String petName;
    private Long ownerId;
    private String ownerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, Long vetId, String vetName, Long petId, String petName, Long ownerId, String ownerName, LocalDateTime startTime, LocalDateTime endTime, String status, LocalDateTime createdAt) {
        this.id = id;
        this.vetId = vetId;
        this.vetName = vetName;
        this.petId = petId;
        this.petName = petName;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static AppointmentResponse from(Appointment a) {
        if (a == null) return null;
        return new AppointmentResponse(
                a.getId(),
                a.getVetId(),
                a.getVetName(),
                a.getPetId(),
                a.getPetName(),
                a.getOwnerId(),
                a.getOwnerName(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVetId() {
        return vetId;
    }

    public void setVetId(Long vetId) {
        this.vetId = vetId;
    }

    public String getVetName() {
        return vetName;
    }

    public void setVetName(String vetName) {
        this.vetName = vetName;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
