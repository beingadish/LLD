package com.beingadish.ratelimiters.FixedWindow;

public class FixedWindowCounter {

    private final long windowSizeInMillis;
    private final long maxRequests;

    private long windowStart;
    private long requestCount;

    public FixedWindowCounter(long windowSizeInMillis, long maxRequests) {
        this.windowSizeInMillis = windowSizeInMillis;
        this.maxRequests = maxRequests;
        this.windowStart = System.currentTimeMillis();
        this.requestCount = 0;
    }

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        if (now - windowStart >= windowSizeInMillis) {
            requestCount = 0;
            windowStart = now;
        }

        if (requestCount < maxRequests) {
            requestCount++;
            return true;
        }

        return false;
    }
}