package com.beingadish.ratelimiters.commons.configurations;

public record LeakyBucketConfig(Long capacity, Long outflowRate) implements RateLimiterConfigurations {
    public LeakyBucketConfig {

        if (capacity == null || outflowRate == null) {
            throw new NullPointerException("capacity or outflowRate is null");
        }

        if (capacity <= 0 || outflowRate <= 0) {
            throw new IllegalArgumentException("Invalid token bucket config");
        }

    }
}
