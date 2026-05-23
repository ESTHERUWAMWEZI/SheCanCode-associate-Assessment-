package rw.igirepay.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IgirePayGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(IgirePayGatewayApplication.class, args);
    }
}
