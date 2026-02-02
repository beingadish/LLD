package com.beingadish.ratelimiters.FixedWindow;

/**
 * A simple fixed window counter for rate limiting.
 * This class is not thread-safe if used for multiple users in a concurrent environment.
 * It is intended to be used as a state for a single user within a thread-safe rate limiter implementation.
 */
public class FixedWindowCounter {

    private final long windowSizeInMillis;
    private final long maxRequests;

    private long windowStart;
    private long requestCount;

    /**
     * Constructs a new FixedWindowCounter.
     *
     * @param windowSizeInMillis The size of the time window in milliseconds.
     * @param maxRequests        The maximum number of requests allowed in a window.
     */
    public FixedWindowCounter(long windowSizeInMillis, long maxRequests) {
        this.windowSizeInMillis = windowSizeInMillis;
        this.maxRequests = maxRequests;
        this.windowStart = System.currentTimeMillis();
        this.requestCount = 0;
    }

    /**
     * Checks if a request is allowed.
     * If the current window has expired, a new window is started.
     *
     * @return {@code true} if the request is allowed, {@code false} otherwise.
     */
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