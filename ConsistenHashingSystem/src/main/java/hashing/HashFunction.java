package hashing;

/**
 * An interface for a hash function.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public interface HashFunction {
    /**
     * Hashes a key.
     *
     * @param key The key to hash.
     * @return The hashed key.
     */
    long hash(String key);
}
