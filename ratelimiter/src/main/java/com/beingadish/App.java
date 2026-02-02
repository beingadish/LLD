package com.beingadish;

import com.beingadish.ratelimiters.RateLimiter;
import com.beingadish.ratelimiters.RateLimiterFactory;
import com.beingadish.ratelimiters.commons.configurations.FixedWindowConfig;
import com.beingadish.ratelimiters.commons.configurations.LeakyBucketConfig;
import com.beingadish.ratelimiters.commons.configurations.SlidingWindowConfig;
import com.beingadish.ratelimiters.commons.configurations.TokenBucketConfig;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class App {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Rate Limiter");
        RateLimiterFactory rateLimiterFactory = new RateLimiterFactory();
        RateLimiter tokenBucketLimiter = rateLimiterFactory.getRateLimiter(new TokenBucketConfig(4L, 2L));
        RateLimiter leakyBucketLimiter = rateLimiterFactory.getRateLimiter(new LeakyBucketConfig(6L, 3L));
        RateLimiter fixedWindowLimiter = rateLimiterFactory.getRateLimiter(new FixedWindowConfig(1000L, 5));
        RateLimiter slidingWindowLimiter = rateLimiterFactory.getRateLimiter(new SlidingWindowConfig(5, 1000L));

        List<String> requests = List.of("user1", "user2", "user2", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user2", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user1", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user1", "user1", "user1", "user1", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user1", "user1", "user2", "user2", "user2", "user1", "user1", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2");

        System.out.println("==================== Token Bucket ===================");
        simulateRequestFlow(tokenBucketLimiter, requests);
        System.out.println("==================== Leaky Bucket ===================");
        simulateRequestFlow(leakyBucketLimiter, requests);
        System.out.println("==================== Fixed Window ===================");
        simulateRequestFlow(fixedWindowLimiter, requests);
        System.out.println("==================== Sliding Window ===================");
        simulateRequestFlow(slidingWindowLimiter, requests);
    }

    private static void simulateRequestFlow(RateLimiter rateLimiter, List<String> requests) throws InterruptedException {
        ConcurrentHashMap<String, Integer> statsAccept = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> statsReject = new ConcurrentHashMap<>();
        double beforeTime = System.nanoTime();
        for (String request : requests) {
            Thread.sleep(10);
            if (rateLimiter.isAllowed(request)) {
                statsAccept.put(request, statsAccept.getOrDefault(request, 0) + 1);
                System.out.print(request + " [ALLOWED] - ");
            } else {
                statsReject.put(request, statsReject.getOrDefault(request, 0) + 1);
                System.out.print(request + " [NOT ALLOWED] - ");
            }
            System.out.println("Current time : " + (System.nanoTime() - beforeTime) / 1_000_000_000.0 + "Seconds");
        }
        print(statsAccept, statsReject);
    }

    private static void print(ConcurrentHashMap<String, Integer> statsAccept, ConcurrentHashMap<String, Integer> statsReject) {
        System.out.println("=================== STATS - [Accept] ===================");
        for (String request : statsAccept.keySet()) {
            System.out.println(request + " : " + statsAccept.get(request));
        }

        System.out.println("=================== STATS - [Reject] ===================");
        for (String request : statsReject.keySet()) {
            System.out.println(request + " : " + statsReject.get(request));
        }
    }
}