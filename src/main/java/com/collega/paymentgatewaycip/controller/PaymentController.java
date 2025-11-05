package com.collega.paymentgatewaycip.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collega.paymentgatewaycip.dto.PaymentRequest;
import com.collega.paymentgatewaycip.dto.PaymentResponse;
import com.collega.paymentgatewaycip.service.PaymentService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;
    
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.create(request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable UUID id) {
        PaymentResponse response = paymentService.get(id);
        return ResponseEntity.ok().body(response);
    }
}
