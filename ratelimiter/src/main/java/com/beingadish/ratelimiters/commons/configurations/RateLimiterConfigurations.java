package com.beingadish.ratelimiters.commons.configurations;

public sealed interface RateLimiterConfigurations permits LeakyBucketConfig, TokenBucketConfig, FixedWindowConfig {
}
