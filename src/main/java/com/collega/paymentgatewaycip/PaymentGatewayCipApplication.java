package com.collega.paymentgatewaycip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentGatewayCipApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayCipApplication.class, args);
	}

}
