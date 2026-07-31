package com.sangam.abcbank.loanservice.repository;

import com.sangam.abcbank.loanservice.model.LoanAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanAuditRepository extends JpaRepository<LoanAudit, Long> {
    List<LoanAudit> findByLoanAccountNumber(String loanAccountNumber);
}
