package com.sangam.abcbank.emiservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmiCalculatorResponse(
        BigDecimal principal,
        BigDecimal annualInterestRate,
        int tenureMonths,
        BigDecimal monthlyEmi,
        BigDecimal totalPayment,
        BigDecimal totalInterest,
        List<AmortizationRow> schedule
) {
    public record AmortizationRow(
            int month,
            BigDecimal emi,
            BigDecimal principalComponent,
            BigDecimal interestComponent,
            BigDecimal remainingBalance
    ) {}
}
