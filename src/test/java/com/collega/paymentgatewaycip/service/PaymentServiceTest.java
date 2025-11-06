package com.collega.paymentgatewaycip.service;

import com.collega.paymentgatewaycip.dto.*;
import com.collega.paymentgatewaycip.enums.StatusEnum;
import com.collega.paymentgatewaycip.feignclient.CoreBankingClient;
import com.collega.paymentgatewaycip.model.Transaction;
import com.collega.paymentgatewaycip.producer.EventProducer;
import com.collega.paymentgatewaycip.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private CoreBankingClient coreBankingClient;

    @Mock
    private BillerService billerService;

    @Mock
    private EventProducer eventProducer;

    private UUID fakeId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        fakeId = UUID.randomUUID();

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            if (transaction.getId() == null) {
                transaction.setId(fakeId);
            }
            return transaction;
        });
    }

    @Test
    void createPayment_successfulTransaction() {

        PaymentRequest request = new PaymentRequest();
        request.setOrderId("INV-TEST-12345");
        request.setChannel("MOBILE_BANKING");
        request.setAmount(BigDecimal.valueOf(25000));
        request.setCurrency("IDR");
        request.setPaymentMethod("VIRTUAL_ACCOUNT");
        request.setAccount("TEST-12345");

        when(transactionRepository.existsByOrderId(request.getOrderId())).thenReturn(false);

        DebitResponse debitResponse = DebitResponse.builder()
                .status("SUCCESS")
                .corebankReference("CB-TEST-12345")
                .build();
        when(coreBankingClient.debit(any(DebitRequest.class))).thenReturn(debitResponse);

        BillerResponse billerResponse = BillerResponse.builder()
                .status("SUCCESS")
                .billerReference("BILLER-TEST-12345")
                .build();
        when(billerService.pay(any(BillerRequest.class))).thenReturn(billerResponse);

        PaymentResponse response = paymentService.create(request);

        
        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getCorebankReference());
        assertNotNull(response.getBillerReference());
        

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(eventProducer, times(1)).publishEvent(eq("transaction.success"), any(PaymentResponse.class));
        verify(coreBankingClient, times(1)).debit(any(DebitRequest.class));
        verify(billerService, times(1)).pay(any(BillerRequest.class));
        verify(validationService, times(1)).validate(request);
        verify(validationService, times(1)).validateChannel(request.getChannel());
    }

    @Test
    void createPayment_invalidChannel_throwsIllegalArgumentException() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("INV-TEST-INVALID");
        request.setChannel("INVALID_CHANNEL");

        doThrow(new IllegalArgumentException("Invalid channel value: INVALID_CHANNEL. Accepted values: [MOBILE_BANKING, INTERNET_BANKING, ATM]"))
                .when(validationService).validateChannel(request.getChannel());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.create(request)
        );

        assertTrue(ex.getMessage().contains("Invalid channel value"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createPayment_transactionAlreadyExists_throwsException() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("INV-EXIST-123");

        when(transactionRepository.existsByOrderId(request.getOrderId())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            paymentService.create(request)
        );

        assertEquals("400 BAD_REQUEST \"Transaction with Order ID: INV-EXIST-123 already exists\"", ex.getMessage());

        verify(eventProducer, never()).publishEvent(anyString(), any());
    }

    @Test
    void createPayment_debitFailed_returnsFailedResponse() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("INV-TEST-12345");
        request.setChannel("MOBILE_BANKING");
        request.setAmount(BigDecimal.valueOf(25000));
        request.setCurrency("IDR");
        request.setPaymentMethod("VIRTUAL_ACCOUNT");
        request.setAccount("TEST-12345");

        when(transactionRepository.existsByOrderId(request.getOrderId())).thenReturn(false);

        DebitResponse debitResponse = DebitResponse.builder()
                .status("FAILED")
                .corebankReference(null)
                .build();
        when(coreBankingClient.debit(any(DebitRequest.class))).thenReturn(debitResponse);

        PaymentResponse response = paymentService.create(request);

        assertNotNull(response.getTransactionId());
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals("FAILED", response.getStatus());
        assertEquals("Insufficient balance", response.getMessage());

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(eventProducer, never()).publishEvent(anyString(), any());
    }

    @Test
    void createPayment_billerFailed_returnsFailedResponse() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("INV-TEST-12345");
        request.setChannel("MOBILE_BANKING");
        request.setAmount(BigDecimal.valueOf(25000));
        request.setCurrency("IDR");
        request.setPaymentMethod("VIRTUAL_ACCOUNT");
        request.setAccount("TEST-12345");

        when(transactionRepository.existsByOrderId(request.getOrderId())).thenReturn(false);

        DebitResponse debitResponse = DebitResponse.builder()
                .status("SUCCESS")
                .corebankReference("CB-OK")
                .build();
        when(coreBankingClient.debit(any(DebitRequest.class))).thenReturn(debitResponse);

        BillerResponse billerResponse = BillerResponse.builder()
                .status("FAILED")
                .billerReference("BILLER-FAILED")
                .build();
        when(billerService.pay(any(BillerRequest.class))).thenReturn(billerResponse);

        PaymentResponse response = paymentService.create(request);

        assertNotNull(response.getTransactionId());
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals("FAILED", response.getStatus());
        assertEquals("Biller is currently unavailable.", response.getMessage());

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(eventProducer, never()).publishEvent(anyString(), any());
    }

    @Test
    void get_existingTransaction_returnsPaymentResponse() {
        Transaction transaction = new Transaction();
        transaction.setId(fakeId);
        transaction.setOrderId("INV-GET-123");
        transaction.setStatus(StatusEnum.SUCCESS);
        transaction.setCorebankReference("CBTEST");
        transaction.setBillerReference("BILLERTEST");

        when(transactionRepository.findById(fakeId)).thenReturn(Optional.of(transaction));

        PaymentResponse response = paymentService.get(fakeId);

        assertNotNull(response);
        assertEquals(fakeId.toString(), response.getTransactionId());
        assertEquals(transaction.getOrderId(), response.getOrderId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(transaction.getCorebankReference(), response.getCorebankReference());
        assertEquals(transaction.getBillerReference(), response.getBillerReference());
    }

    @Test
    void get_transactionNotFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.get(id)
        );

        assertEquals("404 NOT_FOUND \"Transaction not found\"", ex.getMessage());
    }
}
