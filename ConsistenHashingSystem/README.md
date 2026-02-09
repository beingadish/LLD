# Consistent Hashing System

A complete implementation of Consistent Hashing built from first principles in Java. This project demonstrates core distributed systems concepts through clean code, design patterns, and comprehensive analytics.

**This project was built as a deliberate learning and experimentation exercise, not as a framework or production library.**

The implementation is inspired by learning from:

- _System Design Interview_ (Alex Xu)
- _Head First Design Patterns_ (Eric Freeman, Elisabeth Robson)
- Production distributed systems patterns

---

## What This Project Demonstrates

This is a fully functional consistent hashing system that solves the fundamental problem of distributed data placement: how to map keys to servers in a way that minimizes remapping when servers are added or removed.

**Core components:**

- Hash ring construction using sorted data structure
- Virtual nodes for statistical load distribution
- Request routing with clockwise search
- Dynamic server addition and removal
- Load distribution analysis with standard deviation
- Hot key detection and frequency tracking
- Latency measurement in nanoseconds
- Request replay for migration scenarios
- Thread-safe operations for concurrent environments

**What makes this educational:**

- Built without external distributed systems libraries
- Every component explained from first principles
- Runnable simulations with configurable parameters
- Observable metrics to validate theoretical claims
- Clean separation between algorithm, analytics, and simulation

---

## Consistent Hashing Explained Through the Code

### The Problem: Why Modulo-Based Hashing Fails

Traditional approach:

```java
int server = Math.abs(key.hashCode()) % serverCount;
```

**What breaks:** When `serverCount` changes, almost every key maps to a different server.

- Add server: 9 to 10 servers → ~90% of keys remap
- Remove server: 10 to 9 servers → ~89% of keys remap

In production this means:

- Cache invalidation storms (all cached data becomes stale)
- Massive data migration (terabytes moving between nodes)
- Service degradation during scaling operations

### The Solution: Consistent Hashing

Consistent hashing ensures only K/N keys remap when topology changes (where K = total keys, N = server count).

**Core idea:** Hash both servers and keys onto the same circular space.

**Implementation in this codebase:**

```java
// From HashRing.java
public VirtualNode locate(String requestKey) {
    long hash = hashFunction.hash(requestKey);              // Step 1: Hash the key
    Map.Entry<Long, VirtualNode> entry = ring.ceilingEntry(hash);  // Step 2: Find first server clockwise
    return entry != null ? entry.getValue() : ring.firstEntry().getValue();  // Step 3: Wrap around if needed
}
```

**How it works:**

1. **Hash the key** onto the ring (0 to 2^63-1 space)
2. **Find the first server clockwise** from that position
3. **Wrap around** to the first server if no server found after the key

**When a server is removed:**

```java
// From HashRing.java
public void removeServer(int serverId) {
    ring.entrySet().removeIf(e -> e.getValue().server().id() == serverId);
}
```

Only keys mapped to that server move to the next clockwise server. All other keys stay unchanged.

**When a server is added:**

```java
// From ConsistentHashingSystem.java
private void addVirtualNodes(Server server) {
    for (int i = 0; i < virtualNodesPerServer; i++) {
        ring.addVirtualNode(new VirtualNode(server, server.id() + "_" + i));
    }
}
```

The new server takes a portion of keys from multiple existing servers, not all from one.

### Virtual Nodes: Solving Uneven Distribution

**The problem without virtual nodes:**

If you hash 3 servers directly, they might cluster on one side of the ring:

```
Ring: [Server0 at position 100] [Server1 at position 150] [Server2 at position 200] ... [huge gap] ...
```

Result: Severely unbalanced load.

**The solution:**

Each physical server gets multiple virtual positions:

```java
// Server 0 with 3 virtual nodes
ring.addVirtualNode(new VirtualNode(server0, "server0_0"));  // Position: 12453
ring.addVirtualNode(new VirtualNode(server0, "server0_1"));  // Position: 98234
ring.addVirtualNode(new VirtualNode(server0, "server0_2"));  // Position: 234091
```

**Why this works:**

With 100+ virtual nodes per server, statistical distribution ensures even load. This is observable in the simulation with standard deviation metrics.

