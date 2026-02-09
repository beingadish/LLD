package analytics;

import java.util.concurrent.atomic.AtomicLong;

public class LatencyTracker {
    private final AtomicLong totalLatency = new AtomicLong();
    private final AtomicLong count = new AtomicLong();

    public void record(long latencyNs) {
        totalLatency.addAndGet(latencyNs);
        count.incrementAndGet();
    }

    public double averageLatencyMs() {
        return (totalLatency.get() / 1_000_000.0) / Math.max(1, count.get());
    }
}
