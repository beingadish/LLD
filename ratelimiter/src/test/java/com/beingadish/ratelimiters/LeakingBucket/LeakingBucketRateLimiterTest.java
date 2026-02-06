package com.beingadish.ratelimiters.LeakingBucket;

import junit.framework.TestCase;

public class LeakingBucketRateLimiterTest extends TestCase {

    public void testTracksUsersIndependently() {
        LeakingBucketRateLimiter limiter = new LeakingBucketRateLimiter(1L, 1.0);

        assertTrue(limiter.isAllowed("userA"));
        assertFalse(limiter.isAllowed("userA"));
        assertTrue(limiter.isAllowed("userB"));
    }
}
