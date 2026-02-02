package com.beingadish.ratelimiters.TokenBucket;

/**
 * A token bucket for rate limiting.
 * This class is not thread-safe if used for multiple users in a concurrent environment.
 * It is intended to be used as a state for a single user within a thread-safe rate limiter implementation.
 */
public class TokenBucket {
    double capacity;
    double tokens;
    double refillRate;
    long lastRefillTimestamp;

    /**
     * Constructs a new TokenBucket.
     *
     * @param capacity   The maximum number of tokens the bucket can hold.
     * @param refillRate The rate at which tokens are added to the bucket per second.
     */
    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.nanoTime();
    }

    /**
     * Refills the bucket with tokens based on the elapsed time since the last refill.
     */
    public void refill() {
        long now = System.nanoTime();
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
