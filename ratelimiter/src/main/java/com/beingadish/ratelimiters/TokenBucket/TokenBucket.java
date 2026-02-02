package com.beingadish.ratelimiters.TokenBucket;

public class TokenBucket {
    double capacity;
    double tokens;
    double refillRate;
    long lastRefillTimestamp;


    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefillTimestamp = System.nanoTime();
    }


    public void refill() {
        long now = System.nanoTime();
        double seconds = (double) (now - lastRefillTimestamp) / 1_000_000_000;
        double tokensToAdd = seconds * refillRate;
        if (tokensToAdd > 0.0) {
            tokens = Math.min(tokens + tokensToAdd, capacity);
            lastRefillTimestamp = now;
        }
    }


    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }
}
