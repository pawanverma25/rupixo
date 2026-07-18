package dev.pawan.rupixo.payment.service;

import dev.pawan.rupixo.payment.dto.request.CreateOrderRequest;
import dev.pawan.rupixo.payment.dto.response.OrderResponse;
import dev.pawan.rupixo.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse create(UUID merchantId, CreateOrderRequest orderRequest);

    OrderResponse getOrderById(UUID merchantId, UUID orderId);

    OrderResponse cancel(UUID merchantId, UUID orderId);

    List<PaymentResponse> listPayment(UUID merchantId, UUID orderId);
}
