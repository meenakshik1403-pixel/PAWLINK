package com.pawlink.reminder;

import com.pawlink.common.exception.ResourceNotFoundException;
import com.pawlink.reminder.dto.ReminderRequest;
import com.pawlink.reminder.dto.ReminderResponse;
import com.pawlink.reminder.dto.ReminderRunResponse;
import com.pawlink.user.User;
import com.pawlink.user.UserDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller exposing Reminder and Observer Engine endpoints:
 * - GET  /api/reminders?ownerId={id} or ?petId={id} or ?due=true
 * - GET  /api/reminders/pet/{petId}
 * - POST /api/reminders/run (manually triggers checkDueReminders)
 * - POST /api/reminders (creates new reminder)
 * - GET  /api/reminders/{id}
 */
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderDAO reminderDAO;
    private final ReminderScheduler reminderScheduler;
    private final UserDAO userDAO;

    public ReminderController(ReminderDAO reminderDAO, ReminderScheduler reminderScheduler, UserDAO userDAO) {
        this.reminderDAO = reminderDAO;
        this.reminderScheduler = reminderScheduler;
        this.userDAO = userDAO;
    }

    /**
     * GET /api/reminders:
     * - ?due=true -> returns all due & unsent reminders
     * - ?petId={id} -> returns reminders for a specific pet
     * - ?ownerId={id} -> returns reminders for owner's pets
     * - default / X-User-Id -> returns reminders for current user or all clinic reminders
     */
    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getReminders(
            @RequestParam(name = "due", required = false) Boolean due,
            @RequestParam(name = "ownerId", required = false) Long ownerId,
            @RequestParam(name = "petId", required = false) Long petId,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        List<Reminder> list;

        if (Boolean.TRUE.equals(due)) {
            list = reminderDAO.findDueReminders();
        } else if (petId != null) {
            list = reminderDAO.findByPetId(petId);
        } else if (ownerId != null) {
            list = reminderDAO.findByOwnerId(ownerId);
        } else if (xUserId != null) {
            boolean isOwner = userDAO.findById(xUserId)
                    .map(u -> "OWNER".equalsIgnoreCase(u.getRole()))
                    .orElse(false);
            if (isOwner) {
                list = reminderDAO.findByOwnerId(xUserId);
            } else {
                list = reminderDAO.findAll();
            }
        } else {
            list = reminderDAO.findAll();
        }

        List<ReminderResponse> response = list.stream()
                .map(ReminderResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reminders/pet/{petId}:
     * Returns scheduled reminders for a specific pet.
     */
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<ReminderResponse>> getRemindersByPetId(@PathVariable("petId") Long petId) {
        List<Reminder> list = reminderDAO.findByPetId(petId);
        List<ReminderResponse> response = list.stream()
                .map(ReminderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/reminders/run:
     * Manually triggers the observer reminder engine to check and dispatch all due reminders.
     */
    @PostMapping("/run")
    public ResponseEntity<ReminderRunResponse> runReminders() {
        int count = reminderScheduler.checkDueReminders();
        String msg = count > 0
                ? "Processed and dispatched " + count + " due reminder(s) via Observer notifications."
                : "No pending reminders are currently due.";
        return ResponseEntity.ok(new ReminderRunResponse(msg, count));
    }

    /**
     * POST /api/reminders:
     * Creates a new scheduled reminder for a pet (e.g. Medication, Checkup).
     */
    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(@RequestBody ReminderRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Reminder request body cannot be null.");
        }
        if (req.getPetId() == null || req.getPetId() <= 0) {
            throw new IllegalArgumentException("Valid Pet ID is required.");
        }
        if (req.getType() == null || req.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Reminder type is required (e.g., VACCINATION, MEDICATION, CHECKUP).");
        }
        if (req.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required.");
        }

        Reminder reminder = new Reminder(req.getPetId(), req.getType().trim().toUpperCase(), req.getDueDate());
        Long generatedId = reminderDAO.insert(reminder);

        Reminder saved = reminderDAO.findById(generatedId).orElse(reminder);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReminderResponse.from(saved));
    }

    /**
     * GET /api/reminders/{id}:
     * Retrieves single reminder by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable("id") Long id) {
        Reminder r = reminderDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));
        return ResponseEntity.ok(ReminderResponse.from(r));
    }
}
