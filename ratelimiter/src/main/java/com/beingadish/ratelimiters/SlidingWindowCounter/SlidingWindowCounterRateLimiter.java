package com.beingadish.ratelimiters.SlidingWindowCounter;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A rate limiter that uses the sliding window counter algorithm.
 * This implementation is thread-safe.
 */
public class SlidingWindowCounterRateLimiter extends RateLimiter {

    private final int maxRequestsAllowed;
    private final long windowSizeInMs;
    private final ConcurrentHashMap<String, SlidingWindowCounter> userWindows = new ConcurrentHashMap<>();

    public SlidingWindowCounterRateLimiter(Integer maxRequestsAllowed, Long windowSizeInMs) {
        this.maxRequestsAllowed = maxRequestsAllowed;
        this.windowSizeInMs = windowSizeInMs;
    }

    @Override
    public boolean isAllowed(String userId) {
        SlidingWindowCounter counter = userWindows.computeIfAbsent(userId, uid -> new SlidingWindowCounter(maxRequestsAllowed, windowSizeInMs));
        return counter.accept();
    }
}
