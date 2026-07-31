package com.sangam.abcbank.emiservice.service;

import com.sangam.abcbank.emiservice.dto.EmiCalculationRequest;
import com.sangam.abcbank.emiservice.dto.EmiCalculationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard reducing-balance EMI calculator.
 *
 * EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
 *   P = principal
 *   R = monthly interest rate (annual rate / 12 / 100)
 *   N = tenure in months
 */
@Service
public class EmiCalculatorService {

    private static final int MONEY_SCALE = 2;
    private static final int CALC_SCALE = 10;

    public EmiCalculationResponse calculate(EmiCalculationRequest request) {
        return calculate(request.getPrincipalAmount(), request.getAnnualInterestRate(), request.getTenureMonths());
    }

    public EmiCalculationResponse calculate(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        BigDecimal emi = calculateEmiAmount(principal, annualInterestRate, tenureMonths);

        List<EmiCalculationResponse.AmortizationEntry> schedule = new ArrayList<>();
        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(1200), CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal outstanding = principal;
        BigDecimal totalPayment = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interestComponent = outstanding.multiply(monthlyRate)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal principalComponent;
            BigDecimal emiForThisMonth;
            if (i == tenureMonths) {
                // last installment: pay off whatever remains exactly, avoiding rounding drift
                principalComponent = outstanding;
                emiForThisMonth = principalComponent.add(interestComponent);
            } else {
                principalComponent = emi.subtract(interestComponent).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                emiForThisMonth = emi;
            }

            outstanding = outstanding.subtract(principalComponent).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
                outstanding = BigDecimal.ZERO;
            }

            totalPayment = totalPayment.add(emiForThisMonth);
            totalInterest = totalInterest.add(interestComponent);

            schedule.add(new EmiCalculationResponse.AmortizationEntry(
                    i, emiForThisMonth, principalComponent, interestComponent, outstanding));
        }

        return new EmiCalculationResponse(
                principal,
                annualInterestRate,
                tenureMonths,
                emi,
                totalPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                totalInterest.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                schedule
        );
    }

    /** Just the EMI amount, no schedule - used by the auto-payment scheduler. */
    public BigDecimal calculateEmiAmount(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure months must be positive");
        }

        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(1200), CALC_SCALE, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // 0% interest loan - straight division
            return principal.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, new MathContext(20));

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