**Code mapping:**

- Virtual node count: `virtualNodesPerServer` in ConsistentHashingSystem
- Each virtual node: `VirtualNode(server, uniqueName)` record
- Ring storage: `ConcurrentSkipListMap<Long, VirtualNode>` keeps them sorted

---

## Design & Architecture

This implementation separates concerns into distinct packages, mirroring how real distributed systems are built.

### Core Abstractions

**HashRing (ring/)**

The fundamental data structure. Manages the circular space and virtual node placement.

```java
public class HashRing {
    private final ConcurrentSkipListMap<Long, VirtualNode> ring;

    public VirtualNode locate(String requestKey) { ... }
    public void addVirtualNode(VirtualNode node) { ... }
    public void removeServer(int serverId) { ... }
}
```

**Why separate:** In production systems (Cassandra, DynamoDB), the ring is a standalone component that can be queried, visualized, and reasoned about independently.

**VirtualNode and Server (domain/)**

Immutable domain models representing physical and logical entities.

```java
public record Server(int id) { }
public record VirtualNode(Server server, String name) { }
```

**Why records:** Immutability prevents accidental state corruption in concurrent environments. Records provide free thread-safety.

**RequestRouter (routing/)**

Translates business requests into ring operations.

```java
public class RequestRouter {
    public VirtualNode route(String requestKey) {
        return ring.locate(requestKey);
    }
}
```

**Why separate:** Routing logic might involve retries, fallbacks, or health checks in production. Keeping it separate from the ring allows independent evolution.

**Analytics (analytics/)**

Observability layer for metrics and debugging.

```java
HotKeyDetector     // Finds frequently accessed keys
LatencyTracker     // Measures routing performance
StatisticsCalculator  // Computes distribution quality
RequestTracker     // Stores request history for replay
```

**Why separate:** In production, you might swap in Prometheus, StatsD, or DataDog. Analytics should not pollute core algorithm code.

### Why These Separations Matter in Real Systems

**Hashing abstraction:**

```java
public interface HashFunction {
    long hash(String key);
}
```

Different use cases need different hash functions:

- Caching: Fast non-cryptographic hash (MurmurHash3)
- Security-sensitive: Cryptographic hash (SHA-256)
- Compatibility: Legacy hash function from existing system

**Analytics independence:**

Production systems need to:

- A/B test different ring configurations
- Export metrics without changing algorithm code
- Replay production traffic in staging

Separation allows this without risking the core algorithm.

**Domain models as contracts:**

`Server` and `VirtualNode` are records (immutable). This means:

- No defensive copying needed
- Safe to share across threads
- Cannot enter invalid states
- Clear contracts between components

### Design Patterns

**Strategy Pattern (Hash Function)**

Abstraction allows runtime selection of hash algorithm without changing HashRing code.

**Composition over Inheritance**

ConsistentHashingSystem composes HashRing, RequestRouter, and analytics components. No inheritance hierarchies to maintain.

**Single Responsibility Principle**

Each class has one reason to change:

- HashRing changes if ring algorithm changes
- RequestRouter changes if routing logic changes
- HotKeyDetector changes if hot key definition changes

### Thread Safety

**ConcurrentSkipListMap for the ring:**

```java
private final ConcurrentSkipListMap<Long, VirtualNode> ring = new ConcurrentSkipListMap<>();
```

- Lock-free reads (critical for high throughput)
- Sorted by hash value (enables binary search)
- O(log n) operations

**AtomicInteger for counters:**

```java
private final ConcurrentHashMap<Integer, AtomicInteger> serverLoad;
```

Prevents lost updates when multiple threads route concurrently.

**Immutable domain models:**

Records are immutable. Once a VirtualNode is created, it cannot change. This eliminates entire classes of concurrency bugs.

---

## Core Algorithm

### The Hash Ring

```java
private final ConcurrentSkipListMap<Long, VirtualNode> ring =
    new ConcurrentSkipListMap<>();
```

**Why ConcurrentSkipListMap?**

- Sorted by hash value (natural ring ordering)
- O(log n) lookup for ceiling operation
- Thread-safe for concurrent reads/writes

### Key Lookup

