package com.pawlink.web.dto;

import java.time.LocalDateTime;

/**
 * Form backing object for booking a pet sitter through the web portal.
 */
public class BookingForm {

    private Long petId;
    private Long sitterId;
    private String startDateTime; // ISO format: yyyy-MM-ddTHH:mm
    private String endDateTime;   // ISO format: yyyy-MM-ddTHH:mm
    private String paymentMethod = "CARD";

    public BookingForm() {
    }

    public BookingForm(Long sitterId) {
        this.sitterId = sitterId;
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

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

