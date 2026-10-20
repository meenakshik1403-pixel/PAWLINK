package com.pawlink.reminder.observer;

import com.pawlink.reminder.Observer;
import com.pawlink.reminder.Reminder;
import com.pawlink.reminder.notify.EmailAdapter;
import com.pawlink.reminder.notify.SmsAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer that handles notifications targeted at Pet Owners.
 * Uses {@link EmailAdapter} and {@link SmsAdapter} to deliver multi-channel alerts.
 */
@Component
public class OwnerReminderObserver implements Observer {

    private static final Logger log = LoggerFactory.getLogger(OwnerReminderObserver.class);

    private final EmailAdapter emailAdapter;
    private final SmsAdapter smsAdapter;

    public OwnerReminderObserver(EmailAdapter emailAdapter, SmsAdapter smsAdapter) {
        this.emailAdapter = emailAdapter;
        this.smsAdapter = smsAdapter;
    }

    @Override
    public void update(Reminder reminder) {
        if (reminder == null) return;

        String petName = reminder.getPetName() != null ? reminder.getPetName() : "Your pet";
        String type = reminder.getType() != null ? reminder.getType() : "CARE";
        String dueDateStr = reminder.getDueDate() != null ? reminder.getDueDate().toString() : "today";

        String subject = "PawLink Alert: " + type + " Due for " + petName;
        String body = "Dear " + (reminder.getOwnerName() != null ? reminder.getOwnerName() : "Pet Owner") +
                ", " + petName + " has a scheduled " + type + " due on " + dueDateStr +
                ". Please ensure appropriate care or schedule a clinic visit.";

        // 1. Dispatch Email
        if (reminder.getOwnerEmail() != null && !reminder.getOwnerEmail().isBlank()) {
            emailAdapter.send(reminder.getOwnerEmail(), subject, body);
        }

        // 2. Dispatch SMS
        if (reminder.getOwnerPhone() != null && !reminder.getOwnerPhone().isBlank()) {
            String shortSms = "PawLink: " + petName + " has a " + type + " due on " + dueDateStr + ".";
            smsAdapter.send(reminder.getOwnerPhone(), subject, shortSms);
        }

        log.info("OwnerReminderObserver notified for reminder ID: {}", reminder.getId());
    }
}

