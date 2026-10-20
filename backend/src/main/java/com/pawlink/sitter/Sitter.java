package com.pawlink.sitter;

import java.math.BigDecimal;

/**
 * Domain entity representing a Pet Sitter's profile and credentials.
 */
public class Sitter {

    private Long userId;
    private String name;
    private String email;
    private String phone;
    private BigDecimal hourlyRate;
    private boolean verified;

    public Sitter() {
    }

    public Sitter(Long userId, String name, String email, String phone, BigDecimal hourlyRate, boolean verified) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.hourlyRate = hourlyRate;
        this.verified = verified;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}

