package com.beingadish.ratelimiters.LeakingBucket;

import com.beingadish.ratelimiters.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

public class LeakingBucketRateLimiter extends RateLimiter {
    long capacity;
    double outflowRate;

    ConcurrentHashMap<String, LeakingBucket> usageQueue = new ConcurrentHashMap<String, LeakingBucket>();

    public LeakingBucketRateLimiter(long capacity, double outflowRate) {
        this.capacity = capacity;
        this.outflowRate = outflowRate;
    }

    @Override
    public boolean isAllowed(String userId) {
        LeakingBucket queue = usageQueue.computeIfAbsent(userId, uid -> new LeakingBucket((double) capacity, outflowRate));
        return queue.tryFilling();
    }
}
