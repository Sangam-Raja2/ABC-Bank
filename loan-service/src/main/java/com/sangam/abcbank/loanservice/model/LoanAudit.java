package com.sangam.abcbank.loanservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "loan_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String loanAccountNumber;

    private String action;

    private String performedBy;

    private Set<String> roles;

    private LocalDateTime actionTime;

    private String remarks;

}
