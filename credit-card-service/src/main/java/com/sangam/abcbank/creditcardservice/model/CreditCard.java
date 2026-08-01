package com.sangam.abcbank.creditcardservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "credit_cards",
        uniqueConstraints = @UniqueConstraint(columnNames = "card_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, length = 16)
    private String cardNumber;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "expiry_month_year", nullable = false)
    private YearMonth expiryDate;

    // Never store or return the raw CVV — only its hash
    @Column(length = 100, nullable = false)
    private String cvvHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name = "available_credit", nullable = false)
    private BigDecimal availableCredit;

    // Day of month the billing cycle closes, e.g. 1-28
    @Column(name = "billing_cycle_day", nullable = false)
    private Integer billingCycleDay;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (availableCredit == null) {
            availableCredit = creditLimit;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
