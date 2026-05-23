package rw.igirepay.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbound payment response — returned for both first-time processing
 * and cached duplicate retries.
 *
 * Storing httpStatusCode inside the record lets the gateway replay the
 * exact original HTTP status on cache hits (e.g. 200, 201) rather than
 * always defaulting to 200.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    @JsonProperty("message")
    private String message;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("idempotencyKey")
    private String idempotencyKey;

    @JsonProperty("status")
    private String status;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("processedAt")
    private LocalDateTime processedAt;
}
