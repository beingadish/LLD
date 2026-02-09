package system;

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

/**
 * Represents a consistent hashing system.
 * This class manages servers, routing of requests, and tracks system statistics.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class ConsistentHashingSystem {

    /**
     * The hash ring that stores the virtual nodes.
     */
    private final HashRing ring;

    /**
     * The request router that routes requests to the appropriate virtual node.
     */
    private final RequestRouter router;

    /**
     * The request tracker that records all incoming requests.
     */
    private final RequestTracker tracker = new RequestTracker();

    /**
     * The hot key detector that identifies frequently accessed keys.
     */
    private final HotKeyDetector hotKeyDetector = new HotKeyDetector();

    /**
     * The latency tracker that records the latency of each request.
     */
    private final LatencyTracker latencyTracker = new LatencyTracker();

    /**
     * A map to store the load of each server.
     */
    private final ConcurrentHashMap<Integer, AtomicInteger> serverLoad = new ConcurrentHashMap<>();

    /**
     * A map to store the servers in the system.
     */
    private final ConcurrentHashMap<Integer, Server> servers = new ConcurrentHashMap<>();

    /**
     * A counter to generate unique server IDs.
     */
    private final AtomicInteger serverCounter = new AtomicInteger();

    /**
     * The number of virtual nodes per server.
     */
    private volatile int virtualNodesPerServer = 3;

    /**
     * Constructs a new ConsistentHashingSystem.
     */
    public ConsistentHashingSystem() {
        this.ring = new HashRing(new SHA256Hash());
        this.router = new RequestRouter(ring);
    }

    /* ================= CONFIG ================= */

    /**
     * Sets the number of virtual nodes per server.
     *
     * @param count The number of virtual nodes per server.
     * @throws IllegalArgumentException if the count is less than or equal to zero.
     */
    public void setVirtualNodesPerServer(int count) {
        if (count <= 0) throw new IllegalArgumentException();
        this.virtualNodesPerServer = count;
        rebuildRing();
    }

    /* ================= SERVERS ================= */

    /**
     * Adds a new server to the system.
     */
    public void addServer() {
        int id = serverCounter.getAndIncrement();
        Server server = new Server(id);

        servers.put(id, server);
        serverLoad.put(id, new AtomicInteger());

        addVirtualNodes(server);
    }

    /**
     * Removes a server from the system.
     *
     * @param serverId The ID of the server to remove.
     */
    public void removeServer(int serverId) {
        servers.remove(serverId);
        serverLoad.remove(serverId);
        ring.removeServer(serverId);
    }

    /* ================= ROUTING ================= */

    /**
     * Locates the server for a given request key.
     *
     * @param requestKey The key of the request.
     * @return The ID of the server that should handle the request.
     */
    public int locate(String requestKey) {
        long start = System.nanoTime();

        VirtualNode node = router.route(requestKey);
        int serverId = node.server().id();

        serverLoad.get(serverId).incrementAndGet();
        hotKeyDetector.track(requestKey);

        long latency = System.nanoTime() - start;
        latencyTracker.record(latency);

        tracker.record(new RequestRecord(requestKey, serverId, latency, System.currentTimeMillis()));

        return serverId;
    }

    /* ================= STATS ================= */

    /**
     * Displays the current statistics of the system.
     */
    public void displayStats() {
        System.out.println("\n--- SYSTEM STATS ---");

        serverLoad.forEach((k, v) -> System.out.println("Server " + k + " -> " + v.get()));

        System.out.println("Std Deviation: " + StatisticsCalculator.stdDeviation(serverLoad.values().stream().map(AtomicInteger::get).toList()));

        System.out.println("Avg Latency(ms): " + latencyTracker.averageLatencyMs());
        System.out.println("Hot Keys: " + hotKeyDetector.topHotKeys(3));
    }

    /**
     * Visualizes the hash ring.
     */
    public void visualizeRing() {
        RingVisualizer.print(ring);
    }

    /**
     * Replays all the recorded requests.
     */
    public void replayRequests() {
        tracker.replay().forEach(System.out::println);
    }

    /* ================= INTERNAL ================= */

    /**
     * Adds virtual nodes for a given server.
     *
     * @param server The server for which to add virtual nodes.
     */
    private void addVirtualNodes(Server server) {
        for (int i = 0; i < virtualNodesPerServer; i++) {
            ring.addVirtualNode(new VirtualNode(server, server.id() + "_" + i));
        }
    }

    /**
     * Rebuilds the hash ring.
     */
    private void rebuildRing() {
        ring.clear();
        servers.values().forEach(this::addVirtualNodes);
    }
}