package routing;

import domain.VirtualNode;
import ring.HashRing;

/**
 * A class that routes requests to the appropriate virtual node in the hash ring.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class RequestRouter {
    /**
     * The hash ring.
     */
    private final HashRing ring;

    /**
     * Constructs a new RequestRouter.
     *
     * @param ring The hash ring to use for routing.
     */
    public RequestRouter(HashRing ring) {
        this.ring = ring;
    }

    /**
     * Routes a request to the appropriate virtual node.
     *
     * @param requestKey The key of the request.
     * @return The virtual node that should handle the request.
     */
    public VirtualNode route(String requestKey) {
        return ring.locate(requestKey);
    }
}