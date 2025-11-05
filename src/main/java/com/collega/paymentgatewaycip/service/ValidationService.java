package com.collega.paymentgatewaycip.service;

import java.util.Arrays;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.collega.paymentgatewaycip.enums.ChannelEnum;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationService {
    
    private final Validator validator;

    public void validate(Object request) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(request);
        if (constraintViolations.size() != 0) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    public void validateChannel(String channel) {
        try {
            ChannelEnum.valueOf(channel);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                "Invalid channel value: " + channel + ". Accepted values: " + Arrays.toString(ChannelEnum.values())
            );
        }
    }
}
