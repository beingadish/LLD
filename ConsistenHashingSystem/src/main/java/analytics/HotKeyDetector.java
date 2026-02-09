package analytics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HotKeyDetector {
    private final ConcurrentHashMap<String, AtomicInteger> freq = new ConcurrentHashMap<>();

    public void track(String key) {
        freq.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
    }

    public List<String> topHotKeys(int k) {
        return freq.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).limit(k).map(Map.Entry::getKey).toList();
    }
}