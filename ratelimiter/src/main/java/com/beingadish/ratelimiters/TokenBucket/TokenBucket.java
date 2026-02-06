package com.beingadish.ratelimiters.TokenBucket;

import java.util.function.LongSupplier;

/**
 * A token bucket for rate limiting.
 * This class is not thread-safe if used for multiple users in a concurrent environment.
 * It is intended to be used as a state for a single user within a thread-safe rate limiter implementation.
 */
public class TokenBucket {
    private final double capacity;
    private final double refillRate;
    private final LongSupplier currentTimeSupplier;
    private double tokens;
    private long lastRefillTimestamp;

    /**
     * Constructs a new TokenBucket.
     *
     * @param capacity   The maximum number of tokens the bucket can hold.
     * @param refillRate The rate at which tokens are added to the bucket per second.
     */
    public TokenBucket(long capacity, long refillRate) {
        this(capacity, refillRate, System::nanoTime);
    }

    TokenBucket(long capacity, long refillRate, LongSupplier currentTimeSupplier) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.currentTimeSupplier = currentTimeSupplier;
        this.tokens = capacity;
        this.lastRefillTimestamp = currentTimeSupplier.getAsLong();
    }

    /**
     * Refills the bucket with tokens based on the elapsed time since the last refill.
     */
    private void refill() {
        long now = currentTimeSupplier.getAsLong();
        double seconds = (double) (now - lastRefillTimestamp) / 1_000_000_000;
        double tokensToAdd = seconds * refillRate;
        if (tokensToAdd > 0.0) {
            tokens = Math.min(tokens + tokensToAdd, capacity);
            lastRefillTimestamp = now;
        }
    }

    /**
     * Tries to consume a token from the bucket.
     *
     * @return {@code true} if a token was consumed, {@code false} otherwise.
     */
    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }
}
