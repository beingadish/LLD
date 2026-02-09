package analytics;

import domain.RequestRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestTracker {
    private final Queue<RequestRecord> history = new ConcurrentLinkedQueue<>();

    public void record(RequestRecord record) {
        history.add(record);
    }

    public List<RequestRecord> replay() {
        return new ArrayList<>(history);
    }
}