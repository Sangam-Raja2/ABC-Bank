package com.sangam.abcbank.emiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ASSUMPTION: sent to banking-service POST /api/accounts/{accountNumber}/debit
 * to withdraw the EMI amount. Adjust to actual banking-service transaction DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebitRequest {
    private BigDecimal amount;
    private String remarks;
    private String referenceId; // idempotency key, e.g. "EMI-<loanId>-<installmentNo>"
}
