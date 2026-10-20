package com.pawlink.reminder;

/**
 * Observer interface for the Observer Design Pattern.
 * Receives broadcast notifications when a reminder becomes due or triggered.
 */
public interface Observer {
    void update(Reminder reminder);
}

