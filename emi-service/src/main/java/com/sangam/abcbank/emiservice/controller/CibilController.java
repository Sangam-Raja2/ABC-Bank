package com.sangam.abcbank.emiservice.controller;

import com.sangam.abcbank.emiservice.dto.CibilScoreRequest;
import com.sangam.abcbank.emiservice.dto.CibilScoreResponse;
import com.sangam.abcbank.emiservice.service.CibilService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emi/cibil")
public class CibilController {

    private final CibilService cibilService;

    public CibilController(CibilService cibilService) {
        this.cibilService = cibilService;
    }

    /** Check CIBIL score for the logged-in user by PAN. Mocked until a real bureau is wired in. */
    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public CibilScoreResponse check(@Valid @RequestBody CibilScoreRequest request, Authentication authentication) {
        return cibilService.checkScore(authentication, request);
    }
}
