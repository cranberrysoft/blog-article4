package com.example.cranberrysoft.util;

import java.time.Instant;

/**
 * Simple Java implementation return the number of milliseconds since the epoch of 1970-01-01T00:00:00Z
 *
 * @author Mariusz Dubielecki
 */
public class JavaClock implements AtomClock {

    @Override
    public long now() {
        return Instant.now().toEpochMilli();
    }
}
