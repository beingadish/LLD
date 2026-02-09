package hashing;

import java.security.MessageDigest;

/**
 * An implementation of the HashFunction interface that uses the SHA-256 algorithm.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class SHA256Hash implements HashFunction {
    /**
     * Hashes a key using the SHA-256 algorithm.
     *
     * @param key The key to hash.
     * @return The hashed key.
     * @throws RuntimeException if the SHA-256 algorithm is not available.
     */
    @Override
    public long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(key.getBytes());
            long h = 0;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (bytes[i] & 0xff);
            }
            return h & Long.MAX_VALUE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
