package com.sangam.abcbank.emiservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebitResponse {
    private String transactionId;
    private String status;       // SUCCESS, FAILED, INSUFFICIENT_FUNDS
    private BigDecimal balanceAfter;
}
