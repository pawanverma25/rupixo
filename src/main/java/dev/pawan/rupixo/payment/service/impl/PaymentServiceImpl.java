package dev.pawan.rupixo.payment.service.impl;

import dev.pawan.rupixo.common.enums.OrderStatus;
import dev.pawan.rupixo.common.enums.PaymentStatus;
import dev.pawan.rupixo.common.exception.BusinessRuleViolationException;
import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.payment.dto.request.PaymentInitRequest;
import dev.pawan.rupixo.payment.dto.response.PaymentResponse;
import dev.pawan.rupixo.payment.entity.OrderRecord;
import dev.pawan.rupixo.payment.entity.Payment;
import dev.pawan.rupixo.payment.gateway.PaymentAdapterRouter;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import dev.pawan.rupixo.payment.gateway.dto.PaymentResult;
import dev.pawan.rupixo.payment.mapper.PaymentMapper;
import dev.pawan.rupixo.payment.repository.OrderRepository;
import dev.pawan.rupixo.payment.repository.PaymentRepository;
import dev.pawan.rupixo.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAdapterRouter paymentAdapterRouter;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest paymentInitRequest) {
        OrderRecord order = orderRepository.findByIdAndMerchantId(paymentInitRequest.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", paymentInitRequest.orderId()));

        if(order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.CANCELLED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE", "Order cannot accept payment in status: "+order.getOrderStatus());
        }

        //attempts set
        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);
        order = orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .merchantId(merchantId)
                .method(paymentInitRequest.paymentMethod())
                .methodDetails(paymentInitRequest.methodDetails())
                .build();
        payment = paymentRepository.save(payment);

        //
        PaymentRequest paymentRequest = paymentMapper.toPaymentRequest(payment);
        PaymentResult result = paymentAdapterRouter.initiate(paymentRequest);

        switch (result){
            case PaymentResult.Failed failed -> {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode(failed.errorCode());
                payment.setErrorDescription(failed.errorDescription());
            }
            case PaymentResult.Pending pending -> {
                payment.setProcessorReference(pending.paymentRegistrationRef());
            }
        }

        payment = paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }
}
