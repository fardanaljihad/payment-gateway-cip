package com.collega.paymentgatewaycip.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.collega.paymentgatewaycip.dto.BillerRequest;
import com.collega.paymentgatewaycip.dto.BillerResponse;

@FeignClient(name = "${external.biller.name}", url = "${external.biller.url}", path = "${external.biller.path}")
public interface BillerClient {
    
    @PostMapping("/pay")
    public BillerResponse pay(@RequestBody BillerRequest request);
}
