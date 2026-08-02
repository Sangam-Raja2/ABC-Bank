package com.sangam.abcbank.emiservice.service;

import com.sangam.abcbank.common.dto.CommonUser;
import com.sangam.abcbank.common.util.MockCibilBureauClient;
import com.sangam.abcbank.common.util.Utility;
import com.sangam.abcbank.emiservice.dto.CibilScoreRequest;
import com.sangam.abcbank.emiservice.dto.CibilScoreResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * CIBIL score check.
 *
 * IMPORTANT: Real CIBIL/TransUnion bureau access requires a commercial agreement, signed
 * customer consent (per RBI/credit information company regulations), and a paid API - it
 * cannot just be called from application code without that in place.
 *
 * This is a mock provider so the rest of the system (EMI eligibility checks, risk grading)
 * can be built and tested end-to-end now. To go live: implement a second CibilProvider that
 * calls the real bureau API using services.cibil.base-url / services.cibil.api-key, and swap
 * it in based on services.cibil.provider.
 */
@Service
public class CibilService {

    private final MockCibilBureauClient mockCibilBureauClient;

    public CibilService(MockCibilBureauClient mockCibilBureauClient) {
        this.mockCibilBureauClient = mockCibilBureauClient;
    }

    public CibilScoreResponse checkScore(Authentication authentication, CibilScoreRequest request) {
        // Deterministic-ish mock: seed on PAN so repeated checks for the same PAN return the
        // same score within a session (purely cosmetic - replace entirely for real bureau calls).

        CommonUser commonUser = Utility.getFromPrincipal(authentication);
        int cibilScore = mockCibilBureauClient.fetchScore(commonUser.getUsername()).score();// 300-900

        return new CibilScoreResponse(
                commonUser.getUsername(),
                request.panNumber(),
                cibilScore,
                gradeFor(cibilScore),
                "MockCibilBureauClient",
                LocalDateTime.now()
        );
    }

    private String gradeFor(int score) {
        if (score < 550) return "POOR";
        if (score < 650) return "FAIR";
        if (score < 750) return "GOOD";
        if (score < 800) return "VERY_GOOD";
        return "EXCELLENT";
    }
}
