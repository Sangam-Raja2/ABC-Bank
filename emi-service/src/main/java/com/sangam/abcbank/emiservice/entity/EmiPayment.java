package com.sangam.abcbank.emiservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * emi-service's own ledger of attempted/completed EMI payments.
 * This is local bookkeeping - the source of truth for the loan balance still
 * lives in loan-service, and the source of truth for money movement lives in banking-service.
 */
@Entity
@Table(name = "emi_payments", indexes = {
        @Index(name = "idx_loan_id", columnList = "loanId"),
        @Index(name = "idx_due_date_status", columnList = "dueDate,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmiPaymentStatus status; // PENDING, PAID, FAILED_INSUFFICIENT_FUNDS, FAILED_ERROR

    private String bankingTransactionId;

    private String failureReason;

    private LocalDateTime processedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EmiPaymentStatus.PENDING;
        }
    }

    public enum EmiPaymentStatus {
        PENDING,
        PAID,
        FAILED_INSUFFICIENT_FUNDS,
        FAILED_ERROR
    }
}
