package com.pawlink.reminder;

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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ReminderSchedulerTest {

    @Autowired
    private ReminderScheduler reminderScheduler;

    @Autowired
    private ReminderDAO reminderDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long ownerId;
    private Long petId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM appointments");
        jdbcTemplate.update("DELETE FROM reminders");
        jdbcTemplate.update("DELETE FROM vaccinations");
        jdbcTemplate.update("DELETE FROM medical_records");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");

        var owner = UserFactory.createUser("OWNER", null, "John Doe", "john@pawlink.com", "+1-555-0202", "hash", LocalDateTime.now());
        userDAO.insert(owner);
        ownerId = owner.getUserId();

        var pet = PetFactory.createPet("Dog", "Max", LocalDate.of(2022, 4, 1), ownerId);
        petDAO.insert(pet);
        petId = pet.getPetId();
    }

    @Test
    @DisplayName("Observer Pattern: Observers can register, receive notifications, and unregister from Subject")
    void testObserverRegistrationAndNotification() {
        List<Reminder> received = new ArrayList<>();
        Observer testObserver = received::add;

        // Register
        reminderScheduler.registerObserver(testObserver);
        assertTrue(reminderScheduler.getObservers().contains(testObserver));

        // Notify
        Reminder r = new Reminder(petId, "VACCINATION", LocalDate.now());
        r.setPetName("Max");
        r.setOwnerName("John Doe");
        reminderScheduler.notifyObservers(r);

        assertEquals(1, received.size());
        assertEquals("Max", received.get(0).getPetName());
        assertEquals("VACCINATION", received.get(0).getType());

        // Unregister
        reminderScheduler.removeObserver(testObserver);
        assertFalse(reminderScheduler.getObservers().contains(testObserver));
    }

    @Test
    @DisplayName("Scheduler: checkDueReminders finds overdue/due rows, notifies observers, and sets sent=true")
    void testCheckDueRemindersExecution() {
        // Reminder 1: Overdue (yesterday) -> should be processed
        Reminder overdue = new Reminder(petId, "VACCINATION", LocalDate.now().minusDays(1));
        reminderDAO.insert(overdue);

        // Reminder 2: Due today -> should be processed
        Reminder dueToday = new Reminder(petId, "MEDICATION", LocalDate.now());
        reminderDAO.insert(dueToday);

        // Reminder 3: Future due date -> should NOT be processed
        Reminder future = new Reminder(petId, "CHECKUP", LocalDate.now().plusDays(5));
        reminderDAO.insert(future);

        List<Reminder> processed = new ArrayList<>();
        Observer mockObserver = processed::add;
        reminderScheduler.registerObserver(mockObserver);

        try {
            int count = reminderScheduler.checkDueReminders();
            assertEquals(2, count, "Only overdue and due-today reminders must be processed.");
            assertEquals(2, processed.size());

            // Verify database state: due reminders marked as sent=true
            List<Reminder> remainingDue = reminderDAO.findDueReminders();
            assertEquals(0, remainingDue.size(), "No due reminders should remain unsent.");

            // Verify future reminder still untouched (sent=false)
            Reminder futureCheck = reminderDAO.findById(future.getId()).orElseThrow();
            assertFalse(futureCheck.isSent(), "Future reminder must remain unsent.");

            // Verify overdue reminder is now marked sent=true
            Reminder overdueCheck = reminderDAO.findById(overdue.getId()).orElseThrow();
            assertTrue(overdueCheck.isSent(), "Processed reminder must have sent = true.");
        } finally {
            reminderScheduler.removeObserver(mockObserver);
        }
    }

    @Test
    @DisplayName("DAO: findByOwnerId retrieves all reminders for owner's pets")
    void testFindByOwnerId() {
        Reminder r1 = new Reminder(petId, "VACCINATION", LocalDate.now().plusDays(2));
        Reminder r2 = new Reminder(petId, "MEDICATION", LocalDate.now().plusDays(10));
        reminderDAO.insert(r1);
        reminderDAO.insert(r2);

        List<Reminder> ownerReminders = reminderDAO.findByOwnerId(ownerId);
        assertEquals(2, ownerReminders.size());
        assertEquals("Max", ownerReminders.get(0).getPetName());
        assertEquals("John Doe", ownerReminders.get(0).getOwnerName());
    }
}

