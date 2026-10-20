package com.pawlink.admin.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object encapsulating platform-wide metrics and aggregations.
 */
public class SystemStatsDTO {

    private int totalUsers;
    private int totalAppointments;
    private int totalBookings;
    private Map<String, Integer> usersByRole = new HashMap<>();
    private Map<String, Integer> appointmentsPerVet = new HashMap<>();
    private Map<String, Integer> bookingsPerSitter = new HashMap<>();

    public SystemStatsDTO() {
    }

    public SystemStatsDTO(int totalUsers, int totalAppointments, int totalBookings,
                          Map<String, Integer> usersByRole,
                          Map<String, Integer> appointmentsPerVet,
                          Map<String, Integer> bookingsPerSitter) {
        this.totalUsers = totalUsers;
        this.totalAppointments = totalAppointments;
        this.totalBookings = totalBookings;
        this.usersByRole = usersByRole != null ? usersByRole : new HashMap<>();
        this.appointmentsPerVet = appointmentsPerVet != null ? appointmentsPerVet : new HashMap<>();
        this.bookingsPerSitter = bookingsPerSitter != null ? bookingsPerSitter : new HashMap<>();
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

