package com.sangam.abcbank.loanservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;

    private Long loanId;

    private String action;

    private String performedBy;

    private String role;

    private LocalDateTime actionTime;

    private String remarks;
}
