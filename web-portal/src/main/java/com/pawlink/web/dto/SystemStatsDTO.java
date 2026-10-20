package com.pawlink.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.Map;

/**
 * System analytics and aggregation metrics DTO for PawLink Web Portal.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemStatsDTO {

    private int totalUsers;
    private int totalAppointments;
    private int totalBookings;
    private Map<String, Integer> usersByRole;
    private Map<String, Integer> appointmentsPerVet;
    private Map<String, Integer> bookingsPerSitter;

    public SystemStatsDTO() {
        this.usersByRole = Collections.emptyMap();
        this.appointmentsPerVet = Collections.emptyMap();
        this.bookingsPerSitter = Collections.emptyMap();
    }

    public SystemStatsDTO(int totalUsers,
                          int totalAppointments,
                          int totalBookings,
                          Map<String, Integer> usersByRole,
                          Map<String, Integer> appointmentsPerVet,
                          Map<String, Integer> bookingsPerSitter) {
        this.totalUsers = totalUsers;
        this.totalAppointments = totalAppointments;
        this.totalBookings = totalBookings;
        this.usersByRole = usersByRole != null ? usersByRole : Collections.emptyMap();
        this.appointmentsPerVet = appointmentsPerVet != null ? appointmentsPerVet : Collections.emptyMap();
        this.bookingsPerSitter = bookingsPerSitter != null ? bookingsPerSitter : Collections.emptyMap();
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
        this.usersByRole = usersByRole != null ? usersByRole : Collections.emptyMap();
    }

    public Map<String, Integer> getAppointmentsPerVet() {
        return appointmentsPerVet;
    }

    public void setAppointmentsPerVet(Map<String, Integer> appointmentsPerVet) {
        this.appointmentsPerVet = appointmentsPerVet != null ? appointmentsPerVet : Collections.emptyMap();
    }

    public Map<String, Integer> getBookingsPerSitter() {
        return bookingsPerSitter;
    }

    public void setBookingsPerSitter(Map<String, Integer> bookingsPerSitter) {
        this.bookingsPerSitter = bookingsPerSitter != null ? bookingsPerSitter : Collections.emptyMap();
    }
}

