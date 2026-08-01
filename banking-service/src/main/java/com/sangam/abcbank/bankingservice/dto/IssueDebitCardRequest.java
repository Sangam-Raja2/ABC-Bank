package com.sangam.abcbank.bankingservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IssueDebitCardRequest {

    @NotBlank
    private String accountNumber;


    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal dailyLimit;
}