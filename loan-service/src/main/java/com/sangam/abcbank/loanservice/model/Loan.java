package com.sangam.abcbank.loanservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @Column(nullable = false, unique = true, length = 20)
    private String loanAccountNumber;

    private String ownerUsername;

    private BigDecimal loanAmount;

    private Integer tenureInMonths;

    private BigDecimal annualInterestRate;

    private String loanPurpose;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

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

    private LocalDateTime updatedDate;

    private String disbursedByName;

    private String disbursedByRole;

    private LocalDateTime disbursedDate;
}