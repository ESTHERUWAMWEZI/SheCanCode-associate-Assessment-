package rw.igirepay.gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 hashing utility for request body comparison.
 *
 * Serialising to JSON before hashing normalises field ordering — two requests
 * with the same fields in different order produce the same hash, preventing
 * false-positive conflict detection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashUtil {

    private final ObjectMapper objectMapper;

    public String sha256(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JVM spec — this branch is unreachable
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        } catch (Exception e) {
            log.error("Failed to compute SHA-256 hash for payload", e);
            throw new RuntimeException("Hash computation failed", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
