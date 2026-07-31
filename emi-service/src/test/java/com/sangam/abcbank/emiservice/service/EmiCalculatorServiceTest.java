package com.sangam.abcbank.emiservice.service;

import com.sangam.abcbank.emiservice.dto.EmiCalculationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmiCalculatorServiceTest {

    private final EmiCalculatorService calculator = new EmiCalculatorService();

    @Test
    void calculatesKnownEmiCorrectly() {
        // Principal 1,00,000 at 10% p.a. for 12 months -> well-known EMI ~ 8791.59
        BigDecimal emi = calculator.calculateEmiAmount(
                new BigDecimal("100000"), new BigDecimal("10"), 12);

        assertEquals(new BigDecimal("8791.59"), emi);
    }

    @Test
    void zeroInterestLoanIsStraightDivision() {
        BigDecimal emi = calculator.calculateEmiAmount(
                new BigDecimal("12000"), BigDecimal.ZERO, 12);

        assertEquals(new BigDecimal("1000.00"), emi);
    }

    @Test
    void amortizationScheduleFullyPaysOffPrincipal() {
        EmiCalculationResponse response = calculator.calculate(
                new BigDecimal("50000"), new BigDecimal("12"), 6);

        BigDecimal lastRemaining = response.getSchedule()
                .get(response.getSchedule().size() - 1)
                .getRemainingBalance();

        assertEquals(0, lastRemaining.compareTo(BigDecimal.ZERO));
        assertEquals(6, response.getSchedule().size());
        assertTrue(response.getTotalInterest().compareTo(BigDecimal.ZERO) > 0);
    }
}
