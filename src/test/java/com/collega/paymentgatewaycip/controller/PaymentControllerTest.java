package com.collega.paymentgatewaycip.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.collega.paymentgatewaycip.dto.PaymentRequest;
import com.collega.paymentgatewaycip.dto.PaymentResponse;
import com.collega.paymentgatewaycip.service.PaymentService;

public class PaymentControllerTest {
    
    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentService paymentService;

    private UUID fakeId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fakeId = UUID.randomUUID();
    }

    @Test
    void createPayment_success() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("INV-CTRL-123");
        request.setAccount("ACC-CTRL-123");
        request.setAmount(BigDecimal.valueOf(50000));
        request.setCurrency("IDR");
        request.setPaymentMethod("VIRTUAL_ACCOUNT");
        request.setChannel("MOBILE_BANKING");

        PaymentResponse serviceResponse = PaymentResponse.builder()
                .transactionId(fakeId.toString())
                .orderId(request.getOrderId())
                .status("SUCCESS")
                .corebankReference("CB-CTRL-123")
                .billerReference("BILLER-CTRL-123")
                .build();

        when(paymentService.create(request)).thenReturn(serviceResponse);

        ResponseEntity<PaymentResponse> responseEntity = paymentController.create(request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("SUCCESS", responseEntity.getBody().getStatus());
        assertEquals(request.getOrderId(), responseEntity.getBody().getOrderId());
        assertEquals(fakeId.toString(), responseEntity.getBody().getTransactionId());

        verify(paymentService, times(1)).create(request);
    }

    @Test
    void getPayment_success() {
        PaymentResponse serviceResponse = PaymentResponse.builder()
                .transactionId(fakeId.toString())
                .orderId("INV-CTRL-GET-123")
                .status("SUCCESS")
                .corebankReference("CB-GET-123")
                .billerReference("BILLER-GET-123")
                .build();

        when(paymentService.get(fakeId)).thenReturn(serviceResponse);

        ResponseEntity<PaymentResponse> responseEntity = paymentController.get(fakeId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("SUCCESS", responseEntity.getBody().getStatus());
        assertEquals(fakeId.toString(), responseEntity.getBody().getTransactionId());

        verify(paymentService, times(1)).get(fakeId);
    }
}
