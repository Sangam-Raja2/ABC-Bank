package com.sangam.abcbank.bankingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Data
@Builder
public class DebitCardResponse {
    private Long id;
    private String maskedCardNumber;
    private String accountNumber;
    private String cardHolderName;
    private YearMonth expiryDate;
    private String status;
    private BigDecimal dailyLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}