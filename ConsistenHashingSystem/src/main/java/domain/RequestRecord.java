package domain;

/**
 * Represents a record of a request.
 *
 * @param requestKey The key of the request.
 * @param serverId   The ID of the server that handled the request.
 * @param latencyNs  The latency of the request in nanoseconds.
 * @param timestamp  The timestamp of the request.
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public record RequestRecord(String requestKey, int serverId, long latencyNs, long timestamp) {
}
