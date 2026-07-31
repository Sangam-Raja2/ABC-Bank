package com.sangam.abcbank.emiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmiCalculationResponse {
    private BigDecimal principalAmount;
    private BigDecimal annualInterestRate;
    private Integer tenureMonths;
    private BigDecimal monthlyEmi;
    private BigDecimal totalPayment;
    private BigDecimal totalInterest;
    private List<AmortizationEntry> schedule;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmortizationEntry {
        private int installmentNumber;
        private BigDecimal emiAmount;
        private BigDecimal principalComponent;
        private BigDecimal interestComponent;
        private BigDecimal remainingBalance;
    }
}
