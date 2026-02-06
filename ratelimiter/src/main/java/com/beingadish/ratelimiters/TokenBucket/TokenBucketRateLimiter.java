package com.beingadish.ratelimiters.TokenBucket;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A rate limiter that uses a token bucket algorithm.
 * This implementation is thread-safe.
 */
public class TokenBucketRateLimiter extends RateLimiter {
    private final long capacity;
    private final long refillRate;

    /**
     * Constructs a new TokenBucketRateLimiter.
     *
     * @param capacity   The maximum number of tokens the bucket can hold.
     * @param refillRate The rate at which tokens are added to the bucket per second.
     */
    public TokenBucketRateLimiter(long capacity, long refillRate) {
        this.refillRate = refillRate;
        this.capacity = capacity;
    }

    private final ConcurrentHashMap<String, TokenBucket> userBuckets = new ConcurrentHashMap<>();

    /**
     * Checks if a request is allowed for a given user.
     *
     * @param userId The ID of the user making the request.
     * @return {@code true} if the request is allowed, {@code false} otherwise.
     */
    @Override
    public boolean isAllowed(String userId) {
        TokenBucket bucket = userBuckets.computeIfAbsent(userId, uid -> new TokenBucket(capacity, refillRate));
        return bucket.tryConsume();
    }
}
