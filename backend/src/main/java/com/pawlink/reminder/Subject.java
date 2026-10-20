package com.pawlink.reminder;

/**
 * Subject interface for the Observer Design Pattern.
 * Defines contract for managing and broadcasting reminder events to registered Observers.
 */
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(Reminder reminder);
}

