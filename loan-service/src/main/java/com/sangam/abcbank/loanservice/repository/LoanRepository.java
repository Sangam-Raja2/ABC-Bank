package com.sangam.abcbank.loanservice.repository;

import com.sangam.abcbank.loanservice.model.Loan;
import com.sangam.abcbank.loanservice.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByIdAndStatus(Long id, LoanStatus status);
}