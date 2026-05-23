package rw.igirepay.gateway.service;

import rw.igirepay.gateway.dto.IdempotencyResult;
import rw.igirepay.gateway.dto.PaymentRequest;

public interface IdempotencyService {

    IdempotencyResult processWithIdempotency(String idempotencyKey, PaymentRequest request);
}
