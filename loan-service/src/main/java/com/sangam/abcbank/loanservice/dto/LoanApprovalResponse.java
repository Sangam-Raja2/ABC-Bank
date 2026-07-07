package com.sangam.abcbank.loanservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanApprovalResponse {

    private Long loanId;


    private String customerName;

    private BigDecimal loanAmount;

    private Integer tenureInMonths;

    private BigDecimal annualInterestRate;

    private String loanPurpose;

    private String status;

    private String approvedByName;

    private LocalDateTime approvedDate;

    private String approvalRemarks;
}
