package com.example.cranberrysoft.util;

/**
 * Generates actual time
 *
 * @author Mariusz Dubielecki
 */
public interface AtomClock {

    /**
     * Get actual time in ms
     * @return
     */
    long now();
}
