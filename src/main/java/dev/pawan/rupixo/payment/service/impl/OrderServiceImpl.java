package dev.pawan.rupixo.payment.service.impl;

import dev.pawan.rupixo.common.enums.OrderStatus;
import dev.pawan.rupixo.common.exception.BusinessRuleViolationException;
import dev.pawan.rupixo.common.exception.DuplicateResourceException;
import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.payment.dto.request.CreateOrderRequest;
import dev.pawan.rupixo.payment.dto.response.OrderResponse;
import dev.pawan.rupixo.payment.dto.response.PaymentResponse;
import dev.pawan.rupixo.payment.entity.OrderRecord;
import dev.pawan.rupixo.payment.entity.Payment;
import dev.pawan.rupixo.payment.mapper.OrderMapper;
import dev.pawan.rupixo.payment.mapper.PaymentMapper;
import dev.pawan.rupixo.payment.repository.OrderRepository;
import dev.pawan.rupixo.payment.repository.PaymentRepository;
import dev.pawan.rupixo.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Value("${payment.order.defaulr-order-expiry-minutes: 30}")
    private Integer defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest orderRequest) {
        if(orderRequest.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, orderRequest.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists: " + orderRequest.receipt());
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(orderRequest.receipt())
                .amount(orderRequest. amount())
                .notes(orderRequest.notes())

                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(orderRequest.expiresAt() != null ? orderRequest.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

        //TODO: publish kafka order event here

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getOrderById(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // already paid or already canceled orders cannot be marked canceled.
        if(order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID)
            throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL", "cannot cancel the order in status: " + order.getOrderStatus().name());

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        return  orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayment(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        List<Payment> paymentsList = paymentRepository.findByOrder(order);

        return paymentMapper.toResponseList(paymentsList);
    }
}
