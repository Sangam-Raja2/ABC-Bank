package com.sangam.abcbank.bankingservice.controller;

import com.sangam.abcbank.bankingservice.dto.*;
import com.sangam.abcbank.bankingservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /** Create a new account. Owned by the currently authenticated user. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request,
                                                          Authentication authentication) {
        AccountResponse response = accountService.createAccount(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** List accounts owned by the currently authenticated user. */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMyAccounts(authentication));
    }

    /** Admin-only: list every account in the bank. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber,
                                                       Authentication authentication) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber, authentication));
    }

    @PutMapping("/{accountNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String accountNumber,
                                                          @RequestBody UpdateAccountRequest request,
                                                          Authentication authentication) {
        return ResponseEntity.ok(accountService.updateAccount(accountNumber, request, authentication));
    }

    @PostMapping("/{accountNumber}/deposit")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AccountResponse> deposit(@PathVariable String accountNumber,
                                                    @Valid @RequestBody AmountRequest request,
                                                    Authentication authentication) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, request, authentication));
    }

    @PostMapping("/{accountNumber}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable String accountNumber,
                                                     @Valid @RequestBody AmountRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.ok(accountService.withdraw(accountNumber, request, authentication));
    }

    @GetMapping("/{accountNumber}/balance")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountNumber,
                                                       Authentication authentication) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber, authentication));
    }
}
