package com.sangam.abcbank.bankingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class IssueCardRequest {
    private String accountNumber;
    private BigDecimal dailyLimit;

}
