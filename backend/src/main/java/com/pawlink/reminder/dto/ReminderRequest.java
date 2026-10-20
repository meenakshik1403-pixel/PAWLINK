package com.pawlink.reminder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class ReminderRequest {

    private Long petId;
    private String type; // 'VACCINATION', 'MEDICATION', 'CHECKUP'

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    public ReminderRequest() {}

    public ReminderRequest(Long petId, String type, LocalDate dueDate) {
        this.petId = petId;
        this.type = type;
        this.dueDate = dueDate;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
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
}

