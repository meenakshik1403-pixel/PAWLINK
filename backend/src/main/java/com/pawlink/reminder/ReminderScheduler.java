package com.pawlink.reminder;

import com.pawlink.reminder.observer.OwnerReminderObserver;
import com.pawlink.reminder.observer.SitterReminderObserver;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject implementation in the Observer Design Pattern.
 * Manages observers and periodically scans the database for due reminders,
 * broadcasting events to all subscribed listeners and marking rows as sent.
 */
@Service
public class ReminderScheduler implements Subject {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final List<Observer> observers = new CopyOnWriteArrayList<>();
    private final ReminderDAO reminderDAO;

    private final OwnerReminderObserver ownerReminderObserver;
    private final SitterReminderObserver sitterReminderObserver;

    public ReminderScheduler(ReminderDAO reminderDAO,
                             OwnerReminderObserver ownerReminderObserver,
                             SitterReminderObserver sitterReminderObserver) {
        this.reminderDAO = reminderDAO;
        this.ownerReminderObserver = ownerReminderObserver;
        this.sitterReminderObserver = sitterReminderObserver;
    }

    @PostConstruct
    public void initDefaultObservers() {
        if (ownerReminderObserver != null) {
            registerObserver(ownerReminderObserver);
        }
        if (sitterReminderObserver != null) {
            registerObserver(sitterReminderObserver);
        }
        log.info("ReminderScheduler initialized with {} registered observers.", observers.size());
    }

    @Override
    public void registerObserver(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
            log.debug("Registered observer: {}", o.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
        log.debug("Removed observer: {}", o.getClass().getSimpleName());
    }

    @Override
    public void notifyObservers(Reminder reminder) {
        for (Observer observer : observers) {
            try {
                observer.update(reminder);
            } catch (Exception ex) {
                log.error("Error executing observer {}: {}", observer.getClass().getSimpleName(), ex.getMessage(), ex);
            }
        }
    }

    /**
     * Scheduled automated check executed daily at 08:00 AM.
     * Evaluates query: WHERE r.due_date <= CURRENT_DATE AND r.sent = FALSE
     */
    @Scheduled(cron = "${pawlink.reminders.cron:0 0 8 * * ?}")
    @Transactional
    public int checkDueReminders() {
        log.info("Executing scheduled/manual reminder check job...");

        List<Reminder> dueReminders = reminderDAO.findDueReminders();
        log.info("Found {} due reminder(s) eligible for dispatch.", dueReminders.size());

        int count = 0;
        for (Reminder reminder : dueReminders) {
            // 1. Notify all subscribed observers (Owner, Sitter, etc.)
            notifyObservers(reminder);

            // 2. Mark reminder as dispatched in database
            reminderDAO.markAsSent(reminder.getId());
            count++;
        }

        log.info("Successfully dispatched and marked {} reminder(s) as sent.", count);
        return count;
    }

    /**
     * Manual run method directly invokable by REST or Swing clients.
     *
     * @return number of processed and dispatched reminders
     */
    public int runManualCheck() {
        return checkDueReminders();
    }

    public List<Observer> getObservers() {
        return Collections.unmodifiableList(observers);
    }
}

