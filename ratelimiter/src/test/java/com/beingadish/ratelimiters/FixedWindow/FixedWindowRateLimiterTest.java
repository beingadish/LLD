package com.beingadish.ratelimiters.FixedWindow;

import junit.framework.TestCase;

public class FixedWindowRateLimiterTest extends TestCase {

    public void testTracksUsersIndependently() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(10_000L, 1L);

        assertTrue(limiter.isAllowed("userA"));
        assertFalse(limiter.isAllowed("userA"));
        assertTrue(limiter.isAllowed("userB"));
    }
}
