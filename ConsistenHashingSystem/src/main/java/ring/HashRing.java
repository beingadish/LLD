package ring;

import domain.VirtualNode;
import hashing.HashFunction;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Represents a hash ring.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class HashRing {

    /**
     * The underlying map that stores the virtual nodes in the ring.
     */
    private final ConcurrentSkipListMap<Long, VirtualNode> ring = new ConcurrentSkipListMap<>();
    /**
     * The hash function used to hash the keys.
     */
    private final HashFunction hashFunction;

    /**
     * Constructs a new HashRing.
     *
     * @param hashFunction The hash function to use.
     */
    public HashRing(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    /**
     * Adds a virtual node to the ring.
     *
     * @param node The virtual node to add.
     */
    public void addVirtualNode(VirtualNode node) {
        ring.put(hashFunction.hash(node.name()), node);
    }

    /**
     * Locates the virtual node for a given request key.
     *
     * @param requestKey The key of the request.
     * @return The virtual node that should handle the request.
     */
    public VirtualNode locate(String requestKey) {
        long hash = hashFunction.hash(requestKey);
        Map.Entry<Long, VirtualNode> entry = ring.ceilingEntry(hash);
        return entry != null ? entry.getValue() : ring.firstEntry().getValue();
    }

    /**
     * Removes a server from the ring.
     *
     * @param serverId The ID of the server to remove.
     */
    public void removeServer(int serverId) {
        ring.entrySet().removeIf(e -> e.getValue().server().id() == serverId);
    }

    /**
     * Clears the ring.
     */
    public void clear() {
        ring.clear();
    }

    /**
     * Returns an unmodifiable snapshot of the ring.
     *
     * @return An unmodifiable snapshot of the ring.
     */
    public NavigableMap<Long, VirtualNode> snapshot() {
        return Collections.unmodifiableNavigableMap(ring);
    }
}