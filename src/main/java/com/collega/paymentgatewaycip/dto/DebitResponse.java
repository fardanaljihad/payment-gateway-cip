package com.collega.paymentgatewaycip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class DebitResponse {
    
    private String corebankReference;
    private String status;
}
