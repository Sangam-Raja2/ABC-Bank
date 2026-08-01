package com.sangam.abcbank.bankingservice.dto;

import com.sangam.abcbank.bankingservice.model.CardStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
public class CardIssueResponse {
    private Long id;
    private String cardNumber;
    private String cvv;       // shown once, at issuance only
    private String cardHolderName;
    private YearMonth expiryDate;
    private BigDecimal dailyLimit;
    private CardStatus status;
}