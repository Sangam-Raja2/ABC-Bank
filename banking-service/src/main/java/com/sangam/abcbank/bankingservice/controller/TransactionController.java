package com.sangam.abcbank.bankingservice.controller;

import com.sangam.abcbank.bankingservice.dto.TransactionResponse;
import com.sangam.abcbank.bankingservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final AccountService accountService;

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable String accountNumber,
                                                                 Authentication authentication) {
        return ResponseEntity.ok(accountService.getTransactionHistory(accountNumber, authentication));
    }
}
