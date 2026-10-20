package com.pawlink.reminder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing an automated or scheduled pet care reminder.
 * Strictly adheres to technical constraints: NO JPA/Hibernate annotations.
 */
public class Reminder {

    private Long id;
    private Long petId;
    private String petName;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String type; // 'VACCINATION', 'MEDICATION', 'CHECKUP'
    private LocalDate dueDate;
    private boolean sent;
    private LocalDateTime createdAt;

    public Reminder() {}

    public Reminder(Long id, Long petId, String petName, Long ownerId, String ownerName,
                    String ownerEmail, String ownerPhone, String type, LocalDate dueDate,
                    boolean sent, LocalDateTime createdAt) {
        this.id = id;
        this.petId = petId;
        this.petName = petName;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.ownerPhone = ownerPhone;
        this.type = type;
        this.dueDate = dueDate;
        this.sent = sent;
        this.createdAt = createdAt;
    }

    public Reminder(Long petId, String type, LocalDate dueDate) {
        this(null, petId, null, null, null, null, null, type, dueDate, false, LocalDateTime.now());
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

    public boolean isOverdue() {
        return !sent && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public boolean isDueToday() {
        return !sent && dueDate != null && dueDate.isEqual(LocalDate.now());
    }

    public String getUrgencyLevel() {
        if (sent) return "SENT";
        if (isOverdue()) return "OVERDUE";
        if (isDueToday()) return "DUE_TODAY";
        return "UPCOMING";
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", petId=" + petId +
                ", petName='" + petName + '\'' +
                ", type='" + type + '\'' +
                ", dueDate=" + dueDate +
                ", sent=" + sent +
                ", urgency=" + getUrgencyLevel() +
                '}';
    }
}

