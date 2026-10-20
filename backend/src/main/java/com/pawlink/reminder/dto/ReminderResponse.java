package com.pawlink.reminder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pawlink.reminder.Reminder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReminderResponse {

    private Long id;
    private Long petId;
    private String petName;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private boolean sent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String urgency;

    public ReminderResponse() {}

    public static ReminderResponse from(Reminder r) {
        if (r == null) return null;
        ReminderResponse resp = new ReminderResponse();
        resp.setId(r.getId());
        resp.setPetId(r.getPetId());
        resp.setPetName(r.getPetName());
        resp.setOwnerId(r.getOwnerId());
        resp.setOwnerName(r.getOwnerName());
        resp.setOwnerEmail(r.getOwnerEmail());
        resp.setOwnerPhone(r.getOwnerPhone());
        resp.setType(r.getType());
        resp.setDueDate(r.getDueDate());
        resp.setSent(r.isSent());
        resp.setCreatedAt(r.getCreatedAt());
        resp.setUrgency(r.getUrgencyLevel());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
}

