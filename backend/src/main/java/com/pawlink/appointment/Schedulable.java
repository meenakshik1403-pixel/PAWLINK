package com.pawlink.appointment;

import java.time.LocalDateTime;

/**
 * Interface representing any schedule-based event in PawLink that can collide with other events.
 * Encapsulates time interval intersection logic.
 */
public interface Schedulable {

    LocalDateTime getStart();

    LocalDateTime getEnd();

    /**
     * Checks if this schedulable item collides with another schedulable item.
     * Overlap condition: start < other.end AND end > other.start
     *
     * @param other another scheduled item
     * @return true if time intervals overlap
     */
    default boolean conflictsWith(Schedulable other) {
        if (other == null || other.getStart() == null || other.getEnd() == null ||
                this.getStart() == null || this.getEnd() == null) {
            return false;
        }
        return this.getStart().isBefore(other.getEnd()) &&
                this.getEnd().isAfter(other.getStart());
    }
}
