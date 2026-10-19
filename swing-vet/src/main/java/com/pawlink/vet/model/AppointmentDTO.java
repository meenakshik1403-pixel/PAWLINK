package com.pawlink.vet.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for Appointments consumed by the Swing Client.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentDTO {

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

    public AppointmentDTO() {}

    public AppointmentDTO(Long id, Long vetId, String vetName, Long petId, String petName,
                          Long ownerId, String ownerName, LocalDateTime startTime,
                          LocalDateTime endTime, String status, LocalDateTime createdAt) {
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

    public String getTimeSlotFormatted() {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (startTime != null && endTime != null) {
            return startTime.format(dateFmt) + " " + startTime.format(timeFmt) + " - " + endTime.format(timeFmt);
        } else if (startTime != null) {
            return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return "N/A";
    }
}
