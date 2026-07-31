package com.sangam.abcbank.emiservice.service;


import com.sangam.abcbank.emiservice.client.BankingServiceClient;
import com.sangam.abcbank.emiservice.client.LoanServiceClient;
import com.sangam.abcbank.emiservice.dto.*;
import com.sangam.abcbank.emiservice.entity.EmiPayment;
import com.sangam.abcbank.emiservice.exception.ResourceNotFoundException;
import com.sangam.abcbank.emiservice.repository.EmiPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiService {

    private final LoanServiceClient loanServiceClient;
    private final BankingServiceClient bankingServiceClient;
    private final EmiCalculatorService emiCalculatorService;
    private final EmiPaymentRepository emiPaymentRepository;

    /** Fetch the loan (access already checked by @PreAuthorize at the controller). */
    public LoanDto getLoan(Long loanId) {
        LoanDto loan = loanServiceClient.getLoanById(loanId);
        if (loan == null) {
            throw new ResourceNotFoundException("Loan not found with id: " + loanId);
        }
        return loan;
    }

    /** Calculate the EMI for an existing loan by pulling its terms from loan-service. */
    public EmiCalculationResponse calculateEmiForLoan(Long loanId) {
        LoanDto loan = getLoan(loanId);
        return emiCalculatorService.calculate(
                loan.getPrincipalAmount(), loan.getAnnualInterestRate(), loan.getTenureMonths());
    }

    /**
     * Attempts to pay the next due EMI for a loan right now:
     *  1. Recalculate the EMI amount from loan terms.
     *  2. Check banking-service for available balance on the linked account.
     *  3. If sufficient, debit the account and tell loan-service the installment is paid.
     *  4. If insufficient, record a PENDING/FAILED ledger entry and stop - no debit is attempted.
     *
     * This is idempotent per (loanId, installmentNumber): calling it twice for the same
     * installment number will not double-charge, thanks to the DB unique check below.
     */
    @Transactional
    public EmiPayment payNextEmi(Long loanId) {
        LoanDto loan = getLoan(loanId);

        if (!"ACTIVE".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Loan " + loanId + " is not ACTIVE (status=" + loan.getStatus() + ")");
        }

        int nextInstallmentNumber = (loan.getInstallmentsPaid() == null ? 0 : loan.getInstallmentsPaid()) + 1;

        if (emiPaymentRepository.existsByLoanIdAndInstallmentNumber(loanId, nextInstallmentNumber)) {
            throw new IllegalStateException(
                    "Installment " + nextInstallmentNumber + " for loan " + loanId + " was already processed");
        }

        BigDecimal emiAmount = emiCalculatorService.calculateEmiAmount(
                loan.getPrincipalAmount(), loan.getAnnualInterestRate(), loan.getTenureMonths());

        EmiPayment payment = EmiPayment.builder()
                .loanId(loanId)
                .username(loan.getUsername())
                .accountNumber(loan.getAccountNumber())
                .installmentNumber(nextInstallmentNumber)
                .emiAmount(emiAmount)
                .dueDate(java.time.LocalDate.now())
                .status(EmiPayment.EmiPaymentStatus.PENDING)
                .build();

        return attemptAutoPayment(payment, loan);
    }

    /**
     * Core auto-debit logic, reused by both the on-demand payNextEmi() call and the
     * scheduled batch job. Checks balance FIRST, only debits if sufficient.
     */
    @Transactional
    public EmiPayment attemptAutoPayment(EmiPayment payment, LoanDto loan) {
        BankAccountDto account;
        try {
            account = bankingServiceClient.getAccount(payment.getAccountNumber());
        } catch (Exception ex) {
            log.error("Failed to fetch account {} balance from banking-service for loan {}: {}",
                    payment.getAccountNumber(), payment.getLoanId(), ex.getMessage());
            payment.setStatus(EmiPayment.EmiPaymentStatus.FAILED_ERROR);
            payment.setFailureReason("Could not reach banking-service: " + ex.getMessage());
            payment.setProcessedAt(LocalDateTime.now());
            return emiPaymentRepository.save(payment);
        }

        if (account == null || !"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            payment.setStatus(EmiPayment.EmiPaymentStatus.FAILED_ERROR);
            payment.setFailureReason("Linked account is missing or not ACTIVE");
            payment.setProcessedAt(LocalDateTime.now());
            return emiPaymentRepository.save(payment);
        }

        // ---- THE KEY CHECK: only auto-pay if balance is sufficient ----
        if (account.getBalance() == null || account.getBalance().compareTo(payment.getEmiAmount()) < 0) {
            log.warn("Insufficient balance for loan {} installment {}: available={}, required={}",
                    payment.getLoanId(), payment.getInstallmentNumber(), account.getBalance(), payment.getEmiAmount());
            payment.setStatus(EmiPayment.EmiPaymentStatus.FAILED_INSUFFICIENT_FUNDS);
            payment.setFailureReason("Available balance " + account.getBalance()
                    + " is less than EMI amount " + payment.getEmiAmount());
            payment.setProcessedAt(LocalDateTime.now());
            return emiPaymentRepository.save(payment);
        }

        // Sufficient balance - proceed to debit.
        String referenceId = "EMI-" + payment.getLoanId() + "-" + payment.getInstallmentNumber();
        DebitRequest debitRequest = new DebitRequest(
                payment.getEmiAmount(),
                "EMI installment #" + payment.getInstallmentNumber() + " for loan " + payment.getLoanId(),
                referenceId);

        DebitResponse debitResponse;
        try {
            debitResponse = bankingServiceClient.debitAccount(payment.getAccountNumber(), debitRequest);
        } catch (Exception ex) {
            log.error("Debit call failed for loan {} installment {}: {}",
                    payment.getLoanId(), payment.getInstallmentNumber(), ex.getMessage());
            payment.setStatus(EmiPayment.EmiPaymentStatus.FAILED_ERROR);
            payment.setFailureReason("Debit call failed: " + ex.getMessage());
            payment.setProcessedAt(LocalDateTime.now());
            return emiPaymentRepository.save(payment);
        }

        if (debitResponse == null || !"SUCCESS".equalsIgnoreCase(debitResponse.getStatus())) {
            payment.setStatus(EmiPayment.EmiPaymentStatus.FAILED_INSUFFICIENT_FUNDS);
            payment.setFailureReason("banking-service declined debit: "
                    + (debitResponse == null ? "no response" : debitResponse.getStatus()));
            payment.setProcessedAt(LocalDateTime.now());
            return emiPaymentRepository.save(payment);
        }

        // Debit succeeded - update loan-service, then finalize our ledger entry.
        BigDecimal newOutstanding = loan.getOutstandingAmount() != null
                ? loan.getOutstandingAmount().subtract(payment.getEmiAmount())
                : null;

        try {
            loanServiceClient.recordInstallmentPaid(payment.getLoanId(),
                    new EmiInstallmentUpdateRequest(payment.getEmiAmount(), newOutstanding));
        } catch (Exception ex) {
            // Money has already moved - do NOT mark this as failed, but flag loudly for reconciliation.
            log.error("CRITICAL: debited {} for loan {} but failed to update loan-service. "
                            + "Manual reconciliation required. txnId={}, error={}",
                    payment.getEmiAmount(), payment.getLoanId(), debitResponse.getTransactionId(), ex.getMessage());
            payment.setFailureReason("Debited successfully (txn=" + debitResponse.getTransactionId()
                    + ") but loan-service update failed - needs reconciliation: " + ex.getMessage());
        }

        payment.setStatus(EmiPayment.EmiPaymentStatus.PAID);
        payment.setBankingTransactionId(debitResponse.getTransactionId());
        payment.setProcessedAt(LocalDateTime.now());
        return emiPaymentRepository.save(payment);
    }
}
