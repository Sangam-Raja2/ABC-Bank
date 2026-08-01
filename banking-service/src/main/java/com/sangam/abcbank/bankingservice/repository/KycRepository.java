package com.sangam.abcbank.bankingservice.repository;

import com.sangam.abcbank.bankingservice.model.KycDetails;
import com.sangam.abcbank.bankingservice.model.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KycRepository extends JpaRepository<KycDetails, String> {

    Optional<KycDetails> findByCustomerId(String customerId);

    boolean existsByCustomerId(String customerId);

    boolean existsByDocumentNumber(String documentNumber);

    List<KycDetails> findByStatus(KycStatus status);
}
