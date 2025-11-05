package com.collega.paymentgatewaycip.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.collega.paymentgatewaycip.dto.PaymentResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PaymentResponse> constraintViolationException(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            PaymentResponse.builder()
                .status("FAILED")
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            PaymentResponse.builder()
                .status("FAILED")
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<PaymentResponse> responseStatusException(ResponseStatusException ex) {

        String status = "FAILED";

        if (ex.getReason().equals("Transaction not found")) {
            status = null;
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            PaymentResponse.builder()
                .status(status)
                .message(ex.getReason())
                .build()
        );
    }
}
