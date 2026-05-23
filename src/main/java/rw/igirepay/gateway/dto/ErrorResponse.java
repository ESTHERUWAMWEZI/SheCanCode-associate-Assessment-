package rw.igirepay.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standardised error envelope returned for every error response.
 *
 * A consistent error shape is a production requirement — API clients and
 * monitoring pipelines can parse errors reliably without special-casing each
 * status code. The traceId links logs to a specific failed request.
 */
@Data
@Builder
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Unique identifier for this error occurrence.
     * Clients include this in support tickets; engineers grep logs for it.
     */
    private String traceId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
