package rw.igirepay.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "igirepay.idempotency")
public class IdempotencyProperties {

    private int ttlMinutes = 30;
    private long evictionIntervalMs = 60_000;

    public int getTtlMinutes() { return ttlMinutes; }
    public void setTtlMinutes(int ttlMinutes) { this.ttlMinutes = ttlMinutes; }
    public long getEvictionIntervalMs() { return evictionIntervalMs; }
    public void setEvictionIntervalMs(long evictionIntervalMs) { this.evictionIntervalMs = evictionIntervalMs; }
}
