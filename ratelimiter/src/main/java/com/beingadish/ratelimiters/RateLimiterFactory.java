package com.beingadish.ratelimiters;

import com.beingadish.ratelimiters.LeakingBucket.LeakingBucketRateLimiter;
import com.beingadish.ratelimiters.TokenBucket.TokenBucketRateLimiter;
import com.beingadish.ratelimiters.commons.configurations.LeakyBucketConfig;
import com.beingadish.ratelimiters.commons.configurations.RateLimiterConfigurations;
import com.beingadish.ratelimiters.commons.configurations.TokenBucketConfig;

public class RateLimiterFactory {
    public RateLimiter getRateLimiter(RateLimiterConfigurations config) {
        if (config instanceof TokenBucketConfig tb) {
            return new TokenBucketRateLimiter(tb.capacity(), tb.inflowRate());
        }

        if (config instanceof LeakyBucketConfig lb) {
            return new LeakingBucketRateLimiter(lb.capacity(), lb.outflowRate());
        }

        throw new IllegalArgumentException("Unsupported RateLimiter configuration");
    }
}