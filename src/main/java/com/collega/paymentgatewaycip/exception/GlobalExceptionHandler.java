package com.collega.paymentgatewaycip.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.collega.paymentgatewaycip.dto.PaymentResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PaymentResponse> constraintViolationException(
        ConstraintViolationException ex, HttpServletRequest req) {

        LOGGER.warn("Constraint violation: {} on path {}", ex.getMessage(), req.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            PaymentResponse.builder()
                .status("FAILED")
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentResponse> handleIllegalArgumentException(
        IllegalArgumentException ex, HttpServletRequest req) {

        LOGGER.warn("Illegal argument: {} on path {}", ex.getMessage(), req.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            PaymentResponse.builder()
                .status("FAILED")
                .message(ex.getMessage())
                .build()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<PaymentResponse> responseStatusException(
        ResponseStatusException ex, HttpServletRequest req) {

        LOGGER.warn("Bad request: {} on path {}", ex.getReason(), req.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            PaymentResponse.builder()
                .message(ex.getReason())
                .build()
        );
    }
}
