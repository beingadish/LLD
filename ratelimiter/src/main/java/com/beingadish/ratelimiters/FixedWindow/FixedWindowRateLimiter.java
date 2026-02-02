package com.beingadish.ratelimiters.FixedWindow;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter extends RateLimiter {

    private final long windowSizeInMillis;
    private final long maxRequests;

    private final ConcurrentHashMap<String, FixedWindowCounter> userCounters = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(long windowSizeInMillis, long maxRequests) {
        this.windowSizeInMillis = windowSizeInMillis;
        this.maxRequests = maxRequests;
    }

    @Override
    public boolean isAllowed(String userId) {
        FixedWindowCounter counter = userCounters.computeIfAbsent(userId, uid -> new FixedWindowCounter(windowSizeInMillis, maxRequests));
        return counter.allowRequest();
    }
}
