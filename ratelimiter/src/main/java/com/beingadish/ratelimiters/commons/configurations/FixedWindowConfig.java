package com.beingadish.ratelimiters.commons.configurations;

public record FixedWindowConfig(Long windowSizeInMs, Integer requestAllowed) implements RateLimiterConfigurations {
    public FixedWindowConfig {
        if (windowSizeInMs == null) {
            throw new NullPointerException("Window Size is null");
        }

        if (windowSizeInMs <= 0) {
            throw new IllegalArgumentException("Window Size is Invalid");
        }

        if (requestAllowed == null) {
            throw new NullPointerException("Request Limit is null");
        }

        if (requestAllowed <= 0) {
            throw new IllegalArgumentException("Request Limit is Invalid");
        }
    }
}
