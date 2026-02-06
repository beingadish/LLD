package com.beingadish.ratelimiters.LeakingBucket;

import junit.framework.TestCase;

import java.util.function.LongSupplier;

public class LeakingBucketTest extends TestCase {

    public void testRejectsWhenBucketIsFull() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(0L);
        LeakingBucket bucket = new LeakingBucket(2.0, 2.0, timeSupplier);

        assertTrue(bucket.tryFilling());
        assertTrue(bucket.tryFilling());
        assertFalse(bucket.tryFilling());
    }

    public void testAllowsAfterOutflow() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(0L);
        LeakingBucket bucket = new LeakingBucket(2.0, 2.0, timeSupplier);

        assertTrue(bucket.tryFilling());
        assertTrue(bucket.tryFilling());
        assertFalse(bucket.tryFilling());

        timeSupplier.setCurrentTime(1_000_000_000L);
        assertTrue(bucket.tryFilling());
        assertTrue(bucket.tryFilling());
        assertFalse(bucket.tryFilling());
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
