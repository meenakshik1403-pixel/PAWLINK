package com.pawlink.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Domain model representing a veterinary clinic appointment.
 * Implements {@link Schedulable} for interval overlap detection.
 * Strictly NO JPA / Hibernate annotations. Uses Spring JdbcTemplate.
 */
public class Appointment implements Schedulable {

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

    private String status; // 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'
    private LocalDateTime createdAt;

    public Appointment() {
    }

    public Appointment(Long id, Long vetId, Long petId, Long ownerId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id;
        this.vetId = vetId;
        this.petId = petId;
        this.ownerId = ownerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    @Override
    public LocalDateTime getStart() {
        return startTime;
    }

    @Override
    public LocalDateTime getEnd() {
        return endTime;
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

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", vetId=" + vetId +
                ", vetName='" + vetName + '\'' +
                ", petId=" + petId +
                ", petName='" + petName + '\'' +
                ", ownerId=" + ownerId +
                ", ownerName='" + ownerName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                '}';
    }
}
