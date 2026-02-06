package com.beingadish.ratelimiters.TokenBucket;

import junit.framework.TestCase;

import java.util.function.LongSupplier;

public class TokenBucketTest extends TestCase {

    public void testRejectsAfterBucketIsDrained() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(0L);
        TokenBucket bucket = new TokenBucket(2L, 1L, timeSupplier);

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    public void testRefillsOverTime() {
        MutableTimeSupplier timeSupplier = new MutableTimeSupplier(0L);
        TokenBucket bucket = new TokenBucket(2L, 1L, timeSupplier);

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());

        timeSupplier.setCurrentTime(500_000_000L);
        assertFalse(bucket.tryConsume());

        timeSupplier.setCurrentTime(1_000_000_000L);
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
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
