package mz.geator.mensagens;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashUtil {
    private HashUtil() {}

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format("%02x", b));
            return out.toString();
        } catch (Exception e) {
            return String.valueOf(value.hashCode());
        }
    }
}
