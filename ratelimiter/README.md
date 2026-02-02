# Rate Limiter Algorithms from First Principles

This project provides a from-scratch implementation of common rate limiting algorithms in Java. It is designed as a learning resource for backend engineers, system design practitioners, and anyone interested in understanding how rate limiters work under the hood.

The code was built over a weekend for deliberate learning and experimentation, combining principles from:

* *Head First Design Patterns* (Eric Freeman, Elisabeth Robson)
* *System Design Interview* (Alex Xu)

## Design & Architecture

The project uses fundamental design patterns to create a flexible and extensible system.

* **Strategy Pattern**: The `RateLimiter` interface defines a common contract for all rate limiting algorithms. Each specific algorithm (e.g., `FixedWindowRateLimiter`, `TokenBucketRateLimiter`) is a concrete implementation of this strategy. This allows the client to switch between algorithms without changing its own code.

* **Factory Pattern**: The `RateLimiterFactory` provides a centralized way to create instances of different rate limiters. It abstracts the instantiation logic, making it easy to add new algorithms and manage their configurations. The factory ensures that clients get a correctly configured rate limiter without needing to know the implementation details.

This design promotes loose coupling and high cohesion. The factory and strategy patterns work together to create a system where algorithms can be added, removed, or modified with minimal impact on the rest of the application.

## Algorithms Implemented

This repository covers five fundamental rate limiting algorithms.

### 1. Fixed Window Counter

* **Concept**: Divides time into fixed windows (e.g., one minute). A counter tracks the number of requests in the current window. If the counter exceeds a threshold, requests are dropped until a new window starts.
* **Problem It Solves**: Simple, memory-efficient rate limiting.
* **Trade-offs**: Prone to sudden bursts of traffic at the window boundaries. For example, if the limit is 100 requests/minute, a user could make 100 requests at 0:59 and another 100 at 1:01, effectively sending 200 requests in a two-second span.
* **Use Case**: Good for simple, low-stakes rate limiting where perfect accuracy is not required.

### 2. Sliding Window Log

* **Concept**: Stores a timestamp for each request in a sorted set or list. When a new request arrives, it removes all timestamps older than the current window. If the number of remaining timestamps is below the limit, the request is accepted.
* **Problem It Solves**: Provides highly accurate rate limiting and avoids the boundary issues of the Fixed Window Counter.
* **Trade-offs**: High memory usage, as it stores a timestamp for every single request within the window. This can be costly for high-traffic systems.
* **Use Case**: Suitable for applications where rate limiting must be precise and traffic volume is manageable.

### 3. Sliding Window Counter

* **Concept**: A hybrid approach that combines the efficiency of the Fixed Window Counter with the accuracy of the Sliding Window. It tracks the counter for the previous window and the current window. The limit is approximated based on a weighted sum of requests from both windows.
* **Problem It Solves**: Balances memory efficiency and accuracy. It provides a much better defense against bursty traffic than the Fixed Window Counter without the high memory cost of the Sliding Window Log.
* **Trade-offs**: The rate limiting is approximate, not perfectly accurate. However, it is often "good enough" for many real-world scenarios.
* **Use Case**: A very common and practical choice for large-scale systems that need a good balance of performance and accuracy.

### 4. Token Bucket

* **Concept**: A bucket holds a predefined number of tokens. Tokens are added to the bucket at a fixed rate. Each incoming request consumes one token. If the bucket is empty, the request is rejected.
* **Problem It Solves**: Allows for controlled bursts of traffic. If the bucket has accumulated tokens, a user can make a burst of requests up to the bucket's capacity. It smooths out traffic over the long term.
* **Trade-offs**: Can be more complex to implement than window-based algorithms. Determining the right bucket size and refill rate is key to its effectiveness.
* **Use Case**: Ideal for APIs where you want to allow short bursts of activity while enforcing a long-term average rate. For example, allowing a user to upload multiple files at once.

### 5. Leaky Bucket

* **Concept**: Requests are added to a fixed-size queue (the bucket). A processor pulls requests from the queue and processes them at a fixed rate. If the queue is full, new requests are rejected.
* **Problem It Solves**: Enforces a steady, constant outflow of traffic, regardless of how bursty the inflow is. It is excellent for processing requests at a stable rate.
* **Trade-offs**: Does not allow for bursts. Even if the system has been idle, it will not process requests any faster than its fixed rate. This can lead to increased latency.
* **Use Case**: Best for systems that need to process jobs or send data at a constant rate, such as video streaming services or notification dispatch systems.

## Code Structure & Navigation

The codebase is organized into distinct packages, each with a clear responsibility.

* `com.beingadish.ratelimiters`: The root package.
  * `RateLimiter.java`: The core strategy interface.
  * `RateLimiterFactory.java`: The factory for creating rate limiters.
* `com.beingadish.ratelimiters.commons.configurations`: Contains simple configuration objects for each algorithm. This separates configuration from the algorithm logic itself.
* `com.beingadish.ratelimiters.<AlgorithmName>`: Each algorithm resides in its own package (e.g., `FixedWindow`, `TokenBucket`). This includes the `RateLimiter` implementation and any stateful objects it depends on (e.g., `TokenBucket.java`).

Per-user state (e.g., a specific user's token bucket or window counter) is managed by storing a map of user IDs to their respective rate limiter state objects. The `*RateLimiter` classes in this project demonstrate this by taking a `userId` in their `allow()` method.

### How to Read This Codebase

For learners, here is a suggested reading order:

1. **Start with the Core Abstractions**:
    * `RateLimiter.java`: Understand the single `allow(String userId)` method that serves as the entry point for all algorithms.
    * `RateLimiterFactory.java`: See how it maps an enum (`RateLimiterType`) to a concrete implementation.

2. **Explore the Algorithms (Simple to Complex)**:
    * **Fixed Window**: Start with `FixedWindowRateLimiter.java`. It is the most straightforward implementation and demonstrates the basic concept of time-based windows.
    * **Token Bucket**: Next, read `TokenBucketRateLimiter.java` and `TokenBucket.java`. This introduces the concept of stateful objects that manage tokens and refill rates.
    * **Leaky Bucket**: Examine `LeakingBucketRateLimiter.java` and `LeakingBucket.java`. This shows a queue-based approach to smoothing out request rates.
    * **Sliding Window Log**: Read `SlidingWindowRateLimiter.java`. This demonstrates a more accurate but memory-intensive approach using timestamps.
    * **Sliding Window Counter**: This algorithm is not yet implemented but would be the final step, combining concepts from the Fixed Window and Sliding Window Log.

3. **Review the Configurations**:
    * Look at the classes in the `configurations` package. Notice how they are simple data holders that decouple the algorithm's parameters from its logic.

## Real-World Relevance

These algorithms are the building blocks of rate limiting in production systems.

* **API Gateways**: Services like Amazon API Gateway, Kong, or Apigee use rate limiters (often Token Bucket or Sliding Window Counter) to protect backend services from being overwhelmed.
* **Backend Services**: Individual microservices often implement their own rate limiters to prevent cascading failures and ensure fair resource usage among clients.
* **Distributed Systems**: In a distributed environment, a centralized store like Redis is often used to maintain the counters or token buckets, allowing rate limiting to be enforced across a fleet of servers. The Sliding Window Counter algorithm is particularly well-suited for this, as it offers a good trade-off between performance, accuracy, and implementation complexity in a distributed setting.
