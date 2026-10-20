package com.pawlink.appointment;

import com.pawlink.appointment.dto.AppointmentRequest;
import com.pawlink.appointment.dto.AppointmentResponse;
import com.pawlink.appointment.dto.StatusUpdateRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller exposing Appointment endpoints:
 * - POST /api/appointments
 * - GET  /api/appointments?vetId={id}&date={date} or ?ownerId={id}&date={date} or ?date={date}
 * - PATCH /api/appointments/{id}/status
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * POST /api/appointments: Books a new clinic appointment.
     * Throws checked SlotUnavailableException (maps to HTTP 409 Conflict) if slot overlaps.
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @RequestBody AppointmentRequest request,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) throws SlotUnavailableException {
        if (request.getOwnerId() == null) {
            request.setOwnerId(xUserId);
        }
        Appointment created = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(created));
    }

    /**
     * GET /api/appointments: Retrieves appointments by vetId, ownerId, or date, or all appointments.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @RequestParam(name = "vetId", required = false) Long vetId,
            @RequestParam(name = "date", required = false) String dateParam,
            @RequestParam(name = "ownerId", required = false) Long ownerId,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        LocalDate date = null;
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            if ("today".equalsIgnoreCase(dateParam.trim())) {
                date = LocalDate.now();
            } else if ("tomorrow".equalsIgnoreCase(dateParam.trim())) {
                date = LocalDate.now().plusDays(1);
            } else {
                try {
                    date = LocalDate.parse(dateParam.trim());
                } catch (Exception ignored) {
                    // fall back to null date if unparseable
                }
            }
        }

        List<Appointment> list;

        if (ownerId != null) {
            list = appointmentService.getAppointmentsByOwnerAndDate(ownerId, date);
        } else if (vetId != null) {
            list = appointmentService.getAppointmentsByVetAndDate(vetId, date);
        } else if (xUserId != null) {
            // Fallback to checking owner bookings for the authenticated user
            list = appointmentService.getAppointmentsByOwnerAndDate(xUserId, date);
        } else {
            // Return all appointments (optionally filtered by date)
            list = appointmentService.getAllAppointments(date);
        }

        List<AppointmentResponse> responses = list.stream()
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * PATCH /api/appointments/{id}/status: Updates appointment status (CONFIRMED, COMPLETED, CANCELLED).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        Appointment updated = appointmentService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(AppointmentResponse.from(updated));
    }

    /**
     * GET /api/appointments/{id}: Retrieves appointment by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable("id") Long id) {
        Appointment appt = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(AppointmentResponse.from(appt));
    }
}
