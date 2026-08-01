package com.sangam.abcbank.creditcardservice.repository;

import com.sangam.abcbank.creditcardservice.model.CardStatus;
import com.sangam.abcbank.creditcardservice.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findAllByOwnerUsername(String ownerUsername);

    Optional<CreditCard> findByIdAndOwnerUsername(Long id, String ownerUsername);

    boolean existsByOwnerUsernameAndStatus(String ownerUsername, CardStatus status);
}
