package com.beingadish.ratelimiters.LeakingBucket;

public class LeakingBucket {
    double bucketSize;
    double outflowRate;
    double filledSize;
    long lastOutflowTime;

    public LeakingBucket(double bucketSize, double outflowRate) {
        this.bucketSize = bucketSize;
        this.outflowRate = outflowRate;
        filledSize = 0;
        lastOutflowTime = System.nanoTime();
    }


    private void empty() {
        long now = System.nanoTime();
        double elapsed = (now - lastOutflowTime) / 1_000_000_000.0;
        double outflown = elapsed * outflowRate;
        if (outflown > 0.0) {
            filledSize = Math.max(0.0, filledSize - outflown);
            lastOutflowTime = now;
        }
    }

    public synchronized boolean tryFilling() {
        empty();
        if (filledSize + 1 <= bucketSize) {
            filledSize += 1;
            return true;
        }
        return false;
    }
}
