package com.pawlink.reminder.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Concrete Adapter implementing {@link NotificationAdapter} for Email communication.
 * Simulates outgoing SMTP/Email dispatch with standard audit logging.
 */
@Component
public class EmailAdapter implements NotificationAdapter {

    private static final Logger log = LoggerFactory.getLogger(EmailAdapter.class);

    @Override
    public void send(String recipient, String subject, String body) {
        String logMessage = "[EMAIL DISPATCH to " + recipient + "]: " + body;
        System.out.println(logMessage);
        log.info("[EMAIL DISPATCH to {}]: {} | Subject: {}", recipient, body, subject);
    }
}

