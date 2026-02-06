package com.beingadish.ratelimiters.TokenBucket;

import junit.framework.TestCase;

public class TokenBucketRateLimiterTest extends TestCase {

    public void testTracksUsersIndependently() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1L, 1L);

        assertTrue(limiter.isAllowed("userA"));
        assertFalse(limiter.isAllowed("userA"));
        assertTrue(limiter.isAllowed("userB"));
    }
}
