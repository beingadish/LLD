package com.beingadish.ratelimiters.FixedWindow;

import junit.framework.TestCase;

import java.util.function.LongSupplier;

public class FixedWindowCounterTest extends TestCase {

    public void testRejectsWhenLimitReachedWithinSameWindow() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(1_000L);
        FixedWindowCounter counter = new FixedWindowCounter(1_000L, 2L, timeSupplier);

        assertTrue(counter.allowRequest());
        assertTrue(counter.allowRequest());
        assertFalse(counter.allowRequest());
    }

    public void testAllowsAfterWindowReset() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(1_000L);
        FixedWindowCounter counter = new FixedWindowCounter(1_000L, 1L, timeSupplier);

        assertTrue(counter.allowRequest());
        assertFalse(counter.allowRequest());

        timeSupplier.setCurrentTime(2_001L);
        assertTrue(counter.allowRequest());
    }

    private static final class MutableTimeSupplier implements LongSupplier {
        private long currentTime;

        private MutableTimeSupplier(long currentTime) {
            this.currentTime = currentTime;
        }

        private void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public long getAsLong() {
            return currentTime;
        }
    }
}
