package com.sangam.abcbank.emiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ASSUMPTION: mirrors banking-service's GET /api/accounts/{accountNumber} response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDto {
    private String accountNumber;
    private String username;
    private BigDecimal balance;
    private String status; // ACTIVE, FROZEN, CLOSED
}
