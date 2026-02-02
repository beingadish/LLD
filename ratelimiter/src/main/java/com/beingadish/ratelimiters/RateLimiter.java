package com.beingadish.ratelimiters;

public abstract class RateLimiter {
    public abstract boolean isAllowed(String userId);
}