package com.collega.paymentgatewaycip.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.collega.paymentgatewaycip.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    boolean existsByOrderId(String orderId);
}
