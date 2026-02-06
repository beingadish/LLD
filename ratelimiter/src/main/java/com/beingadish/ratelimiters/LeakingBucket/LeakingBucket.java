package com.beingadish.ratelimiters.LeakingBucket;

import java.util.function.LongSupplier;

/**
 * A leaking bucket for rate limiting.
 * This class is not thread-safe if used for multiple users in a concurrent environment.
 * It is intended to be used as a state for a single user within a thread-safe rate limiter implementation.
 */
public class LeakingBucket {
    private final double bucketSize;
    private final double outflowRate;
    private final LongSupplier currentTimeSupplier;
    private double filledSize;
    private long lastOutflowTime;

    /**
     * Constructs a new LeakingBucket.
     *
     * @param bucketSize  The maximum number of requests the bucket can hold.
     * @param outflowRate The rate at which requests are processed from the bucket per second.
     */
    public LeakingBucket(double bucketSize, double outflowRate) {
        this(bucketSize, outflowRate, System::nanoTime);
    }

    LeakingBucket(double bucketSize, double outflowRate, LongSupplier currentTimeSupplier) {
        this.bucketSize = bucketSize;
        this.outflowRate = outflowRate;
        this.currentTimeSupplier = currentTimeSupplier;
        this.filledSize = 0;
        this.lastOutflowTime = currentTimeSupplier.getAsLong();
    }

    /**
     * Empties the bucket based on the elapsed time since the last outflow.
     */
    private void empty() {
        long now = currentTimeSupplier.getAsLong();
        double elapsed = (now - lastOutflowTime) / 1_000_000_000.0;
        double outflown = elapsed * outflowRate;
        if (outflown > 0.0) {
            filledSize = Math.max(0.0, filledSize - outflown);
            lastOutflowTime = now;
        }
    }

    /**
     * Tries to add a request to the bucket.
     *
     * @return {@code true} if the request was added, {@code false} otherwise.
     */
    public synchronized boolean tryFilling() {
        empty();
        if (filledSize + 1 <= bucketSize) {
            filledSize += 1;
            return true;
        }
        return false;
    }
}
