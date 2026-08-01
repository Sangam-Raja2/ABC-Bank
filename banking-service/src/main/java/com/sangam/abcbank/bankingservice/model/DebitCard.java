package com.sangam.abcbank.bankingservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "debit_cards",
        uniqueConstraints = @UniqueConstraint(columnNames = "card_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebitCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, length = 16)
    private String cardNumber;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "expiry_month_year", nullable = false)
    private YearMonth expiryDate;

    // Never store or return the raw CVV — only its hash
    @Column(length = 100)
    private String cvvHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(name = "daily_limit", nullable = false)
    private BigDecimal dailyLimit;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}