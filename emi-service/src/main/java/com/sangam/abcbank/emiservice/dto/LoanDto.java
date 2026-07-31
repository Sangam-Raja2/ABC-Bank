package com.sangam.abcbank.emiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ASSUMPTION: mirrors loan-service's GET /api/loans/{loanId} response body.
 * Adjust field names to match the actual Loan entity/DTO in loan-service once confirmed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private Long loanId;
    private String username;          // owner of the loan - used for ownership check
    private String accountNumber;     // linked banking-service account to debit EMIs from
    private BigDecimal principalAmount;
    private BigDecimal annualInterestRate; // e.g. 10.5 for 10.5%
    private Integer tenureMonths;
    private LocalDate startDate;
    private Integer installmentsPaid;
    private BigDecimal outstandingAmount;
    private String status;            // ACTIVE, CLOSED, DEFAULTED, etc.
}
