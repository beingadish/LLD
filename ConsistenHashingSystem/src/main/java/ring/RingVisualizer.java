package ring;

public class RingVisualizer {

    public static void print(HashRing ring) {
        System.out.println("\n--- HASH RING ---");
        ring.snapshot().forEach((k, v) -> System.out.println(k + " -> " + v.name() + " (Server " + v.server().id() + ")"));
    }
}
