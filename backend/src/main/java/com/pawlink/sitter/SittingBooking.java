package com.pawlink.sitter;

import com.pawlink.appointment.Schedulable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain entity representing a Pet Sitting Booking in PawLink.
 * Implements Schedulable to allow interval intersection and conflict detection.
 */
public class SittingBooking implements Schedulable {

    private Long id;
    private Long petId;
    private String petName;
    private Long sitterId;
    private String sitterName;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    private LocalDateTime createdAt;
    private BigDecimal hourlyRate;
    private BigDecimal totalAmount;
    private String paymentStatus; // 'HELD', 'RELEASED', 'REFUNDED', 'FAILED'
    private String paymentMethod; // 'CARD', 'UPI', 'WALLET'

    public SittingBooking() {
    }

    public SittingBooking(Long id, Long petId, Long sitterId, Long ownerId,
                          LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id;
        this.petId = petId;
        this.sitterId = sitterId;
        this.ownerId = ownerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = LocalDateTime.now();
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

    public Long getSitterId() {
        return sitterId;
    }

    public void setSitterId(Long sitterId) {
        this.sitterId = sitterId;
    }

    public String getSitterName() {
        return sitterName;
    }

    public void setSitterName(String sitterName) {
        this.sitterName = sitterName;
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

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

