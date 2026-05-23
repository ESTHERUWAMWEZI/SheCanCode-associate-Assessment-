package rw.igirepay.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed binding for the igirepay.payment.* namespace in application.yml.
 */
@Data
@Component
@ConfigurationProperties(prefix = "igirepay.payment")
public class PaymentProperties {

    /** Simulated downstream processing delay in milliseconds. */
    private long processingDelayMs = 2_000;
}
