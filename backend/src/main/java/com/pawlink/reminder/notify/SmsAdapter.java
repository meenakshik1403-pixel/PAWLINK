package com.pawlink.reminder.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Concrete Adapter implementing {@link NotificationAdapter} for SMS communication.
 * Simulates cellular carrier SMS dispatch with standard audit logging.
 */
@Component
public class SmsAdapter implements NotificationAdapter {

    private static final Logger log = LoggerFactory.getLogger(SmsAdapter.class);

    @Override
    public void send(String recipient, String subject, String body) {
        String logMessage = "[SMS DISPATCH to " + recipient + "]: " + body;
        System.out.println(logMessage);
        log.info("[SMS DISPATCH to {}]: {} | Subject: {}", recipient, body, subject);
    }
}

