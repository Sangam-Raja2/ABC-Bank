package com.sangam.abcbank.loanservice.dto;

import com.sangam.abcbank.loanservice.model.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponse {

    private Long loanId;

    private String ownerUsername;

    private BigDecimal loanAmount;

    private Integer tenureInMonths;

    private BigDecimal annualInterestRate;

    private String loanPurpose;

    private String status;

    private LocalDateTime appliedDate;

    private String approvedByName;

    private LocalDateTime approvedDate;

    private String approvalRemarks;

    private String disbursedByName;

    private LocalDateTime disbursedDate;

}
