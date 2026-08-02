package com.sangam.abcbank.emiservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EmiCalculatorRequest(

        @NotNull @DecimalMin(value = "1", message = "principal must be greater than 0")
        BigDecimal principal,

        @NotNull @DecimalMin(value = "0", message = "annualInterestRate cannot be negative")
        BigDecimal annualInterestRate, // e.g. 12.5 for 12.5% per annum

        @NotNull @Min(value = 1, message = "tenureMonths must be at least 1")
        Integer tenureMonths
) {}
