package com.sangam.abcbank.emiservice.service;

import com.sangam.abcbank.emiservice.dto.EmiCalculatorRequest;
import com.sangam.abcbank.emiservice.dto.EmiCalculatorResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard reducing-balance EMI calculation:
 *   EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 * where P = principal, r = monthly interest rate (annualRate / 12 / 100), n = tenure in months.
 * Falls back to a flat P/n split when the rate is 0.
 */
@Service
public class EmiCalculatorService {

    private static final int SCALE = 2;
    private static final MathContext MC = new MathContext(20);

    public EmiCalculatorResponse calculate(EmiCalculatorRequest request) {
        BigDecimal principal = request.principal();
        int n = request.tenureMonths();
        BigDecimal annualRate = request.annualInterestRate();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), MC); // /12/100

        BigDecimal emi;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            emi = principal.divide(BigDecimal.valueOf(n), SCALE, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRPowN = onePlusR.pow(n, MC);
            BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(onePlusRPowN, MC);
            BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE, MC);
            emi = numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
        }

        List<EmiCalculatorResponse.AmortizationRow> schedule = new ArrayList<>();
        BigDecimal balance = principal;
        BigDecimal totalPayment = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= n; month++) {
            BigDecimal interestComponent = balance.multiply(monthlyRate, MC).setScale(SCALE, RoundingMode.HALF_UP);
            BigDecimal principalComponent = emi.subtract(interestComponent);

            // Last installment absorbs any rounding drift so balance lands exactly at zero.
            if (month == n) {
                principalComponent = balance;
                emi = principalComponent.add(interestComponent);
            }

            balance = balance.subtract(principalComponent).setScale(SCALE, RoundingMode.HALF_UP);
            if (balance.compareTo(BigDecimal.ZERO) < 0) balance = BigDecimal.ZERO;

            totalPayment = totalPayment.add(emi);
            totalInterest = totalInterest.add(interestComponent);

            schedule.add(new EmiCalculatorResponse.AmortizationRow(
                    month, emi, principalComponent, interestComponent, balance));
        }

        BigDecimal headlineEmi = schedule.isEmpty() ? BigDecimal.ZERO : schedule.get(0).emi();

        return new EmiCalculatorResponse(
                principal, annualRate, n,
                headlineEmi,
                totalPayment.setScale(SCALE, RoundingMode.HALF_UP),
                totalInterest.setScale(SCALE, RoundingMode.HALF_UP),
                schedule
        );
    }
}
