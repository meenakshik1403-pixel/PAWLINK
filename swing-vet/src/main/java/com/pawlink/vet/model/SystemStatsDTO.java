package com.pawlink.vet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for Platform Statistics in Swing Desktop Client.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemStatsDTO {

    private int totalUsers;
    private int totalAppointments;
    private int totalBookings;
    private Map<String, Integer> usersByRole = new HashMap<>();
    private Map<String, Integer> appointmentsPerVet = new HashMap<>();
    private Map<String, Integer> bookingsPerSitter = new HashMap<>();

    public SystemStatsDTO() {
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(int totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public Map<String, Integer> getUsersByRole() {
        return usersByRole;
    }

    public void setUsersByRole(Map<String, Integer> usersByRole) {
        this.usersByRole = usersByRole;
    }

    public Map<String, Integer> getAppointmentsPerVet() {
        return appointmentsPerVet;
    }

    public void setAppointmentsPerVet(Map<String, Integer> appointmentsPerVet) {
        this.appointmentsPerVet = appointmentsPerVet;
    }

    public Map<String, Integer> getBookingsPerSitter() {
        return bookingsPerSitter;
    }

    public void setBookingsPerSitter(Map<String, Integer> bookingsPerSitter) {
        this.bookingsPerSitter = bookingsPerSitter;
    }
}

