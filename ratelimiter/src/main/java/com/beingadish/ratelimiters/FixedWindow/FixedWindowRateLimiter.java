package com.beingadish.ratelimiters.FixedWindow;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A rate limiter that uses a fixed window algorithm.
 * This implementation is thread-safe.
 */
public class FixedWindowRateLimiter extends RateLimiter {

    private final long windowSizeInMillis;
    private final long maxRequests;

    private final ConcurrentHashMap<String, FixedWindowCounter> userCounters = new ConcurrentHashMap<>();

    /**
     * Constructs a new FixedWindowRateLimiter.
     *
     * @param windowSizeInMillis The size of the time window in milliseconds.
     * @param maxRequests        The maximum number of requests allowed in a window.
     */
    public FixedWindowRateLimiter(long windowSizeInMillis, long maxRequests) {
        this.windowSizeInMillis = windowSizeInMillis;
        this.maxRequests = maxRequests;
    }

    /**
     * Checks if a request is allowed for a given user.
     *
     * @param userId The ID of the user making the request.
     * @return {@code true} if the request is allowed, {@code false} otherwise.
     */
    @Override
    public boolean isAllowed(String userId) {
        FixedWindowCounter counter = userCounters.computeIfAbsent(userId, uid -> new FixedWindowCounter(windowSizeInMillis, maxRequests));
        return counter.allowRequest();
    }
}
