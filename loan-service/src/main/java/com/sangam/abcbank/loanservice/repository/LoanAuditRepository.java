package com.sangam.abcbank.loanservice.repository;

import com.sangam.abcbank.loanservice.model.LoanAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanAuditRepository extends JpaRepository<LoanAudit, Long> {
}
