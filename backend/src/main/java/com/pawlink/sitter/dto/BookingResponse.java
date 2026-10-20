package com.pawlink.sitter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pawlink.sitter.SittingBooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Standard response representation for a Sitter Booking.
 */
public class BookingResponse {

    private Long id;
    private Long petId;
    private String petName;
    private Long sitterId;
    private String sitterName;
    private Long ownerId;
    private String ownerName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private String status;
    private BigDecimal hourlyRate;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public BookingResponse() {
    }

    public static BookingResponse from(SittingBooking booking) {
        if (booking == null) return null;
        BookingResponse resp = new BookingResponse();
        resp.setId(booking.getId());
        resp.setPetId(booking.getPetId());
        resp.setPetName(booking.getPetName());
        resp.setSitterId(booking.getSitterId());
        resp.setSitterName(booking.getSitterName());
        resp.setOwnerId(booking.getOwnerId());
        resp.setOwnerName(booking.getOwnerName());
        resp.setStartTime(booking.getStartTime());
        resp.setEndTime(booking.getEndTime());
        resp.setStatus(booking.getStatus());
        resp.setHourlyRate(booking.getHourlyRate());
        resp.setTotalAmount(booking.getTotalAmount());
        resp.setPaymentStatus(booking.getPaymentStatus());
        resp.setPaymentMethod(booking.getPaymentMethod());
        resp.setCreatedAt(booking.getCreatedAt());
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

