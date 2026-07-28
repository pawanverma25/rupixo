package dev.pawan.rupixo.payment.controller;

import dev.pawan.rupixo.payment.dto.request.PaymentInitRequest;
import dev.pawan.rupixo.payment.dto.response.PaymentResponse;
import dev.pawan.rupixo.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/v1/payment")
@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    UUID merchantId = UUID.fromString("23c2c6d6-b97b-4b57-b5de-b839427e0386"); //TODO : replace with security

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody @Valid PaymentInitRequest paymentInitRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(
                paymentService.initiate(merchantId, paymentInitRequest));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId){
        return ResponseEntity.ok(
                paymentService.capture(merchantId, paymentId));
    }

}
