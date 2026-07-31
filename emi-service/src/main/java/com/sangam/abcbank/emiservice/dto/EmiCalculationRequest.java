package com.sangam.abcbank.emiservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** Standalone EMI calculator input - usable without an existing loan record. */
@Data
public class EmiCalculationRequest {

    @NotNull
    @DecimalMin(value = "1", message = "Principal must be greater than 0")
    private BigDecimal principalAmount;

    @NotNull
    @DecimalMin(value = "0", message = "Interest rate cannot be negative")
    private BigDecimal annualInterestRate; // e.g. 9.5 for 9.5% per annum

    @NotNull
    @Min(value = 1, message = "Tenure must be at least 1 month")
    private Integer tenureMonths;
}
