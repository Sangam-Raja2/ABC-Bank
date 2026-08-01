package com.sangam.abcbank.creditcardservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IssueCreditCardRequest(

        @NotNull
        @DecimalMin(value = "1000.00")
        BigDecimal creditLimit,

        @NotNull
        @Min(1)
        @Max(28)
        Integer billingCycleDay
) {}
