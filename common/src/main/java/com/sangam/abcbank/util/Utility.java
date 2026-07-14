package com.sangam.abcbank.util;

import com.sangam.abcbank.dto.CommonUser;
import org.springframework.security.core.Authentication;

public class Utility {
    public static CommonUser getFromPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CommonUser) {
            return  (CommonUser) principal;
            // Proceed with logic
        } else if (principal instanceof String) {
            // The principal is just the username string. Fetch the full DTO from your database/service:
            return null;

        }else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }
    }
}
