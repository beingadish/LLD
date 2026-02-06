package com.beingadish.ratelimiters.SlidingWindowCounter;

import java.util.function.LongSupplier;

/**
 * A sliding window counter implementation for a single user.
 * This class combines the current fixed window count with a weighted previous window count.
 */
public class SlidingWindowCounter {
    private final int maxRequestsAllowed;
    private final long windowSizeInMs;
    private final LongSupplier currentTimeSupplier;

    private long currentWindowStart;
    private long currentWindowCount;
    private long previousWindowCount;

    public SlidingWindowCounter(int maxRequestsAllowed, long windowSizeInMs) {
        this(maxRequestsAllowed, windowSizeInMs, System::currentTimeMillis);
    }

    SlidingWindowCounter(int maxRequestsAllowed, long windowSizeInMs, LongSupplier currentTimeSupplier) {
        this.maxRequestsAllowed = maxRequestsAllowed;
        this.windowSizeInMs = windowSizeInMs;
        this.currentTimeSupplier = currentTimeSupplier;
        long now = currentTimeSupplier.getAsLong();
        this.currentWindowStart = now - (now % windowSizeInMs);
        this.currentWindowCount = 0;
        this.previousWindowCount = 0;
    }

    /**
     * Checks if a request can be accepted based on weighted count from current and previous windows.
     *
     * @return {@code true} when request is accepted, {@code false} otherwise.
     */
    public synchronized boolean accept() {
        long now = currentTimeSupplier.getAsLong();
        rotateWindowIfRequired(now);

        long elapsedInCurrentWindow = now - currentWindowStart;
        double previousWindowWeight = (double) (windowSizeInMs - elapsedInCurrentWindow) / windowSizeInMs;
        double effectiveRequestCount = currentWindowCount + (previousWindowCount * previousWindowWeight);

        if (effectiveRequestCount < maxRequestsAllowed) {
            currentWindowCount++;
            return true;
        }

        return false;
    }

    private void rotateWindowIfRequired(long now) {
        if (now < currentWindowStart + windowSizeInMs) {
            return;
        }

        long windowsPassed = (now - currentWindowStart) / windowSizeInMs;
        if (windowsPassed == 1) {
            previousWindowCount = currentWindowCount;
        } else {
            previousWindowCount = 0;
        }

        currentWindowCount = 0;
        currentWindowStart += windowsPassed * windowSizeInMs;
    }
}
