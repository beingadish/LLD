package com.beingadish.ratelimiters.SlidingWindowLog;

import junit.framework.TestCase;

import java.util.function.LongSupplier;

public class SlidingWindowTest extends TestCase {

    public void testRejectsRequestAfterLimitReached() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(1_000L);
        SlidingWindow slidingWindow = new SlidingWindow(2, 1_000L, timeSupplier);

        assertTrue(slidingWindow.accept());
        timeSupplier.setCurrentTime(1_100L);
        assertTrue(slidingWindow.accept());
        timeSupplier.setCurrentTime(1_200L);
        assertFalse(slidingWindow.accept());
    }

    public void testExpiresOutdatedTimestamps() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(1_000L);
        SlidingWindow slidingWindow = new SlidingWindow(2, 1_000L, timeSupplier);

        assertTrue(slidingWindow.accept());
        timeSupplier.setCurrentTime(1_100L);
        assertTrue(slidingWindow.accept());
        timeSupplier.setCurrentTime(1_200L);
        assertFalse(slidingWindow.accept());

        timeSupplier.setCurrentTime(2_201L);
        assertTrue(slidingWindow.accept());
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
