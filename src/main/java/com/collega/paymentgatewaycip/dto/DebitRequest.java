package com.collega.paymentgatewaycip.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DebitRequest {
    
    private String account;
    private BigDecimal amount;
}
