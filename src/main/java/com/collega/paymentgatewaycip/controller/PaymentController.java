package com.collega.paymentgatewaycip.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collega.paymentgatewaycip.dto.PaymentRequest;
import com.collega.paymentgatewaycip.dto.PaymentResponse;
import com.collega.paymentgatewaycip.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Tag(name = "Payment Gateway", description = "REST API for managing payment transactions")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    
    @Operation(summary = "Create a new payment transaction")
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        LOGGER.debug("Received payment transaction request for orderId={}, amount={}", request.getOrderId(), request.getAmount());
        PaymentResponse response = paymentService.create(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Retreive the status of a transaction")
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable UUID id) {
        LOGGER.debug("Retrieve the status of transaction for transactionId={}", id);
        PaymentResponse response = paymentService.get(id);
        return ResponseEntity.ok().body(response);
    }
}
