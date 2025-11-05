package com.collega.paymentgatewaycip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    
    private String transactionId;

    private String orderId;

    private String status;

    private String corebankReference;

    private String billerReference;

    private String message;
}
