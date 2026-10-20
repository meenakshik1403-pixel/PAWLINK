package com.pawlink.reminder.notify;

/**
 * Target interface for the Adapter Design Pattern.
 * Unifies diverse communication channels (Email, SMS, Push) under a single contract.
 */
public interface NotificationAdapter {
    void send(String recipient, String subject, String body);
}

