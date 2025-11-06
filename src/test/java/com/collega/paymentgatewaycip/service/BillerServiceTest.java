package com.collega.paymentgatewaycip.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.collega.paymentgatewaycip.dto.BillerRequest;
import com.collega.paymentgatewaycip.dto.BillerResponse;
import com.collega.paymentgatewaycip.feignclient.BillerClient;

public class BillerServiceTest {

    @Mock
    private BillerClient billerClient;

    private BillerService billerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        billerService = new BillerService(billerClient);
    }

    @Test
    void pay_shouldReturnSuccessResponse() {
        BillerRequest request = new BillerRequest();
        BillerResponse expectedResponse = BillerResponse.builder()
                .billerReference("BILLER123")
                .status("SUCCESS")
                .build();

        when(billerClient.pay(request)).thenReturn(expectedResponse);

        BillerResponse actualResponse = billerService.pay(request);

        assertNotNull(actualResponse);
        assertEquals("BILLER123", actualResponse.getBillerReference());
        assertEquals("SUCCESS", actualResponse.getStatus());
        verify(billerClient, times(1)).pay(request);
    }

    @Test
    void pay_shouldTriggerFallbackOnException() {
        BillerRequest request = new BillerRequest();
        request.setOrderId("INV-12345");
        when(billerClient.pay(request)).thenThrow(new RuntimeException("Biller is currently unavailable."));

        BillerResponse fallbackResponse = billerService.billerFallback(request, new RuntimeException("Biller is currently unavailable."));

        assertNotNull(fallbackResponse);
        assertEquals("FAILED", fallbackResponse.getStatus());
        verify(billerClient, never()).pay(any());
    }
}
