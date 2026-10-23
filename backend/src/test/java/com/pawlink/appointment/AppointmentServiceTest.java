package com.pawlink.appointment;

import com.pawlink.appointment.dto.AppointmentRequest;
import com.pawlink.pet.PetDAO;
import com.pawlink.pet.PetFactory;
import com.pawlink.user.UserDAO;
import com.pawlink.user.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long vetId;
    private Long ownerId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM appointments");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var vet = UserFactory.createUser("VET", null, "Dr. Sreekumar", "vet.sreekumar@pawlink.com", "555-0101", "hash", LocalDateTime.now());
        userDAO.insert(vet);
        vetId = vet.getUserId();

        var owner = UserFactory.createUser("OWNER", null, "John Vargese", "john@pawlink.com", "555-0202", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();

        var pet = PetFactory.createPet("Dog", "Bruno", LocalDate.of(2021, 5, 10), ownerId);
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("Schedulable interface default conflictsWith detects interval overlap")
    void testSchedulableInterfaceConflict() {
        Schedulable slot1 = new Schedulable() {
            @Override public LocalDateTime getStart() { return LocalDateTime.of(2026, 10, 25, 10, 0); }
            @Override public LocalDateTime getEnd() { return LocalDateTime.of(2026, 10, 25, 11, 0); }
        };

        // Overlapping slot (10:30 to 11:30)
        Schedulable slot2 = new Schedulable() {
            @Override public LocalDateTime getStart() { return LocalDateTime.of(2026, 10, 25, 10, 30); }
            @Override public LocalDateTime getEnd() { return LocalDateTime.of(2026, 10, 25, 11, 30); }
        };

        // Adjacent non-overlapping slot (11:00 to 12:00)
        Schedulable slot3 = new Schedulable() {
            @Override public LocalDateTime getStart() { return LocalDateTime.of(2026, 10, 25, 11, 0); }
            @Override public LocalDateTime getEnd() { return LocalDateTime.of(2026, 10, 25, 12, 0); }
        };

        assertTrue(slot1.conflictsWith(slot2));
        assertTrue(slot2.conflictsWith(slot1));
        assertFalse(slot1.conflictsWith(slot3));
        assertFalse(slot3.conflictsWith(slot1));
    }

    @Test
    @DisplayName("Should successfully book appointment and return populated details")
    void testBookAppointmentSuccess() throws SlotUnavailableException {
        LocalDateTime start = LocalDateTime.of(2026, 10, 25, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 25, 15, 0);

        AppointmentRequest req = new AppointmentRequest(vetId, petId, ownerId, start, end);
        Appointment booked = appointmentService.bookAppointment(req);

        assertNotNull(booked.getId());
        assertEquals("PENDING", booked.getStatus());
        assertEquals("Dr. Sreekumar", booked.getVetName());
        assertEquals("Bruno", booked.getPetName());
        assertEquals("John Vargese", booked.getOwnerName());
    }

    @Test
    @DisplayName("Double booking the same doctor time slot throws SlotUnavailableException")
    void testDoubleBookingCollision() throws SlotUnavailableException {
        LocalDateTime start = LocalDateTime.of(2026, 10, 25, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 25, 10, 0);

        AppointmentRequest req1 = new AppointmentRequest(vetId, petId, ownerId, start, end);
        appointmentService.bookAppointment(req1);

        // Try booking overlapping slot (9:30 - 10:30)
        AppointmentRequest req2 = new AppointmentRequest(vetId, petId, ownerId,
                LocalDateTime.of(2026, 10, 25, 9, 30),
                LocalDateTime.of(2026, 10, 25, 10, 30));

        assertThrows(SlotUnavailableException.class, () -> appointmentService.bookAppointment(req2));
    }

    @Test
    @DisplayName("Adjacent or cancelled appointments do not collide")
    void testAdjacentAndCancelledSlotsAllowed() throws SlotUnavailableException {
        LocalDateTime start1 = LocalDateTime.of(2026, 10, 25, 9, 0);
        LocalDateTime end1 = LocalDateTime.of(2026, 10, 25, 10, 0);

        AppointmentRequest req1 = new AppointmentRequest(vetId, petId, ownerId, start1, end1);
        Appointment first = appointmentService.bookAppointment(req1);

        // Adjacent slot (10:00 to 11:00) should succeed!
        AppointmentRequest req2 = new AppointmentRequest(vetId, petId, ownerId,
                LocalDateTime.of(2026, 10, 25, 10, 0),
                LocalDateTime.of(2026, 10, 25, 11, 0));
        Appointment second = appointmentService.bookAppointment(req2);
        assertNotNull(second.getId());

        // Cancel first appointment
        appointmentService.updateStatus(first.getId(), "CANCELLED");

        // Now re-booking 9:00 to 10:00 must succeed because previous is CANCELLED
        AppointmentRequest req3 = new AppointmentRequest(vetId, petId, ownerId, start1, end1);
        Appointment third = appointmentService.bookAppointment(req3);
        assertNotNull(third.getId());
    }

    @Test
    @DisplayName("Updating appointment status")
    void testUpdateStatus() throws SlotUnavailableException {
        LocalDateTime start = LocalDateTime.of(2026, 10, 26, 11, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 26, 12, 0);

        Appointment appt = appointmentService.bookAppointment(new AppointmentRequest(vetId, petId, ownerId, start, end));
        assertEquals("PENDING", appt.getStatus());

        Appointment confirmed = appointmentService.updateStatus(appt.getId(), "CONFIRMED");
        assertEquals("CONFIRMED", confirmed.getStatus());

        Appointment completed = appointmentService.updateStatus(appt.getId(), "COMPLETED");
        assertEquals("COMPLETED", completed.getStatus());
    }
}
