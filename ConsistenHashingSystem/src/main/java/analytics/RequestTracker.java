package analytics;

import domain.RequestRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A utility class to track the history of requests.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class RequestTracker {
    /**
     * A queue to store the history of requests.
     */
    private final Queue<RequestRecord> history = new ConcurrentLinkedQueue<>();

    /**
     * Records a request.
     *
     * @param record The request to record.
     */
    public void record(RequestRecord record) {
        history.add(record);
    }

    /**
     * Returns a list of all recorded requests.
     *
     * @return A list of all recorded requests.
     */
    public List<RequestRecord> replay() {
        return new ArrayList<>(history);
    }
}