```java
public VirtualNode locate(String requestKey) {
    long hash = hashFunction.hash(requestKey);
    Map.Entry<Long, VirtualNode> entry = ring.ceilingEntry(hash);
    return entry != null ? entry.getValue() : ring.firstEntry().getValue();
}
```

**Steps:**

1. Hash the request key to get position on ring
2. Find first virtual node at or after that position (ceiling)
3. If no node after, wrap around to first node (ring property)
4. Return the virtual node, which knows its physical server

### Server Addition

```java
private void addVirtualNodes(Server server) {
    for (int i = 0; i < virtualNodesPerServer; i++) {
        ring.addVirtualNode(new VirtualNode(server, server.id() + "_" + i));
    }
}
```

- Each server gets multiple positions on the ring
- Virtual node names must be unique: `"server0_0"`, `"server0_1"`, etc.
- More virtual nodes = better distribution but more memory

### Server Removal

```java
public void removeServer(int serverId) {
    servers.remove(serverId);
    serverLoad.remove(serverId);
    ring.removeServer(serverId);
}
```

- Removes all virtual nodes belonging to that server
- Requests previously routed to that server now go to the next server clockwise
- Only affects keys mapped to the removed server

---

## Code Structure & Navigation

### Package Responsibilities

**`domain/`**

- Core immutable entities
- `Server`: physical server (just an ID in this implementation)
- `VirtualNode`: represents a server's position on the ring
- `RequestRecord`: captures request metadata for replay

**`hashing/`**

- Hash function abstraction and implementations
- `HashFunction`: interface
- `SHA256Hash`: cryptographic hash with good distribution

**`ring/`**

- Hash ring data structure and visualization
- `HashRing`: core consistent hashing logic
- `RingVisualizer`: debugging tool to print ring state

**`routing/`**

- Request routing logic
- `RequestRouter`: simple wrapper around HashRing for clarity

**`analytics/`**

- Observability and metrics
- `RequestTracker`: stores request history
- `HotKeyDetector`: finds frequently accessed keys
- `LatencyTracker`: measures routing performance
- `StatisticsCalculator`: computes distribution metrics (std deviation)

**`system/`**

- Orchestration and public API
- `ConsistentHashingSystem`: main coordinator
- Manages servers, routes requests, tracks metrics

---

## How to Read This Codebase

### Suggested Reading Order

**1. Start with domain models (5 minutes)**

- [domain/Server.java](src/main/java/domain/Server.java)
- [domain/VirtualNode.java](src/main/java/domain/VirtualNode.java)
- [domain/RequestRecord.java](src/main/java/domain/RequestRecord.java)

**2. Understand hashing abstraction (5 minutes)**

- [hashing/HashFunction.java](src/main/java/hashing/HashFunction.java)
- [hashing/SHA256Hash.java](src/main/java/hashing/SHA256Hash.java)

**3. Core algorithm (15 minutes)**

- [ring/HashRing.java](src/main/java/ring/HashRing.java) ← **Most important**
  - Focus on: `locate()`, `addVirtualNode()`, `removeServer()`
  - Understand why ConcurrentSkipListMap is used
  - Notice ceiling entry for clockwise search

**4. Routing layer (5 minutes)**

- [routing/RequestRouter.java](src/main/java/routing/RequestRouter.java)

**5. System orchestration (15 minutes)**

- [system/ConsistentHashingSystem.java](src/main/java/system/ConsistentHashingSystem.java)
  - See how components are composed
  - Notice analytics integration
  - Understand virtual node configuration

**6. Analytics (optional, 10 minutes)**

- [analytics/HotKeyDetector.java](src/main/java/analytics/HotKeyDetector.java)
- [analytics/LatencyTracker.java](src/main/java/analytics/LatencyTracker.java)
- [analytics/StatisticsCalculator.java](src/main/java/analytics/StatisticsCalculator.java)

**7. Run the simulation**

- [Main.java](src/main/java/Main.java)
  - See realistic usage
  - Observe load distribution
  - Test server removal scenario

### What to Focus On

**In each package:**

- **Domain**: Immutability through records, clear naming
- **Hashing**: Strategy pattern, interface abstraction
- **Ring**: Algorithm correctness, thread safety, edge cases
- **Analytics**: ConcurrentHashMap for thread safety, streaming APIs
- **System**: Component coordination, configuration management

