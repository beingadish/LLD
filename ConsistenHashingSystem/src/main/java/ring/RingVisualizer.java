package ring;

/**
 * A utility class to visualize the hash ring.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class RingVisualizer {

    /**
     * Prints the hash ring to the console.
     *
     * @param ring The hash ring to print.
     */
    public static void print(HashRing ring) {
        System.out.println("\n--- HASH RING ---");
        ring.snapshot().forEach((k, v) -> System.out.println(k + " -> " + v.name() + " (Server " + v.server().id() + ")"));
    }
}
