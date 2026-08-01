package com.sangam.abcbank.bankingservice.controller;

import com.sangam.abcbank.bankingservice.dto.*;
import com.sangam.abcbank.bankingservice.service.DebitCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debit-cards")
@RequiredArgsConstructor
public class DebitCardController {

    private final DebitCardService debitCardService;

    /** Issue a new debit card linked to one of the caller's own accounts (or any account, for admins). */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<DebitCardResponse> issueCard(@Valid @RequestBody IssueDebitCardRequest request,
                                                       Authentication authentication) {
        DebitCardResponse response = debitCardService.issueCard(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<CardIssueResponse> issuePhysicalCard(
            @RequestBody @Valid IssueCardRequest request,
            Authentication authentication) {
        CardIssueResponse response = debitCardService.issuePhysicalCard(authentication, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/my",produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<List<DebitCardResponse>> getMyCards(Authentication authentication) {
        return ResponseEntity.ok(debitCardService.getMyCards(authentication));
    }

    @GetMapping("/{cardNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<DebitCardResponse> getCard(@PathVariable String cardNumber,
                                                     Authentication authentication) {
        return ResponseEntity.ok(debitCardService.getCard(cardNumber, authentication));
    }

    @PutMapping("/{cardNumber}/block")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<DebitCardResponse> blockCard(@PathVariable String cardNumber,
                                                       Authentication authentication) {
        return ResponseEntity.ok(debitCardService.blockCard(cardNumber, authentication));
    }

    @PutMapping("/{cardNumber}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<DebitCardResponse> activateCard(@PathVariable String cardNumber,
                                                          Authentication authentication) {
        return ResponseEntity.ok(debitCardService.activateCard(cardNumber, authentication));
    }

    @DeleteMapping("/{cardNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<DebitCardResponse> cancelCard(@PathVariable String cardNumber,
                                                        Authentication authentication) {
        return ResponseEntity.ok(debitCardService.cancelCard(cardNumber, authentication));
    }

    @PostMapping("/{cardNumber}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<AccountResponse> pay(@PathVariable String cardNumber,
                                               @Valid @RequestBody CardPaymentRequest request,
                                               Authentication authentication) {
        return ResponseEntity.ok(debitCardService.payWithCard(cardNumber, request, authentication));
    }
}