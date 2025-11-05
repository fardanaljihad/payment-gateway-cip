package com.collega.paymentgatewaycip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillerResponse {
    
    private String billerReference;
    private String status;
}
