package com.pawlink.vet.base;

/**
 * Interface implemented by panels that can automatically refresh their data
 * when navigated to or displayed in the application shell.
 */
public interface Refreshable {
    /**
     * Trigger an asynchronous refresh / reload of the view's data.
     */
    void refresh();
}

