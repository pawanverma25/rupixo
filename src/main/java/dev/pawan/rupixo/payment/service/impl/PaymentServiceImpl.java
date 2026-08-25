package dev.pawan.rupixo.payment.service.impl;

import dev.pawan.rupixo.common.enums.OrderStatus;
import dev.pawan.rupixo.common.enums.PaymentEvent;
import dev.pawan.rupixo.common.enums.PaymentStatus;
import dev.pawan.rupixo.common.exception.BusinessRuleViolationException;
import dev.pawan.rupixo.common.exception.ResourceNotFoundException;
import dev.pawan.rupixo.payment.dto.request.PaymentInitRequest;
import dev.pawan.rupixo.payment.dto.response.PaymentResponse;
import dev.pawan.rupixo.payment.entity.OrderRecord;
import dev.pawan.rupixo.payment.entity.Payment;
import dev.pawan.rupixo.payment.gateway.PaymentGatewayAdapterRouter;
import dev.pawan.rupixo.payment.gateway.dto.PaymentRequest;
import dev.pawan.rupixo.payment.gateway.dto.PaymentResult;
import dev.pawan.rupixo.payment.mapper.PaymentMapper;
import dev.pawan.rupixo.payment.repository.OrderRepository;
import dev.pawan.rupixo.payment.repository.PaymentRepository;
import dev.pawan.rupixo.payment.service.PaymentService;
import dev.pawan.rupixo.payment.statemachine.PaymentTransitionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayAdapterRouter paymentAdapterRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionLogService paymentTransitionLogService;

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
                .idempotencyKey(UUID.randomUUID().toString())
                .method(paymentInitRequest.paymentMethod())
                .methodDetails(paymentInitRequest.methodDetails())
                .build();
        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = paymentMapper.toPaymentRequest(payment);

        paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        PaymentResult result = paymentAdapterRouter.initiate(paymentRequest);

        switch (result){
            case PaymentResult.Failed failed -> {
                paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failed.errorCode());
                payment.setErrorDescription(failed.errorDescription());
            }
            case PaymentResult.Pending pending -> {
                payment.setProcessorReference(pending.paymentRegistrationRef());
            }
            case PaymentResult.Success success -> {
                paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
                payment.setBankReference(success.bankReference());
            }
        }

        payment = paymentRepository.save(payment);

        //TODO: send Kafka event for initiation

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentAdapterRouter.capture(payment.getMethod(), paymentId);

        switch(paymentResult){
            case PaymentResult.Success  success -> {
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                log.info("Payment captured, paymentId: {}", paymentId);
            }
            case PaymentResult.Failed failed -> {
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failed.errorCode());
                payment.setErrorDescription(failed.errorDescription());
                log.info("Payment failed, paymentId: {}", paymentId);
            }
            case PaymentResult.Pending pending -> {}
        }

        payment = paymentRepository.save(payment);

        //TODO: send Kafka event for capture

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean isSuccessful, String bankRef, String errorCode, String errorMessage) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        // Implementation for resolving authorization

        if(payment.getStatus() != PaymentStatus.AUTHORIZING){
            log.warn("Payment {} is not in AUTHORIZING state. Current state: {}", paymentId, payment.getStatus());
            return;
        }

        OrderRecord orderRecord = payment.getOrder();
        if(isSuccessful) {
            payment.setBankReference(bankRef);
            payment.setStatus(PaymentStatus.AUTHORIZED);

            //Auto capture if the order is set to auto-capture
            paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentAdapterRouter.capture(payment.getMethod(), paymentId);

            if(captureResult instanceof PaymentResult.Success) {
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                orderRecord.setOrderStatus(OrderStatus.PAID);
            } else if(captureResult instanceof PaymentResult.Failed(String code, String errorDescription)) {
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(code);
                payment.setErrorDescription(errorDescription);
                log.info("Payment capture failed, paymentId: {}", paymentId);
            }
        } else {
            paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorMessage);
        }

        paymentRepository.save(payment);
        orderRepository.save(orderRecord);
    }
}
