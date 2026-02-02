package com.beingadish.ratelimiters.SlidingWindowLog;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowRateLimiter extends RateLimiter {

    private final int maxRequestsAllowed;
    private final long windowSizeInMs;
    ConcurrentHashMap<String, SlidingWindow> userWindows = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(Integer maxRequestsAllowed, Long windowSizeInMs) {
        this.maxRequestsAllowed = maxRequestsAllowed;
        this.windowSizeInMs = windowSizeInMs;
    }

    @Override
    public boolean isAllowed(String userId) {
        SlidingWindow slidingWindow = userWindows.computeIfAbsent(userId, uid -> new SlidingWindow(maxRequestsAllowed, windowSizeInMs));
        return slidingWindow.accept();
    }
}
