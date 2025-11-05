package com.collega.paymentgatewaycip.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.collega.paymentgatewaycip.dto.BillerRequest;
import com.collega.paymentgatewaycip.dto.BillerResponse;
import com.collega.paymentgatewaycip.dto.DebitRequest;
import com.collega.paymentgatewaycip.dto.DebitResponse;
import com.collega.paymentgatewaycip.dto.PaymentRequest;
import com.collega.paymentgatewaycip.dto.PaymentResponse;
import com.collega.paymentgatewaycip.enums.StatusEnum;
import com.collega.paymentgatewaycip.feignclient.BillerClient;
import com.collega.paymentgatewaycip.feignclient.CoreBankingClient;
import com.collega.paymentgatewaycip.mapper.TransactionMapper;
import com.collega.paymentgatewaycip.model.Transaction;
import com.collega.paymentgatewaycip.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final TransactionRepository transactionRepository;

    private final ValidationService validationService;

    private final CoreBankingClient coreBankingClient;

    private final BillerClient billerClient;

    @Transactional
    public PaymentResponse create(PaymentRequest request) {
        validationService.validate(request);
        validationService.validateChannel(request.getChannel());

        if (transactionRepository.existsByOrderId(request.getOrderId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Transaction with Order ID: " + request.getOrderId() + " already exists");
        } 

        Transaction transaction = TransactionMapper.toModel(request);
        transaction.setStatus(StatusEnum.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);

        DebitRequest debitRequest = new DebitRequest(request.getAccount(), request.getAmount());
        DebitResponse debitResponse = coreBankingClient.debit(debitRequest);
        if (debitResponse.getStatus().equals("FAILED")) {
            return handleFailure(transaction, "Insufficient balance");
        }

        BillerRequest billerRequest = new BillerRequest(request.getOrderId(), request.getAmount(), request.getPaymentMethod());
        BillerResponse billerResponse = billerClient.pay(billerRequest);
        if (billerResponse.getStatus().equals("FAILED")) {
            return handleFailure(transaction, "Payment failed");
        }

        transaction.setCorebankReference(debitResponse.getCorebankReference());
        transaction.setBillerReference(billerResponse.getBillerReference());
        transaction.setStatus(StatusEnum.SUCCESS);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // TODO Publish event to Kafka

        return TransactionMapper.toPaymentResponse(transaction);
    }

    private PaymentResponse handleFailure(Transaction transaction, String message) {
        transaction.setStatus(StatusEnum.FAILED);
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        return PaymentResponse.builder()
            .transactionId(transaction.getId().toString())
            .orderId(transaction.getOrderId())
            .status("FAILED")
            .message(message)
            .build();
    }

    public PaymentResponse get(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        return TransactionMapper.toPaymentResponse(transaction);
    }
}
