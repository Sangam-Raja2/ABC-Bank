package com.sangam.abcbank.bankingservice.repository;

import com.sangam.abcbank.bankingservice.model.Transaction;
import com.sangam.abcbank.bankingservice.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountNumberOrderByTimestampDesc(String accountNumber);
    List<Transaction> findByAccountNumberAndTypeAndTimestampBetween(
            String accountNumber, TransactionType type, LocalDateTime start, LocalDateTime end);
}
