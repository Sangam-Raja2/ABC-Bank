package com.sangam.abcbank.emiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ASSUMPTION: sent to loan-service PATCH/PUT /api/loans/{loanId}/installment
 * to record that one EMI installment has been paid, and to update outstanding balance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmiInstallmentUpdateRequest {
    private BigDecimal amountPaid;
    private BigDecimal newOutstandingAmount;
}
