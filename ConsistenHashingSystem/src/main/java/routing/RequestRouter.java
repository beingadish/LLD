package routing;

import domain.VirtualNode;
import ring.HashRing;

public class RequestRouter {
    private final HashRing ring;

    public RequestRouter(HashRing ring) {
        this.ring = ring;
    }

    public VirtualNode route(String requestKey) {
        return ring.locate(requestKey);
    }
}