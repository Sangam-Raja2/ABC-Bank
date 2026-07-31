package com.sangam.abcbank.emiservice.service;

import com.sangam.abcbank.emiservice.client.LoanServiceClient;
import com.sangam.abcbank.emiservice.dto.LoanDto;
import com.sangam.abcbank.emiservice.entity.EmiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Daily job: for every ACTIVE loan due for its next installment, recalculate the EMI,
 * check banking-service balance, and auto-debit if funds are sufficient.
 *
 * ASSUMPTION: loan-service exposes GET /api/loans/due-today (or similar) returning all
 * ACTIVE loans whose next installment is due. If loan-service doesn't have that endpoint
 * yet, add it there (recommended), or replace getLoansDueToday() below with a paginated
 * scan of /api/loans?status=ACTIVE and compute "due" locally from startDate + installmentsPaid.
 *
 * Since this runs without an inbound user request, FeignAuthInterceptor has nothing to
 * propagate - configure loan-service/banking-service to accept a trusted service-to-service
 * credential (e.g. a client-credentials JWT for "emi-service", or mutual TLS) for these calls.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AutoPaymentScheduler {

    private final LoanServiceClient loanServiceClient;
    private final EmiService emiService;

    @Value("${emi.auto-payment.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${emi.auto-payment.cron}")
    public void runDailyAutoPayment() {
        if (!enabled) {
            log.info("EMI auto-payment scheduler is disabled (emi.auto-payment.enabled=false)");
            return;
        }

        log.info("Starting daily EMI auto-payment run");

        java.util.List<LoanDto> dueLoans;
        try {
            dueLoans = fetchDueLoans();
        } catch (Exception ex) {
            log.error("Could not fetch due loans from loan-service, aborting this run: {}", ex.getMessage());
            return;
        }

        int paid = 0, insufficientFunds = 0, failed = 0;

        for (LoanDto loan : dueLoans) {
            try {
                EmiPayment result = emiService.payNextEmi(loan.getLoanId());
                switch (result.getStatus()) {
                    case PAID -> paid++;
                    case FAILED_INSUFFICIENT_FUNDS -> insufficientFunds++;
                    default -> failed++;
                }
            } catch (Exception ex) {
                failed++;
                log.error("Auto-payment failed for loan {}: {}", loan.getLoanId(), ex.getMessage());
            }
        }

        log.info("Daily EMI auto-payment run complete: paid={}, insufficientFunds={}, failed={}, total={}",
                paid, insufficientFunds, failed, dueLoans.size());
    }

    private java.util.List<LoanDto> fetchDueLoans() {
        // Placeholder call - swap for the real loan-service endpoint once confirmed.
        // e.g. return loanServiceClient.getLoansDueToday();
        throw new UnsupportedOperationException(
                "Wire this up to loan-service's 'due today' / active-loans endpoint once confirmed");
    }
}
