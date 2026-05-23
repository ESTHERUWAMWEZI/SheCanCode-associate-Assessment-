package rw.igirepay.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "igirepay.payment")
public class PaymentProperties {

    private long processingDelayMs = 2_000;

    public long getProcessingDelayMs() { return processingDelayMs; }
    public void setProcessingDelayMs(long processingDelayMs) { this.processingDelayMs = processingDelayMs; }
}
