package com.collega.paymentgatewaycip.mapper;

import com.collega.paymentgatewaycip.dto.PaymentRequest;
import com.collega.paymentgatewaycip.dto.PaymentResponse;
import com.collega.paymentgatewaycip.enums.ChannelEnum;
import com.collega.paymentgatewaycip.model.Transaction;

public class TransactionMapper {
    
    public static Transaction toModel(PaymentRequest request) {
        Transaction  transaction = new Transaction();
        transaction.setOrderId(request.getOrderId());
        transaction.setChannel(ChannelEnum.valueOf(request.getChannel()));
        transaction.setAmount(request.getAmount());
        transaction.setAccount(request.getAccount());
        transaction.setCurrency(request.getCurrency());
        transaction.setPaymentMethod(request.getPaymentMethod());

        return transaction;
    }

    public static PaymentResponse toPaymentResponse(Transaction transaction) {
        return PaymentResponse.builder()
            .transactionId(transaction.getId().toString())
            .orderId(transaction.getOrderId())
            .status(transaction.getStatus().toString())
            .corebankReference(transaction.getCorebankReference())
            .billerReference(transaction.getBillerReference())
            .build();
    }
}
