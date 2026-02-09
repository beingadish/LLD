package hashing;

import java.security.MessageDigest;

public class SHA256Hash implements HashFunction {
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