### Mapping Code to Theory

**Consistent Hashing Theory → Code**

| Concept           | Implementation                             |
| ----------------- | ------------------------------------------ |
| Hash Ring         | `ConcurrentSkipListMap<Long, VirtualNode>` |
| Hash Function     | `HashFunction` interface + SHA256Hash      |
| Virtual Nodes     | Multiple `VirtualNode` per `Server`        |
| Clockwise Search  | `ring.ceilingEntry(hash)`                  |
| Wrap Around       | `ring.firstEntry()` fallback               |
| Load Distribution | Standard deviation calculation             |
| Key Remapping     | Server removal and request replay          |

---

## Simulation & Experiments

This project includes a configurable simulation (Main.java) that validates consistent hashing theory through observable metrics.

### How the Simulation Works

```java
// 1. Setup
ConsistentHashingSystem system = new ConsistentHashingSystem();
system.setVirtualNodesPerServer(VIRTUAL_NODES_PER_SERVER);

// 2. Add servers
for (int i = 0; i < SERVERS; i++) system.addServer();

// 3. Route millions of requests
for (int i = 1; i <= TOTAL_REQUESTS; i++) {
    String key = generateKey();  // Mix of unique keys and hot keys
    int server = system.locate(key);  // Route and measure
}

// 4. Analyze distribution
system.displayStats();  // Load per server, std deviation, hot keys

// 5. Test removal
system.removeServer(0);
// Route more requests and observe key movement
```

### Configurable Parameters

| Parameter                  | Purpose                  | Typical Range | Impact                                    |
| -------------------------- | ------------------------ | ------------- | ----------------------------------------- |
| `SERVERS`                  | Physical server count    | 3-100         | More servers = finer-grained distribution |
| `VIRTUAL_NODES_PER_SERVER` | Virtual nodes per server | 1-500         | Higher = better balance, more memory      |
| `TOTAL_REQUESTS`           | Simulation size          | 1K-100M       | Larger = more accurate statistics         |
| `HOT_KEY_RATIO`            | Hot key frequency        | 5-100         | Lower = more hot key skew                 |

### Experiment 1: Impact of Virtual Nodes

**Hypothesis:** More virtual nodes improve load distribution.

**Setup:**

- 10 servers, 10 million requests, 1000 unique keys
- Vary virtual nodes: 1, 10, 50, 100, 200

**Expected observations:**

```
Virtual Nodes = 1:   Std Dev ~300,000  (very uneven)
Virtual Nodes = 10:  Std Dev ~80,000   (better)
Virtual Nodes = 50:  Std Dev ~20,000   (good)
Virtual Nodes = 100: Std Dev ~10,000   (excellent)
Virtual Nodes = 200: Std Dev ~7,000    (diminishing returns)
```

**Why:** With few virtual nodes, randomness of hash function causes clustering. More virtual nodes provide statistical smoothing.

**Diminishing returns:** Beyond 100-150 virtual nodes, improvement is minimal but memory usage increases linearly.

### Experiment 2: Key Cardinality vs Server Count

**Hypothesis:** Adding more servers helps only if key cardinality is high enough.

**Setup:**

- 100 virtual nodes per server, 10 million requests
- Scenario A: 10 unique keys, vary servers 3 → 10 → 50
- Scenario B: 10,000 unique keys, vary servers 3 → 10 → 50

**Expected observations:**

**Scenario A (10 unique keys):**

```
3 servers:  Each handles ~3 keys, balanced
10 servers: Some servers get 0 keys (wasted capacity)
50 servers: Most servers idle
```

**Scenario B (10,000 unique keys):**

```
3 servers:  Each handles ~3,333 keys, balanced
10 servers: Each handles ~1,000 keys, balanced
50 servers: Each handles ~200 keys, balanced
```

**Why:** Consistent hashing distributes _keys_, not _requests_. If you have only 10 unique keys, 50 servers cannot all be utilized evenly. This is a fundamental limitation.

### Experiment 3: Hot Key Behavior

**Hypothesis:** Consistent hashing does not solve hot key problems.

