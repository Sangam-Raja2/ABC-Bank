package com.sangam.abcbank.loanservice.dto;

import com.sangam.abcbank.loanservice.model.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponse {

    private String loanAccountNumber;

    private String ownerUsername;

    private BigDecimal loanAmount;

    private Integer tenureInMonths;

    private BigDecimal annualInterestRate;

    private String loanPurpose;

    private String status;

    private LocalDateTime appliedDate;

    private String reviewedByName;

    private String reviewersRole;

    private LocalDateTime reviewedDate;

    private String reviewalRemarks;

    private String approvedByName;

    private String approversRole;

    private LocalDateTime approvedDate;

    private String approvalRemarks;

    private String rejectedByName;

    private String rejectorsRole;

    private LocalDateTime rejectedDate;

    private String rejectedRemarks;

    private String disbursedByName;

    private String disbursedByRole;

    private LocalDateTime disbursedDate;

}
