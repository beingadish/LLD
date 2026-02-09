package domain;

/**
 * Represents a virtual node in the hash ring.
 *
 * @param server The server that this virtual node belongs to.
 * @param name   The name of the virtual node.
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public record VirtualNode(Server server, String name) {
}
