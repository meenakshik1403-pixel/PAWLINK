package com.pawlink.appointment;

/**
 * Checked exception thrown when an appointment booking conflicts with an existing slot.
 * Maps to HTTP 409 Conflict.
 */
public class SlotUnavailableException extends com.pawlink.common.exception.SlotUnavailableException {

    public SlotUnavailableException(String message) {
        super(message);
    }
}
