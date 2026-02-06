package com.beingadish.ratelimiters.SlidingWindowCounter;

import junit.framework.TestCase;

import java.util.function.LongSupplier;

public class SlidingWindowCounterTest extends TestCase {

    public void testRejectsBurstInSameWindow() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(100L);
        SlidingWindowCounter counter = new SlidingWindowCounter(2, 1_000L, timeSupplier);

        assertTrue(counter.accept());
        assertTrue(counter.accept());
        assertFalse(counter.accept());
    }

    public void testUsesWeightedPreviousWindowCount() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(100L);
        SlidingWindowCounter counter = new SlidingWindowCounter(5, 1_000L, timeSupplier);

        assertTrue(counter.accept());
        assertTrue(counter.accept());
        assertTrue(counter.accept());
        assertTrue(counter.accept());
        assertTrue(counter.accept());
        assertFalse(counter.accept());

        timeSupplier.setCurrentTime(1_100L);
        assertTrue(counter.accept());
        assertFalse(counter.accept());

        timeSupplier.setCurrentTime(2_100L);
        assertTrue(counter.accept());
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
