package com.beingadish.ratelimiters.commons.configurations;

public record TokenBucketConfig(Long capacity, Long inflowRate) implements RateLimiterConfigurations {

    public TokenBucketConfig {
        if (capacity == null || inflowRate == null) {
            throw new NullPointerException("capacity or inflowRate is null");
        }
        if (capacity <= 0 || inflowRate <= 0) {
            throw new IllegalArgumentException("Invalid token bucket config");
        }
    }
}
