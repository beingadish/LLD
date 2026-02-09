package ring;

import domain.VirtualNode;
import hashing.HashFunction;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class HashRing {

    private final ConcurrentSkipListMap<Long, VirtualNode> ring = new ConcurrentSkipListMap<>();
    private final HashFunction hashFunction;

    public HashRing(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public void addVirtualNode(VirtualNode node) {
        ring.put(hashFunction.hash(node.name()), node);
    }

    public VirtualNode locate(String requestKey) {
        long hash = hashFunction.hash(requestKey);
        Map.Entry<Long, VirtualNode> entry = ring.ceilingEntry(hash);
        return entry != null ? entry.getValue() : ring.firstEntry().getValue();
    }

    public void removeServer(int serverId) {
        ring.entrySet().removeIf(e -> e.getValue().server().id() == serverId);
    }

    public void clear() {
        ring.clear();
    }

    public NavigableMap<Long, VirtualNode> snapshot() {
        return Collections.unmodifiableNavigableMap(ring);
    }
}