package analytics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A utility class to track the latency of requests.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class LatencyTracker {
    /**
     * The total latency of all requests in nanoseconds.
     */
    private final AtomicLong totalLatency = new AtomicLong();
    /**
     * The total number of requests.
     */
    private final AtomicLong count = new AtomicLong();

    /**
     * Records the latency of a request.
     *
     * @param latencyNs The latency of the request in nanoseconds.
     */
    public void record(long latencyNs) {
        totalLatency.addAndGet(latencyNs);
        count.incrementAndGet();
    }

    /**
     * Calculates the average latency of all requests in milliseconds.
     *
     * @return The average latency in milliseconds.
     */
    public double averageLatencyMs() {
        return (totalLatency.get() / 1_000_000.0) / Math.max(1, count.get());
    }
}
