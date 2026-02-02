package com.beingadish.ratelimiters.commons.configurations;

public record SlidingWindowConfig(Integer maxRequestsAllowed,
                                  Long windowSizeInMs) implements RateLimiterConfigurations {
    public SlidingWindowConfig {
        if (maxRequestsAllowed == null) {
            throw new NullPointerException("maxRequestsAllowed cannot be null");
        }
        if (maxRequestsAllowed <= 0) {
            throw new IllegalArgumentException("maxRequestsAllowed should be greater than 0");
        }
        if (windowSizeInMs == null) {
            throw new NullPointerException("windowSizeInMs cannot be null");
        }
        if (windowSizeInMs <= 0) {
            throw new IllegalArgumentException("windowSizeInMs should be greater than 0");
        }
    }
}
