package com.beingadish.ratelimiters.TokenBucket;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter extends RateLimiter {
    long capacity;
    long refillRate;

    public TokenBucketRateLimiter(long capacity, long refillRate) {
        this.refillRate = refillRate;
        this.capacity = capacity;
    }

    ConcurrentHashMap<String, TokenBucket> userBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean isAllowed(String userId) {
        TokenBucket bucket = userBuckets.computeIfAbsent(userId, uid -> new TokenBucket(capacity, refillRate));
        return bucket.tryConsume();
    }
}
