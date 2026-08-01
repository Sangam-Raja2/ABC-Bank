package com.sangam.abcbank.common.util;

import  com.sangam.abcbank.common.dto.BureauScoreResult;
import com.sangam.abcbank.common.exception.CibilBureauUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;

@Component
@Profile({"dev", "test", "local"}) // never active in prod
@Slf4j
public class MockCibilBureauClient {

    private static final int MIN_SCORE = 300;
    private static final int MAX_SCORE = 900;

    public BureauScoreResult fetchScore(String panNumber) {
        if (panNumber == null || panNumber.isBlank()) {
            throw new IllegalArgumentException("PAN number must not be null or blank");
        }

        // Simulate occasional bureau downtime/failure for realistic testing
        simulateOccasionalFailure(panNumber);

        // Simulate network latency
        simulateLatency();

        int score = deterministicScoreFromPan(panNumber);
        log.info("[MOCK] Generated CIBIL score {} for PAN {}", score, maskPan(panNumber));

        return new BureauScoreResult(score, LocalDate.now());
    }

    /**
     * Hashes the PAN into a stable score between MIN_SCORE and MAX_SCORE.
     * Same PAN always yields the same score across calls/runs.
     */
    private int deterministicScoreFromPan(String panNumber) {
        int hash = Math.abs(panNumber.hashCode());
        return MIN_SCORE + (hash % (MAX_SCORE - MIN_SCORE + 1));
    }

    private void simulateLatency() {
        try {
            Thread.sleep(150 + new SecureRandom().nextInt(350)); // 150-500ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateOccasionalFailure(String panNumber) {
        // ~5% simulated failure rate, deterministic per PAN so it's reproducible
        int hash = Math.abs(panNumber.hashCode());
        if (hash % 20 == 0) {
            throw new CibilBureauUnavailableException(
                    "Mock bureau timeout for PAN: " + maskPan(panNumber));
        }
    }

    private String maskPan(String pan) {
        if (pan.length() < 4) return "****";
        return "****" + pan.substring(pan.length() - 4);
    }
}
