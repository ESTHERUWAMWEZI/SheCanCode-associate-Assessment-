package rw.igirepay.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the IgirePay Idempotency Gateway.
 *
 * @EnableScheduling is required for the TTL eviction task that cleans up
 * expired idempotency records — a key production concern to prevent
 * unbounded memory growth.
 */
@SpringBootApplication
@EnableScheduling
public class IgirePayGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(IgirePayGatewayApplication.class, args);
    }
}
