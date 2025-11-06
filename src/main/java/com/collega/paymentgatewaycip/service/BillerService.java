package com.collega.paymentgatewaycip.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.collega.paymentgatewaycip.dto.BillerRequest;
import com.collega.paymentgatewaycip.dto.BillerResponse;
import com.collega.paymentgatewaycip.feignclient.BillerClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillerService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BillerService.class);

    private final BillerClient billerClient;

    @CircuitBreaker(name = "billerBreaker", fallbackMethod = "billerFallback") // Gunakan @Retry untuk mengaktifkan retry mechanism
    public BillerResponse pay(BillerRequest request) {
        return billerClient.pay(request);
    }

    public BillerResponse billerFallback(BillerRequest request, Throwable ex) {
        LOGGER.warn("Fallback biller request for orderId={} : {}", request.getOrderId(), ex.getMessage());
        return BillerResponse.builder()
                .status("FAILED")
                .build();
    }
}
