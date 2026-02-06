package com.beingadish.ratelimiters.SlidingWindowLog;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.LongSupplier;

public class SlidingWindow {
    private final int maxRequestAllowed;
    private final long windowSizeInMs;
    private final LongSupplier currentTimeSupplier;
    private final Deque<Long> requestTimes;

    public SlidingWindow(int maxRequestAllowed, long windowSizeInMs) {
        this(maxRequestAllowed, windowSizeInMs, System::currentTimeMillis);
    }

    SlidingWindow(int maxRequestAllowed, long windowSizeInMs, LongSupplier currentTimeSupplier) {
        this.maxRequestAllowed = maxRequestAllowed;
        this.windowSizeInMs = windowSizeInMs;
        this.currentTimeSupplier = currentTimeSupplier;
        this.requestTimes = new ArrayDeque<>();
    }


    public synchronized boolean accept() {
        long now = currentTimeSupplier.getAsLong();
        long windowStart = now - windowSizeInMs;
        while (!requestTimes.isEmpty() && requestTimes.peekFirst() < windowStart) {
            requestTimes.pollFirst();
        }
        if (requestTimes.size() < maxRequestAllowed) {
            requestTimes.addLast(now);
            return true;
        } else {
            requestTimes.addLast(now);
            return false;
        }
    }
}
