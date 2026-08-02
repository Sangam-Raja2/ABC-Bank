package com.sangam.abcbank.emiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "emi_transitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmiTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;              // owner, taken from JWT subject

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;        // CREDIT_CARD or LOAN

    private String sourceReferenceId;     // e.g. credit card transaction id, or loan account id

    private BigDecimal principal;
    private BigDecimal annualInterestRate;
    private Integer tenureMonths;
    private BigDecimal monthlyEmi;

    @Enumerated(EnumType.STRING)
    private TransitionStatus status;      // PENDING, CONFIRMED, FAILED, CANCELLED

    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum SourceType { CREDIT_CARD, LOAN }
    public enum TransitionStatus { PENDING, CONFIRMED, FAILED, CANCELLED }
}
