package domain;

public record RequestRecord(String requestKey, int serverId, long latencyNs, long timestamp) {
}
