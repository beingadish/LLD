import system.ConsistentHashingSystem;
import java.util.Random;

/**
 * This file contains the main entry point for a simulation of the consistent hashing system.
 * It demonstrates request distribution, server load balancing, and the effects of server addition and removal.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */

/**
 * The main method to run the consistent hashing simulation.
 */
void main() {

    /* ================= SIMULATION CONFIG ================= */

    final int SERVERS = 10;
    final int VIRTUAL_NODES_PER_SERVER = 100;
    final long TOTAL_REQUESTS = 10_000_000;
    final int HOT_KEY_RATIO = 20;
    final int MAX_VERBOSE_LINES = 100;

    boolean VERBOSE = TOTAL_REQUESTS <= MAX_VERBOSE_LINES;

    printHeader("CONSISTENT HASHING SIMULATION");

    ConsistentHashingSystem system = new ConsistentHashingSystem();
    system.setVirtualNodesPerServer(VIRTUAL_NODES_PER_SERVER);

    step("Adding Servers");
    for (int i = 0; i < SERVERS; i++) system.addServer();

    if (VERBOSE) system.visualizeRing();

    step("Routing Requests");

    Random random = new Random();

    for (int i = 1; i <= TOTAL_REQUESTS; i++) {
        String key = (i % HOT_KEY_RATIO == 0) ? "HOT_KEY" : "user-" + random.nextInt(1_000);

        long start = System.nanoTime();
        int server = system.locate(key);
        long latency = System.nanoTime() - start;

        if (VERBOSE) {
            System.out.printf("REQ-%05d | key=%-10s | server=%-3d | latency=%6.3f ms%n", i, key, server, latency / 1_000_000.0);
        }
    }

    step("Final Stats");
    system.displayStats();

    step("Removing Server 0 & Re-testing");
    system.removeServer(0);

    for (int i = 0; i < TOTAL_REQUESTS / 10; i++) {
        system.locate("post-removal-" + i);
    }

    system.displayStats();

    printFooter("SIMULATION COMPLETE");
}

/* ================= PRINT HELPERS ================= */

/**
 * Prints a formatted step title to the console.
 * @param title The title of the step.
 */
void step(String title) {
    System.out.println("\n--------------------------------------------------");
    System.out.println("STEP → " + title);
    System.out.println("--------------------------------------------------");
}

/**
 * Prints a formatted header title to the console.
 * @param title The title of the header.
 */
void printHeader(String title) {
    System.out.println("\n==================================================");
    System.out.println(" " + title);
    System.out.println("==================================================");
}

/**
 * Prints a formatted footer title to the console.
 * @param title The title of the footer.
 */
void printFooter(String title) {
    System.out.println("\n==================================================");
    System.out.println(" " + title);
    System.out.println("==================================================");
}