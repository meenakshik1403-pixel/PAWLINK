package com.pawlink.appointment;

import com.pawlink.appointment.dto.AppointmentRequest;
import com.pawlink.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service managing veterinary clinic appointment bookings and status updates.
 * Implements transaction-level collision detection using {@link Schedulable} intervals.
 */
@Service
public class AppointmentService {

    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");

    private final AppointmentDAO appointmentDAO;

    public AppointmentService(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
    }

    /**
     * Books a new veterinary appointment.
     * Transactional conflict check:
     * 1. Runs hasConflict(vetId, start, end).
     * 2. If true, throws checked SlotUnavailableException.
     * 3. Inserts and returns saved appointment.
     *
     * @param req appointment booking payload
     * @return persisted Appointment with patient, vet, and owner details populated
     * @throws SlotUnavailableException if the selected time slot overlaps with an existing booking
     */
    @Transactional
    public Appointment bookAppointment(AppointmentRequest req) throws SlotUnavailableException {
        if (req == null) {
            throw new IllegalArgumentException("Appointment request cannot be null.");
        }
        if (req.getVetId() == null || req.getVetId() <= 0) {
            throw new IllegalArgumentException("Valid Veterinarian ID is required.");
        }
        if (req.getPetId() == null || req.getPetId() <= 0) {
            throw new IllegalArgumentException("Valid Pet ID is required.");
        }
        if (req.getOwnerId() == null || req.getOwnerId() <= 0) {
            throw new IllegalArgumentException("Valid Pet Owner ID is required.");
        }
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and End time are both required.");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        // 1. Conflict Collision Detection via SQL Overlap Query
        boolean hasConflict = appointmentDAO.hasConflict(req.getVetId(), req.getStartTime(), req.getEndTime());
        if (hasConflict) {
            throw new SlotUnavailableException("Doctor is not available at the selected time slot.");
        }

        // 2. Insert Appointment
        Appointment appointment = new Appointment();
        appointment.setVetId(req.getVetId());
        appointment.setPetId(req.getPetId());
        appointment.setOwnerId(req.getOwnerId());
        appointment.setStartTime(req.getStartTime());
        appointment.setEndTime(req.getEndTime());
        appointment.setStatus("PENDING");

        appointmentDAO.insert(appointment);

        // 3. Return full entity with resolved joins
        return appointmentDAO.findById(appointment.getId()).orElse(appointment);
    }

    /**
     * Updates an appointment's status (CONFIRMED, COMPLETED, CANCELLED).
     *
     * @param id        appointment ID
     * @param newStatus target status
     * @return updated Appointment
     */
    @Transactional
    public Appointment updateStatus(Long id, String newStatus) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Valid appointment ID is required.");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be blank.");
        }

        String normalized = newStatus.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus + ". Valid statuses are: " + VALID_STATUSES);
        }

        Appointment existing = appointmentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        appointmentDAO.updateStatus(id, normalized);
        existing.setStatus(normalized);
        return existing;
    }

    /**
     * Retrieves appointments by veterinarian and optional date.
     */
    public List<Appointment> getAppointmentsByVetAndDate(Long vetId, LocalDate date) {
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Valid Veterinarian ID is required.");
        }
        return appointmentDAO.findByVetAndDate(vetId, date);
    }

    /**
     * Retrieves appointments booked by an owner.
     */
    public List<Appointment> getAppointmentsByOwner(Long ownerId) {
        return getAppointmentsByOwnerAndDate(ownerId, null);
    }

    /**
     * Retrieves appointments booked by an owner, optionally filtered by date.
     */
    public List<Appointment> getAppointmentsByOwnerAndDate(Long ownerId, LocalDate date) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Valid Pet Owner ID is required.");
        }
        return appointmentDAO.findByOwnerIdAndDate(ownerId, date);
    }

    /**
     * Retrieves all platform appointments, optionally filtered by date.
     */
    public List<Appointment> getAllAppointments(LocalDate date) {
        return appointmentDAO.findAll(date);
    }

    /**
     * Retrieves single appointment by ID.
     */
    public Appointment getAppointmentById(Long id) {
        return appointmentDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));
    }
}
