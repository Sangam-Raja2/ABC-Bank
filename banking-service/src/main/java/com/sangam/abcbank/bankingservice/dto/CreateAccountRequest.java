package com.sangam.abcbank.bankingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "accountHolderName is required")
    private String accountHolderName;

    @NotBlank(message = "accountType is required (SAVINGS or CURRENT)")
    private String accountType;

    @NotNull(message = "initialDeposit is required")
    @DecimalMin(value = "0.0", message = "initialDeposit cannot be negative")
    private BigDecimal initialDeposit;
}
