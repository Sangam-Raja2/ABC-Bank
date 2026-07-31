package com.sangam.abcbank.loanservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;

    private String loanAccountNumber;

    private String action;

    private String performedBy;

    private Set<String> role;

    private LocalDateTime actionTime;

    private String remarks;
}
