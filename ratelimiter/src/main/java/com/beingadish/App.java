package com.beingadish;

import com.beingadish.ratelimiters.RateLimiter;
import com.beingadish.ratelimiters.RateLimiterFactory;
import com.beingadish.ratelimiters.commons.configurations.FixedWindowConfig;
import com.beingadish.ratelimiters.commons.configurations.LeakyBucketConfig;
import com.beingadish.ratelimiters.commons.configurations.SlidingWindowCounterConfig;
import com.beingadish.ratelimiters.commons.configurations.SlidingWindowConfig;
import com.beingadish.ratelimiters.commons.configurations.TokenBucketConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A demonstration of different rate limiter implementations.
 */
public class App {

    /**
     * The main entry point for the rate limiter demonstration.
     *
     * @param args Command line arguments (not used).
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Rate Limiter Demonstration");

        RateLimiterFactory rateLimiterFactory = new RateLimiterFactory();
        List<String> requests = getRequests();

        // Create different types of rate limiters
        Map<String, RateLimiter> rateLimiters = Map.of(
                "Token Bucket", rateLimiterFactory.getRateLimiter(new TokenBucketConfig(4L, 2L)),
                "Leaky Bucket", rateLimiterFactory.getRateLimiter(new LeakyBucketConfig(6L, 3L)),
                "Fixed Window", rateLimiterFactory.getRateLimiter(new FixedWindowConfig(1000L, 5)),
                "Sliding Window Log", rateLimiterFactory.getRateLimiter(new SlidingWindowConfig(5, 1000L)),
                "Sliding Window Counter", rateLimiterFactory.getRateLimiter(new SlidingWindowCounterConfig(5, 1000L))
        );

        // Simulate request flow for each rate limiter
        for (Map.Entry<String, RateLimiter> entry : rateLimiters.entrySet()) {
            System.out.println("==================== " + entry.getKey() + " ===================");
            simulateRequestFlow(entry.getValue(), requests);
        }
    }

    /**
     * Simulates a flow of requests against a given rate limiter and prints the statistics.
     *
     * @param rateLimiter The rate limiter to test.
     * @param requests    The list of requests to simulate.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    private static void simulateRequestFlow(RateLimiter rateLimiter, List<String> requests) throws InterruptedException {
        ConcurrentHashMap<String, Integer> statsAccept = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> statsReject = new ConcurrentHashMap<>();
        double beforeTime = System.nanoTime();

        for (String request : requests) {
            Thread.sleep(10); // Simulate a small delay between requests
            if (rateLimiter.isAllowed(request)) {
                statsAccept.compute(request, (k, v) -> (v == null) ? 1 : v + 1);
                // System.out.print(request + " [ALLOWED] - "); // Uncomment to see individual request status
            } else {
                statsReject.compute(request, (k, v) -> (v == null) ? 1 : v + 1);
                // System.out.print(request + " [NOT ALLOWED] - "); // Uncomment to see individual request status
            }
            // System.out.println("Current time : " + (System.nanoTime() - beforeTime) / 1_000_000_000.0 + " Seconds");
        }
        double totalTime = (System.nanoTime() - beforeTime) / 1_000_000_000.0;
        System.out.printf("Total simulation time: %.2f seconds%n", totalTime);
        printStats(statsAccept, statsReject);
    }

    /**
     * Prints the statistics of accepted and rejected requests.
     *
     * @param statsAccept A map containing the count of accepted requests per user.
     * @param statsReject A map containing the count of rejected requests per user.
     */
    private static void printStats(Map<String, Integer> statsAccept, Map<String, Integer> statsReject) {
        System.out.println("--- STATS [Accepted] ---");
        statsAccept.forEach((user, count) -> System.out.printf("%s: %d%n", user, count));

        System.out.println("--- STATS [Rejected] ---");
        statsReject.forEach((user, count) -> System.out.printf("%s: %d%n", user, count));
        System.out.println();
    }

    /**
     * Generates a list of sample requests.
     *
     * @return A list of user IDs representing requests.
     */
    private static List<String> getRequests() {
        return List.of("user1", "user2", "user2", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user2", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user1", "user1", "user1", "user1", "user1", "user1", "user1", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user1", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user1", "user1", "user1", "user1", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user1", "user1", "user2", "user2", "user2", "user1", "user1", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user3", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2", "user2");
    }
}
