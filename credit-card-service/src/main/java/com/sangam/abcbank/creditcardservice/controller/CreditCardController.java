package com.sangam.abcbank.creditcardservice.controller;

import com.sangam.abcbank.creditcardservice.dto.CreditCardIssueResponse;
import com.sangam.abcbank.creditcardservice.dto.CreditCardResponse;
import com.sangam.abcbank.creditcardservice.dto.IssueCreditCardRequest;
import com.sangam.abcbank.creditcardservice.service.CreditCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-cards")
public class CreditCardController {

    private final CreditCardService creditCardService;

    public CreditCardController(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @PostMapping("/issue")
    public ResponseEntity<CreditCardIssueResponse> issueCard(
            @RequestBody @Valid IssueCreditCardRequest request,
            Authentication authentication) {
        CreditCardIssueResponse response = creditCardService.issueCard(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<CreditCardResponse>> getMyCards(Authentication authentication) {
        return ResponseEntity.ok(creditCardService.getMyCards(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponse> getCard(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(creditCardService.getCard(id, authentication));
    }
}
