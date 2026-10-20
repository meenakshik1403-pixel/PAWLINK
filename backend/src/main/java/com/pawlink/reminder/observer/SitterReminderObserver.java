package com.pawlink.reminder.observer;

import com.pawlink.reminder.Observer;
import com.pawlink.reminder.Reminder;
import com.pawlink.reminder.notify.EmailAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer that handles alerts for assigned Pet Sitters.
 * Broadcasts critical care and medication routines that sitters must observe.
 */
@Component
public class SitterReminderObserver implements Observer {

    private static final Logger log = LoggerFactory.getLogger(SitterReminderObserver.class);

    private final EmailAdapter emailAdapter;

    public SitterReminderObserver(EmailAdapter emailAdapter) {
        this.emailAdapter = emailAdapter;
    }

    @Override
    public void update(Reminder reminder) {
        if (reminder == null) return;

        // Dispatches sitter alerts when relevant (e.g. daily medication or critical checkup)
        String petName = reminder.getPetName() != null ? reminder.getPetName() : "Assigned Pet";
        String type = reminder.getType() != null ? reminder.getType() : "CARE";

        String logMessage = "[SITTER ALERT]: Daily care reminder for pet " + petName +
                " (" + type + ") on " + reminder.getDueDate();
        System.out.println(logMessage);
        log.info(logMessage);

        // If specific pet sitters are subscribed to pet alerts
        if ("MEDICATION".equalsIgnoreCase(type)) {
            emailAdapter.send("sitter-network@pawlink.com",
                    "CRITICAL: Medication Due for " + petName,
                    "Pet Sitter Attention Required: " + petName + " is scheduled for medication on " + reminder.getDueDate());
        }
    }
}

