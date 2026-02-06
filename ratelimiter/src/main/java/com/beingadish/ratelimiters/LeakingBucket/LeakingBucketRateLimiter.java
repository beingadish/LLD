package com.beingadish.ratelimiters.LeakingBucket;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A rate limiter that uses a leaking bucket algorithm.
 * This implementation is thread-safe.
 */
public class LeakingBucketRateLimiter extends RateLimiter {
    private final long capacity;
    private final double outflowRate;

    private final ConcurrentHashMap<String, LeakingBucket> usageQueue = new ConcurrentHashMap<>();

    /**
     * Constructs a new LeakingBucketRateLimiter.
     *
     * @param capacity    The maximum number of requests the bucket can hold.
     * @param outflowRate The rate at which requests are processed from the bucket per second.
     */
    public LeakingBucketRateLimiter(long capacity, double outflowRate) {
        this.capacity = capacity;
        this.outflowRate = outflowRate;
    }

    /**
     * Checks if a request is allowed for a given user.
     *
     * @param userId The ID of the user making the request.
     * @return {@code true} if the request is allowed, {@code false} otherwise.
     */
    @Override
    public boolean isAllowed(String userId) {
        LeakingBucket queue = usageQueue.computeIfAbsent(userId, uid -> new LeakingBucket((double) capacity, outflowRate));
        return queue.tryFilling();
    }
}
