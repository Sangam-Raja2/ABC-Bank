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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerUsername;

    private BigDecimal loanAmount;

    private Integer tenureInMonths;

    private BigDecimal annualInterestRate;

    private String loanPurpose;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private LocalDateTime appliedDate;


    private String approvedByName;

    private LocalDateTime approvedDate;

    private String approvalRemarks;

    private LocalDateTime updatedDate;
}