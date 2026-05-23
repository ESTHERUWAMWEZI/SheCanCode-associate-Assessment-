package rw.igirepay.gateway.service;

import rw.igirepay.gateway.dto.PaymentRequest;
import rw.igirepay.gateway.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request, String idempotencyKey);
}
