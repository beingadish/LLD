package analytics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class to detect hot keys (frequently accessed keys).
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class HotKeyDetector {
    /**
     * A map to store the frequency of each key.
     */
    private final ConcurrentHashMap<String, AtomicInteger> freq = new ConcurrentHashMap<>();

    /**
     * Tracks the access of a key.
     *
     * @param key The key to track.
     */
    public void track(String key) {
        freq.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * Returns the top k hot keys.
     *
     * @param k The number of hot keys to return.
     * @return A list of the top k hot keys.
     */
    public List<String> topHotKeys(int k) {
        return freq.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).limit(k).map(Map.Entry::getKey).toList();
    }
}