package com.sangam.abcbank.bankingservice.repository;

import com.sangam.abcbank.bankingservice.model.CardStatus;
import com.sangam.abcbank.bankingservice.model.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    Optional<DebitCard> findByCardNumber(String cardNumber);
    List<DebitCard> findByOwnerUsername(String ownerUsername);
    boolean existsByAccountNumberAndStatus(String accountNumber, CardStatus status);
}