**Setup:**

- 10 servers, 100 virtual nodes each
- 10 million requests
- 50% of requests go to a single hot key (e.g., "trending_video_id")

**Expected observations:**

```
Server 0: 100,000 requests
Server 1: 100,000 requests
Server 3: 5,100,000 requests  ← Hot key landed here
Server 4: 100,000 requests
...
```

**Why:** Consistent hashing maps each key to exactly one server. If one key is extremely popular, that server becomes a bottleneck.

**Real-world solutions:**

- Application-level sharding (split hot key requests)
- Caching at edge/CDN layer
- Read replicas for hot partitions

### Experiment 4: Server Removal Impact

**Hypothesis:** Removing a server causes only ~1/N keys to remap.

**Setup:**

- 10 servers, 100 virtual nodes each, 10 million requests to 10,000 unique keys
- Record which server handles each key
- Remove server 0
- Route same keys again and compare

**Expected observations:**

```
Keys remapped: ~1,000 out of 10,000 (10%)
Keys stable: ~9,000 (90%)

Server 0 had: ~1,000 keys
Server 1 now has: ~1,100 keys (gained ~100 from server 0)
Server 2 now has: ~1,100 keys (gained ~100 from server 0)
...
```

**Why:** Virtual nodes from server 0 were distributed across the ring. Their keys move to the next clockwise server for each virtual node. Load spreads to multiple servers.

### Key Learnings from Simulation

**1. Virtual nodes are essential**

- Without them, load distribution is unpredictable
- 100-150 virtual nodes per server is a sweet spot
- Beyond 200, gains are marginal

**2. Server count should match key cardinality**

- 100 servers for 50 unique keys is wasteful
- Horizontal scaling requires horizontal data growth

**3. Consistent hashing does NOT solve:**

- Hot keys (one key = one server = bottleneck)
- Skewed key access patterns
- Request-level load balancing

**4. What it DOES solve:**

- Minimizing data movement during scaling
- Predictable key placement
- Graceful degradation when servers fail

---

## Running the Simulation

### Prerequisites

- Java 25+ (uses preview features like unnamed patterns and `void main()`)
- Maven

### Build and Run

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=Main
```

### Configuration (in Main.java)

```java
final int SERVERS = 10;                      // Physical servers
final int VIRTUAL_NODES_PER_SERVER = 100;    // Distribution quality
final long TOTAL_REQUESTS = 10_000_000;      // Simulation size
final int HOT_KEY_RATIO = 20;                // Every 20th request is a hot key
```

### Sample Output

```
STEP → Adding Servers
--------------------------------------------------

STEP → Routing Requests
--------------------------------------------------

STEP → Final Stats
--------------------------------------------------

--- SYSTEM STATS ---
Server 0 -> 1000432
Server 1 -> 998721
Server 2 -> 1001234
...
Std Deviation: 12456.78
Avg Latency(ms): 0.00234
Hot Keys: [HOT_KEY, user-42, user-789]

STEP → Removing Server 0 & Re-testing
--------------------------------------------------

