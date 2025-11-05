package com.collega.paymentgatewaycip.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.collega.paymentgatewaycip.dto.DebitRequest;
import com.collega.paymentgatewaycip.dto.DebitResponse;


@FeignClient(name = "${external.corebank.name}", url = "${external.corebank.url}", path = "${external.corebank.path}")
public interface CoreBankingClient {

    @PostMapping("/debit")
    public DebitResponse debit(@RequestBody DebitRequest request); 
}
