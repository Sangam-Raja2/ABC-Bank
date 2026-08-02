package com.sangam.abcbank.emiservice.controller;

import com.sangam.abcbank.emiservice.dto.EmiTransitionRequest;
import com.sangam.abcbank.emiservice.dto.EmiTransitionResponse;
import com.sangam.abcbank.emiservice.service.EmiTransitionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emi/transition")
public class EmiTransitionController {

    private final EmiTransitionService transitionService;

    public EmiTransitionController(EmiTransitionService transitionService) {
        this.transitionService = transitionService;
    }

    /** Convert a credit-card transaction or an outstanding loan balance into EMI. */
    @PostMapping
    public EmiTransitionResponse initiate(@Valid @RequestBody EmiTransitionRequest request,
                                           @RequestHeader("Authorization") String authHeader,
                                           Authentication authentication) {
        String token = authHeader.substring(7); // strip "Bearer "
        return transitionService.initiateTransition(authentication, token, request);
    }

    /** History of EMI transitions for the logged-in user. */
    @GetMapping("/history")
    public List<EmiTransitionResponse> history(Authentication authentication) {
        return transitionService.getHistory(authentication.getName());
    }
}
