package com.sangam.abcbank.loanservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequest {

    @NotNull
    @DecimalMin(value = "1000")
    private BigDecimal loanAmount;

    @NotNull
    @Min(6)
    @Max(360)
    private Integer tenureInMonths;

    @NotNull
    @DecimalMin(value = "1")
    private BigDecimal annualInterestRate;

    @NotBlank
    private String loanPurpose;
}