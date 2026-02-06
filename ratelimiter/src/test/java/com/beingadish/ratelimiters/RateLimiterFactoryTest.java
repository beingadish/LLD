package com.beingadish.ratelimiters;

import com.beingadish.ratelimiters.FixedWindow.FixedWindowRateLimiter;
import com.beingadish.ratelimiters.LeakingBucket.LeakingBucketRateLimiter;
import com.beingadish.ratelimiters.SlidingWindowCounter.SlidingWindowCounterRateLimiter;
import com.beingadish.ratelimiters.SlidingWindowLog.SlidingWindowRateLimiter;
import com.beingadish.ratelimiters.TokenBucket.TokenBucketRateLimiter;
import com.beingadish.ratelimiters.commons.configurations.FixedWindowConfig;
import com.beingadish.ratelimiters.commons.configurations.LeakyBucketConfig;
import com.beingadish.ratelimiters.commons.configurations.SlidingWindowConfig;
import com.beingadish.ratelimiters.commons.configurations.SlidingWindowCounterConfig;
import com.beingadish.ratelimiters.commons.configurations.TokenBucketConfig;
import junit.framework.TestCase;

public class RateLimiterFactoryTest extends TestCase {

    public void testCreatesTokenBucketLimiter() {
        RateLimiterFactory factory = new RateLimiterFactory();
        RateLimiter limiter = factory.getRateLimiter(new TokenBucketConfig(5L, 2L));

        assertTrue(limiter instanceof TokenBucketRateLimiter);
    }

    public void testCreatesLeakyBucketLimiter() {
        RateLimiterFactory factory = new RateLimiterFactory();
        RateLimiter limiter = factory.getRateLimiter(new LeakyBucketConfig(5L, 2L));

        assertTrue(limiter instanceof LeakingBucketRateLimiter);
    }

    public void testCreatesFixedWindowLimiter() {
        RateLimiterFactory factory = new RateLimiterFactory();
        RateLimiter limiter = factory.getRateLimiter(new FixedWindowConfig(1_000L, 5));

        assertTrue(limiter instanceof FixedWindowRateLimiter);
    }

    public void testCreatesSlidingWindowLogLimiter() {
        RateLimiterFactory factory = new RateLimiterFactory();
        RateLimiter limiter = factory.getRateLimiter(new SlidingWindowConfig(5, 1_000L));

        assertTrue(limiter instanceof SlidingWindowRateLimiter);
    }

    public void testCreatesSlidingWindowCounterLimiter() {
        RateLimiterFactory factory = new RateLimiterFactory();
        RateLimiter limiter = factory.getRateLimiter(new SlidingWindowCounterConfig(5, 1_000L));

        assertTrue(limiter instanceof SlidingWindowCounterRateLimiter);
    }
}
