package com.beingadish.ratelimiters.SlidingWindowLog;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindow {
    private final int maxRequestAllowed;
    private final long windowSizeInMs;

    Deque<Long> requestTimes;

    public SlidingWindow(int maxRequestAllowed, long windowSizeInMs) {
        this.maxRequestAllowed = maxRequestAllowed;
        this.windowSizeInMs = windowSizeInMs;
        requestTimes = new ArrayDeque<>();
    }


    public synchronized boolean accept() {
        long now = System.currentTimeMillis();
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