Server 1 -> 1100234  (notice increased load)
Server 2 -> 1001234
...
```

**What to observe:**

- Low standard deviation indicates balanced distribution
- Server removal affects only ~10% of keys
- Hot keys are detected and reported
- Routing latency is sub-microsecond (pure in-memory operation)

---

## Real-World Relevance

### Where Consistent Hashing is Used in Production

**Distributed Caches**

- Memcached clusters: Client-side consistent hashing to route keys
- Redis Cluster: Uses hash slots (a variant of consistent hashing)
- Amazon ElastiCache: Node addition/removal with minimal cache invalidation

**Why it matters:** When a cache node fails, only keys on that node are lost. Others remain cached and accessible.

**Distributed Databases**

- Apache Cassandra: Partition keys distributed via consistent hashing
- Amazon DynamoDB: Uses consistent hashing for partition placement
- Riak KV: Ring-based architecture for data distribution

**Why it matters:** Adding a node to a 100-node cluster causes only 1% of data to move, not 50%.

**Load Balancers**

- HAProxy: Consistent hashing for backend selection
- NGINX: Upstream server selection with hash directive
- CDN edge routing: Route requests to nearest cache node

**Why it matters:** Same user hits same backend server (session affinity) even when backend pool changes.

**Distributed Storage**

- Amazon S3: Internal partitioning of object storage
- Ceph: CRUSH algorithm (advanced consistent hashing variant)
- HDFS: Data node selection for block placement

**Why it matters:** Files are evenly distributed without maintaining a central mapping table.

### What Consistent Hashing Solves

**Problem 1: Minimizing Data Movement**

Without consistent hashing:

- Add 1 server to 10-server cluster → 90% of data moves
- With consistent hashing → ~10% of data moves

**Problem 2: Avoiding Central Coordination**

No need for a central "key → server" lookup table. Each client can independently compute where data lives using the same hash function.

**Problem 3: Graceful Degradation**

When a server fails, its load is distributed across remaining servers. No single server becomes the new bottleneck.

### What Consistent Hashing Does NOT Solve

**Problem 1: Hot Keys**

Scenario: 1 million requests/second to key "trending_video_123"

Consistent hashing maps this key to exactly one server. That server gets 100% of the load for that key.

**Solutions:**

- Application-level sharding (split requests before hashing)
- Client-side caching
- Read replicas
- CDN/edge caching

**Problem 2: Uneven Key Cardinality**

Scenario: 100 servers, but only 10 unique keys in your dataset

Consistent hashing cannot utilize all 100 servers. Only ~10 servers handle traffic.

**Solutions:**

- Ensure data model has high cardinality
- Use composite keys if needed
- Accept that over-provisioning does not help

**Problem 3: Range Queries**

Scenario: "Get all users with IDs between 1000 and 2000"

Consistent hashing scatters consecutive IDs across different servers. Range queries require scatter-gather from all servers.

**Solutions:**

- Use range-based partitioning for range-heavy workloads
- CRDB, TiDB use range partitioning + rebalancing
- Accept scatter-gather cost

**Problem 4: Rebalancing on Key Size Skew**

Scenario: Most keys are 1KB, but some keys are 100MB

Consistent hashing distributes keys evenly, not bytes. Servers storing large keys run out of disk.

**Solutions:**

- Monitor actual resource usage, not just key count
- Implement weighted consistent hashing (more virtual nodes for larger capacity)
- Split large values

**Problem 5: Network Partition Handling**

Consistent hashing does not specify what happens during split-brain scenarios. It is a data placement algorithm, not a consensus algorithm.

**Solutions:**

- Combine with consensus protocols (Raft, Paxos)
- Use quorum reads/writes
- Implement conflict resolution (last-write-wins, vector clocks)

### Production Considerations

**Hash Function Choice:**

- This implementation: SHA-256 (cryptographically secure, excellent distribution, slower)
- Production caches: MurmurHash3, xxHash (non-cryptographic, 10x faster)
- Why speed matters: Routing happens on every request (millions/second)

**Virtual Node Count:**

- This implementation: Configurable, default 100
- Production typical: 100-200 per server
- High-scale systems: 150-256 (power of 2 for bit manipulation)
- Memory cost: ~50 bytes per virtual node

**Common Extensions:**

- **Weighted servers:** Powerful servers get more virtual nodes (2x capacity = 2x virtual nodes)
- **Replication:** Store each key on N consecutive clockwise servers (3-way replication is common)
- **Rack/zone awareness:** Spread virtual nodes across failure domains
- **Bounded loads:** Cap any server at 1.25x average load, route overflow elsewhere

**Why This Matters for Interviews:**

- Consistent hashing is a favorite topic for senior/staff engineer interviews
- Explaining trade-offs (virtual nodes, hash function, hot keys) demonstrates depth
- Knowing real-world limitations separates book knowledge from production experience
- Being able to implement from scratch shows algorithmic maturity

### Typical Interview Questions

**Q: Why do we need virtual nodes?**

A: Without virtual nodes, servers land at random positions on the ring due to hash randomness. With only 3-10 servers, clustering is likely, causing severe load imbalance. Virtual nodes (100+ per server) provide statistical smoothing. Each server gets many positions spread across the ring, ensuring approximately equal key distribution.

**Q: What happens when a server is removed?**

A: Only keys mapped to that server's virtual nodes are remapped. Each virtual node's keys move to the next clockwise virtual node. Since virtual nodes are distributed across the ring, the load spreads to multiple remaining servers. Typically ~1/N of total keys move (where N is server count).

**Q: How would you handle replication?**

A: Store each key on K consecutive servers clockwise from the hash position. For example, with 3-way replication, key "user123" is stored on servers at positions clockwise-1, clockwise-2, and clockwise-3 from its hash position. This provides redundancy. Read from any replica, write to all replicas (or use quorum).

**Q: What if servers have different capacities?**

A: Use weighted consistent hashing. Assign virtual nodes proportional to capacity. A server with 2x CPU/memory gets 2x virtual nodes. Server with 200 virtual nodes handles ~2x keys compared to server with 100 virtual nodes.

**Q: How do you handle hot keys?**

A: Consistent hashing does not solve hot keys. Solutions include: (1) Application-level sharding (split hot key into sub-keys), (2) Caching at edge/CDN layer, (3) Read replicas for hot partitions, (4) Detect hot keys and route differently (this implementation includes HotKeyDetector for this purpose).

**Q: What is the time complexity of key lookup?**

A: O(log V) where V is total virtual nodes. With ConcurrentSkipListMap, ceiling entry lookup is binary search. For 10 servers with 100 virtual nodes each, that is log(1000) ≈ 10 comparisons. In practice, this is sub-microsecond.

**Q: Why not just use a hash table for the ring?**

A: Hash tables do not maintain order. Consistent hashing requires finding the "next" server clockwise, which requires sorted order. ConcurrentSkipListMap provides both sorted order and efficient ceiling/floor lookups.

---

## Key Takeaways

**What Consistent Hashing Achieves:**

- Reduces key remapping from ~90% to ~10% when servers are added or removed
- Enables decentralized routing (no central coordinator needed)
- Provides predictable key placement across distributed nodes
- Supports graceful degradation when servers fail

**What It Does NOT Solve:**

- Hot key problems (one popular key still bottlenecks one server)
- Request-level load balancing (only distributes keys, not request volume)
- Range queries (consecutive keys are scattered across servers)
- Skewed value sizes (large values can unbalance storage even with even key distribution)

**Implementation Insights:**

- Virtual nodes (100-150 per server) are essential for statistical distribution
- ConcurrentSkipListMap is the ideal data structure (sorted + thread-safe + O(log n))
- More servers only help if you have enough unique keys to distribute
- Cryptographic hashes (SHA-256) provide better distribution but are slower than non-cryptographic alternatives

**Engineering Principles:**

- Separation of concerns enables independent testing and evolution
- Immutability (records) eliminates entire categories of concurrency bugs
- Observable systems (metrics, hot key detection) are debuggable systems
- Strategy pattern allows production flexibility (swap hash functions without changing core logic)

**Learning Through Building:**

- Implementing from scratch reveals subtleties invisible when using libraries
- Runnable simulations validate theoretical claims with observable data
- Clean code architecture makes complex algorithms accessible
- Understanding limitations is as important as understanding capabilities

**Interview Preparation:**

- Know why virtual nodes exist (statistical distribution)
- Explain trade-offs (hash function speed vs distribution quality)
- Understand real-world limitations (hot keys, key cardinality)
- Be able to extend the design (replication, weighted nodes, bounded loads)

---

## Further Reading

**Books:**

- _Designing Data-Intensive Applications_ by Martin Kleppmann (Chapter 6: Partitioning)
- _System Design Interview_ by Alex Xu (Chapter 5: Design Consistent Hashing)

**Papers:**

- [Consistent Hashing and Random Trees](https://www.akamai.com/us/en/multimedia/documents/technical-publication/consistent-hashing-and-random-trees-distributed-caching-protocols-for-relieving-hot-spots-on-the-world-wide-web-technical-publication.pdf) (Karger et al., 1997)

**Production Systems:**

- [Cassandra Architecture](https://cassandra.apache.org/doc/latest/architecture/dynamo.html)
- [Amazon DynamoDB](https://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf)

---

## License

This is an educational project. Use freely for learning and experimentation.
