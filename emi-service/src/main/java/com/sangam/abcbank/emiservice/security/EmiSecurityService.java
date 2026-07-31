package com.sangam.abcbank.emiservice.security;


import com.sangam.abcbank.emiservice.client.LoanServiceClient;
import com.sangam.abcbank.emiservice.dto.LoanDto;
import com.sangam.abcbank.emiservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Bean referenced from @PreAuthorize SpEL expressions, e.g.:
 *   @PreAuthorize("@emiSecurityService.canAccessLoan(#loanId, authentication)")
 *
 * Rule: allow if the caller is ROLE_ADMIN, OR if the caller's username matches
 * the username on the loan record (loan-service is the source of truth for ownership).
 */
@Service("emiSecurityService")
@RequiredArgsConstructor
public class EmiSecurityService {

    private final LoanServiceClient loanServiceClient;

    public boolean canAccessLoan(Long loanId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (isAdmin(authentication)) {
            return true;
        }

        LoanDto loan = loanServiceClient.getLoanById(loanId);
        if (loan == null) {
            throw new ResourceNotFoundException("Loan not found with id: " + loanId);
        }

        String callerUsername = authentication.getName();
        return loan.getUsername() != null && loan.getUsername().equalsIgnoreCase(callerUsername);
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
