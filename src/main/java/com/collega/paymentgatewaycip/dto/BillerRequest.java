package com.collega.paymentgatewaycip.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillerRequest {
    
    private String orderId;
    private BigDecimal amount;
    private String paymentMethod;
}
