package com.pawlink.common.exception;

/**
 * Thrown when an appointment slot collides with an existing booking.
 * Maps to HTTP 409 Conflict.
 */
public class SlotUnavailableException extends Exception {
    public SlotUnavailableException(String message) {
        super(message);
    }
}
