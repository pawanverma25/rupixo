package dev.pawan.rupixo.payment.controller;

import dev.pawan.rupixo.payment.dto.request.CreateOrderRequest;
import dev.pawan.rupixo.payment.dto.response.OrderResponse;
import dev.pawan.rupixo.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    UUID merchantId = UUID.randomUUID(); //TODO: replace it after spring security impl.

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody  CreateOrderRequest orderRequest){
        return ResponseEntity.ok(orderService.create(merchantId, orderRequest));
    }


}
