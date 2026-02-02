package com.beingadish.ratelimiters;

import com.beingadish.ratelimiters.FixedWindow.FixedWindowRateLimiter;
import com.beingadish.ratelimiters.LeakingBucket.LeakingBucketRateLimiter;
import com.beingadish.ratelimiters.TokenBucket.TokenBucketRateLimiter;
import com.beingadish.ratelimiters.commons.configurations.FixedWindowConfig;
import com.beingadish.ratelimiters.commons.configurations.LeakyBucketConfig;
import com.beingadish.ratelimiters.commons.configurations.RateLimiterConfigurations;
import com.beingadish.ratelimiters.commons.configurations.TokenBucketConfig;

public class RateLimiterFactory {
    public RateLimiter getRateLimiter(RateLimiterConfigurations config) {
        if (config instanceof TokenBucketConfig(var capacity, var inflowRate)) {
            return new TokenBucketRateLimiter(capacity, inflowRate);
        }

        if (config instanceof LeakyBucketConfig(var capacity, var outflowRate)) {
            return new LeakingBucketRateLimiter(capacity, outflowRate);
        }

        if(config instanceof FixedWindowConfig(var windowSizeInMs, var allowedRequests)){
            return new FixedWindowRateLimiter(windowSizeInMs, allowedRequests);
        }

        throw new IllegalArgumentException("Unsupported RateLimiter configuration");
    }
}
