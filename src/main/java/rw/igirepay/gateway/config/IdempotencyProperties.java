package rw.igirepay.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed binding for the igirepay.idempotency.* namespace in application.yml.
 * Centralises configuration so changes need only touch one file.
 */
@Data
@Component
@ConfigurationProperties(prefix = "igirepay.idempotency")
public class IdempotencyProperties {

    /** How long (in minutes) an idempotency record lives before TTL eviction. */
    private int ttlMinutes = 30;

    /** How often (in milliseconds) the background eviction task runs. */
    private long evictionIntervalMs = 60_000;
}
