package com.sangam.abcbank.loanservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

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

    private Set<String> reviewersRole;

    private LocalDateTime reviewedDate;

    @Column(columnDefinition = "TEXT")
    private String reviewalRemarks;

    private String approvedByName;

    private Set<String> approversRole;

    private LocalDateTime approvedDate;

    @Column(columnDefinition = "TEXT")
    private String approvalRemarks;

    private String rejectedByName;

    private Set<String> rejectorsRole;

    private LocalDateTime rejectedDate;

    @Column(columnDefinition = "TEXT")
    private String rejectedRemarks;

    private LocalDateTime updatedDate;

    private String disbursedByName;

    private Set<String> disbursedByRole;

    private LocalDateTime disbursedDate;
}