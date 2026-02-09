import analytics.HotKeyDetector;
import analytics.LatencyTracker;
import analytics.RequestTracker;
import analytics.StatisticsCalculator;
import domain.RequestRecord;
import domain.Server;
import domain.VirtualNode;
import hashing.SHA256Hash;
import ring.HashRing;
import ring.RingVisualizer;
import routing.RequestRouter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsistentHashingSystem {

    private final HashRing ring;
    private final RequestRouter router;
    private final RequestTracker tracker = new RequestTracker();
    private final HotKeyDetector hotKeyDetector = new HotKeyDetector();
    private final LatencyTracker latencyTracker = new LatencyTracker();

    private final ConcurrentHashMap<Integer, AtomicInteger> serverLoad = new ConcurrentHashMap<>();
    private final AtomicInteger serverCounter = new AtomicInteger();

    public ConsistentHashingSystem() {
        this.ring = new HashRing(new SHA256Hash());
        this.router = new RequestRouter(ring);
    }

    public void addServer() {
        int id = serverCounter.getAndIncrement();
        Server server = new Server(id);
        serverLoad.put(id, new AtomicInteger());

        int virtualNodesPerServer = 3;
        for (int i = 0; i < virtualNodesPerServer; i++) {
            ring.addVirtualNode(new VirtualNode(server, id + "_" + i));
        }
    }

    public int locate(String requestKey) {
        long start = System.nanoTime();

        VirtualNode node = router.route(requestKey);
        int serverId = node.server().id();

        serverLoad.get(serverId).incrementAndGet();
        hotKeyDetector.track(requestKey);

        long latency = System.nanoTime() - start;
        latencyTracker.record(latency);

        tracker.record(new RequestRecord(requestKey, serverId, latency, System.currentTimeMillis()));

        System.out.println("Request " + requestKey + " hit " + node.name() + " -> Server " + serverId);

        return serverId;
    }

    public void displayStats() {
        System.out.println("\n--- SYSTEM STATS ---");
        serverLoad.forEach((k, v) -> System.out.println("Server " + k + ": " + v.get()));

        System.out.println("Std Deviation: " + StatisticsCalculator.stdDeviation(serverLoad.values().stream().map(AtomicInteger::get).toList()));

        System.out.println("Avg Latency(ms): " + latencyTracker.averageLatencyMs());

        System.out.println("Hot Keys: " + hotKeyDetector.topHotKeys(3));
    }

    public void visualizeRing() {
        RingVisualizer.print(ring);
    }

    public void replayRequests() {
        tracker.replay().forEach(System.out::println);
    }

    public void removeServer(int serverId) {
        ring.removeServer(serverId);
        serverLoad.remove(serverId);
    }
}