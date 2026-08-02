package com.sangam.abcbank.emiservice.dto;

import com.sangam.abcbank.emiservice.entity.EmiTransition.SourceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EmiTransitionRequest(

        @NotNull SourceType sourceType,               // CREDIT_CARD or LOAN

        @NotBlank String sourceReferenceId,            // credit card txn id, or loan account id

        @NotNull @DecimalMin(value = "1") BigDecimal amount,   // outstanding amount to convert

        @NotNull @DecimalMin(value = "0") BigDecimal annualInterestRate,

        @NotNull @Min(1) Integer tenureMonths
) {}
