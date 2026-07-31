package com.sangam.abcbank.emiservice.repository;

import com.sangam.abcbank.emiservice.entity.EmiPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmiPaymentRepository extends JpaRepository<EmiPayment, Long> {

    List<EmiPayment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);

    List<EmiPayment> findByStatusAndDueDateLessThanEqual(
            EmiPayment.EmiPaymentStatus status, LocalDate dueDate);

    boolean existsByLoanIdAndInstallmentNumber(Long loanId, Integer installmentNumber);
}
